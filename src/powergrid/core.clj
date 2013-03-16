(ns powergrid.core
  (:require [powergrid.power-plants :refer :all]))
(use 'clojure.pprint)

(def phases
  ["Determine Player Order"
   "Auction Power Plants"
   "Buying Resources"
   "Building"
   "Bureaucracy"])

(defn init-resources
  []
  {:market (concat
             (for [cost (range 1 9)]
               {:cost cost
                :coal 3
                :oil (if (>= cost 3) 3 0)
                :garbage (if (>= cost 7) 3 0)
                :uranium 0})
             [{:cost 10 :uranium 0}
              {:cost 12 :uranium 0}
              {:cost 14 :uranium 1}
              {:cost 16 :uranium 1}])
   :supply {:coal 0
            :oil 6
            :garbage 16
            :uranium 10}})

(defn init-power-plants
  []
  (let [actual-market (take 4 power-plant-cards)
        future-market (take 4 (drop 4 power-plant-cards))
        deck (drop 8 power-plant-cards)
        ;; remove 13 card
        card-13? #(= (:number %) 13)
        card-13 (filter card-13? deck)
        deck (filter (complement card-13?) deck)
        ;; shuffle rest
        deck (shuffle deck)
        ;; place card-13 on top & step-3 on bottom
        deck (concat card-13 deck [:step-3])]
    {:market actual-market
     :future future-market
     :deck deck}))

(defrecord Player [id money cities power-plants])

(defn init-players
  [num-players]
  (vec (for [i (range 1 (inc num-players))]
         (Player. i 50 #{} {}))))

(defn init-state
  [num-players]
  {:phase 1
   :round 1
   :resources    (init-resources)
   :power-plants (init-power-plants)
   :players      (init-players num-players)})

(defn prompt-player
  [player prompt & {:keys [choices passable? formatter validator]}]
  )

(defn accepts-resource?
  "Returns true if the power-plant accepts the resource, otherwise false"
  [{power-plant-resource :resource} resource]
  (if (coll? power-plant-resource)
    (boolean (some #(= resource %) power-plant-resource))
    (condp = power-plant-resource
      :ecological false
      :fusion     false
      resource    true
      false)))

(defn player-pos
  "Returns the position (0-index) of player in player order"
  [{:keys [players]} {pid :id}]
  (loop [[player & others] players i 0]
    (when player
      (if (= (:id player) pid)
        i
        (recur others (inc i))))))

(defn update-player
  "Returns state after updating player with f"
  [state player f & args]
  (apply update-in state [:players (player-pos state player)] f args))

(defn network-size
  "Returns the number of cities in the player's network"
  [player]
  (count (:cities player)))

(defn power-plants
  "Returns the power-plants owned by player"
  [player]
  (keys (:power-plants player)))

(defn add-power-plant
  "Returns updated player after adding power-plant"
  [player power-plant]
  (assoc-in player [:power-plants power-plant] {}))

(defn add-resources
  [player power-plant resource amount]
  {:pre [(accepts-resource? power-plant resource)
         (contains? (:power-plants player) power-plant)]}
  (update-in player
             [:power-plants power-plant resource]
             (fnil #(+ % amount) 0)))

(defn add-city
  "Returns updated player after adding city"
  [player city]
  (update-in player [:cities] conj city))

(defn owns-city?
  "Returns true of the player owns city, false otherwise"
  [player city]
  (contains? (:cities player) city))

(defn max-power-plant
  "Returns the highest power-plant number the player owns"
  [player]
  (when-let [players-plants (power-plants player)]
   (apply max (map :number players-plants))))

(defn update-money
  [player amt]
  (assoc player :money (+ (:money player 0) amt)))

;(let [state (init-state 2)
      ;plant1 {:number 36, :resource :coal, :capacity 3, :yield 7}
      ;plant2 {:number 17, :resource :uranium, :capacity 1, :yield 2}
      ;[p1 p2] (state :players)]
  ;(pprint (-> state
            ;(update-player p1 add-power-plant plant1)
            ;(update-player p1 add-resources plant1 :coal 5)
            ;(update-player p2 add-power-plant plant2)
            ;(update-player p2 add-city :norfolk)
            ;(update-player p1 update-money (+ 10)))))

(defn player-order
  "First player is player with most cities in network. If two or more players
  are tied for the most number of cities, if the first player is the player
  among them with the largest-numbered power plant. Determine remaining player
  order using same rules"
  [players]
  (sort
    (fn [p1 p2]
      (let [p1-cities (network-size p1)
            p2-cities (network-size p2)]
        (if (= p1-cities p2-cities)
          (compare (max-power-plant p2) (max-power-plant p1))
          (compare p2-cities p1-cities))))
    players))

;; PHASE 1

(defn phase-1
  [state]
  (update-in state [:players] player-order))

;; PHASE 2

(defn negotiate-auction
  "Prompts each bidder for bid until a single bidder remains.
  Returns tuple of [player price]"
  ([plant-num bidders bids]
   (let [[bidder & others] bidders
         bids? (not (empty? bids))
         min-bid (if bids? (apply max (vals bids)) plant-num)
         max-bid (:money bidder)
         ;; TODO handle min-bid > max-bid
         bid (prompt-player
               bidder
               (str "What would you like to bid for power-plant #" plant-num "? (min-bid: " min-bid ")")
               :formatter #(Integer/parseInt %)
               :validator #(and (>= % min-bid) (< % max-bid))
               :passable? bids?)
         bids (assoc bids bidder bid)]
     (if others
       (negotiate-auction plant-num
                          (if bid (conj others bidder) others)
                          bids)
       (apply max-key val bids))))
  ([plant-num bidders] (negotiate-auction plant-num bidders {})))


(defn do-auction
  "Returns tuple of [auctioned-power-plant purchasing-user price] if a power
  plant is purchased as result of auction. Otherwise, returns nil"
  [state player other-players]
  (let [choice (prompt-player
                 player
                 "Choose power plant for auction"
                 :passable? (not= 1 (:round state))
                 :choices (:market (:power-plants state)))]
    (when choice
      (cons choice (negotiate-auction choice
                                      (cons player other-players))))))

(defn replace-power-plant
  "Returns power-plants after removing choice, drawing new power plant, and
  reordering according to rules."
  [power-plants choice]
  (let [[next-plant & deck] (:deck power-plants)
        actual-market (remove #(= % choice) (:market power-plants))
        future-market (:future power-plants)
        markets (sort-by :number
                         (concat [next-plant] actual-market future-market))
        [actual-market future-market] (split-at (/ (count markets) 2) markets)]
    (merge power-plants
           {:market actual-market
            :future future-market
            :deck deck})))

(defn do-auctions
  "Runs auction for each player. Returns collection auctions, which are tuples
  of [power-plant-purchased purchasing-user purchase-amount]"
  [state]
  (second
    (loop [state state
           round-players (:players state) ;; players active in round's auctions
           auctions []]
      (if-let [player (first round-players)]
        (if-let [auction (do-auction state player (rest round-players))]
          (let [[power-plant purchaser price] auction]
            (recur (-> state
                     (update-in [:power-plants] replace-power-plant power-plant)
                     (update-player purchaser update-money (- price)))
                   (remove #(= purchaser %) round-players)
                   (conj auctions auction)))
          (recur state (rest round-players) auctions))
        [state auctions]))))

(defn phase-2
  [state]
  (let [[state auctions] (do-auctions state)]
    ;; If no power plant is sold in a round, the players remove the lowest
    ;; numbered power plant from the market, placing it back in the box, and
    ;; replace it by drawing a power plant from the draw stack
    (if (empty? auctions)
      (update-in state [:power-plants] replace-power-plant
                 (-> state :power-plants :market first))
      state)))

(defn phase-3
  [state]
  (let [round-players (reverse (:players state))]
    ))
