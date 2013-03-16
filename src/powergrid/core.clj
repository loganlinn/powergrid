(ns powergrid.core
  (:require [powergrid.power-plants :refer :all]))
(use 'clojure.pprint)

(def phases
  ["Determine Player Order"
   "Auction Power Plants"
   "Buying Resources"
   "Building"
   "Bureaucracy"])

(defn init-power-plants
  []
  (let [actual-market (take 4 power-plants)
        future-market (take 4 (drop 4 power-plants))
        deck (drop 8 power-plants)
        ;; remove 13 card
        card-13? #(= (:number %) 13)
        card-13 (filter card-13? deck)
        deck (filter (complement card-13?) deck)
        ;; shuffle rest
        deck (shuffle deck)
        ;; place card-13 on top
        deck (concat card-13 deck)]
    {:actual actual-market
     :future future-market
     :deck deck}))

(defn init-state
  [num-players]
  {:phase 1
   :round 1
   :resources {}
   :power-plants (init-power-plants)
   :players []})

;(pprint (:power-plants (init-state 2)))

(defn prompt-player
  [player prompt & {:keys [choices passable? formatter validator]}]
  )

(defn network-size
  [player]
  (count (:cities player)))

(defn max-power-plant
  [player]
  (apply max (map :number (:power-plants player))))

(defn update-money
  [players player-id amt]
  (map
    #(if (= (:id %) player-id)
       (assoc % :money (+ (:money % 0) amt))
       %)
    players))

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
  (update-in state :players player-order))

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
                 :choices (:actual (:power-plants state)))]
    (when choice
      (cons choice (negotiate-auction choice
                                      (cons player other-players))))))

(defn replace-power-plant
  "Returns power-plants after removing choice, drawing new power plant, and
  reordering according to rules."
  [power-plants choice]
  (let [[next-plant & deck] (:deck power-plants)
        actual-market (remove #(= % choice) (:actual power-plants))
        future-market (:future power-plants)
        markets (sort-by :number
                         (concat [next-plant] actual-market future-market))
        [actual-market future-market] (split-at (/ (count markets) 2) markets)]
    (merge power-plants
           {:actual actual-market
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
                     (update-in :power-plants replace-power-plant power-plant)
                     (update-in :players update-money (:id purchaser) (- price)))
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
      (update-in state :power-plants replace-power-plant
                 (-> state :power-plants :actual first))
      state)))

(defn phase-3
  [state]
  (let [round-players (reverse (:players state))]
    ))
