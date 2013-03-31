(ns powergrid.core
  (:require [powergrid.power-plants :refer :all]))

;; TODO Remove
(use 'clojure.pprint)

;; TODO move to utils
(defn separate
  [f s]
  "Returns a vector: [ (filter f s) (filter (complement f) s) ]"
  [(filter f s) (filter (complement f) s)])

;; DATA STRUCTURES

(defrecord Player [id ctx money cities power-plants])
(defrecord Game [id phase step round resources power-plants players turns])

;; GENERAL GAME RULES

(def phases
  ["Determine Player Order"
   "Auction Power Plants"
   "Buying Resources"
   "Building"
   "Bureaucracy"])

(defn num-regions-chosen
  "Returns the number of regions chosen on map"
  [num-players]
  (case (int num-players)
    (2 3) 3
    4 4
    (5 6) 5))

(defn num-randomly-removed-power-plants
  "Returns the number of randomly removed power plants after preparing the
  power plant market"
  [num-players]
  (case (int num-players)
    (2 3) 8
    4 4
    (5 6) 0))

(defn num-cities-trigger-step-2
  "Returns the number of connected cities needed to trigger step 2"
  [num-players]
  (case (int num-players)
    2 10
    (3 4 5) 7
    6 6))

(defn max-power-plants
  "Returns the max number of player plants a player can have"
  [num-players]
  (case (int num-players)
    2 4
    (3 4 5 6) 3))

(defn num-cities-trigger-end
  "Returns the number of connected cities to trigger game end"
  [num-players]
  (case (int num-players)
    2 21
    (3 4) 17
    5 15
    6 14))

(defn step-3-card [] :step-3)
(defn step-3-card? [card] (= :step-3 card))

;; GAME INITIALIZATION

(defn init-resources
  []
  {:market {1 {:coal 3 :oil 0 :garbage 0 :uranium 0}
            2 {:coal 3 :oil 0 :garbage 0 :uranium 0}
            3 {:coal 3 :oil 3 :garbage 0 :uranium 0}
            4 {:coal 3 :oil 3 :garbage 0 :uranium 0}
            5 {:coal 3 :oil 3 :garbage 0 :uranium 0}
            6 {:coal 3 :oil 3 :garbage 0 :uranium 0}
            7 {:coal 3 :oil 3 :garbage 3 :uranium 0}
            8 {:coal 3 :oil 3 :garbage 3 :uranium 0}
            10 {:uranium 0}
            12 {:uranium 0}
            14 {:uranium 1}
            16 {:uranium 1}}
   :supply {:coal 0
            :oil 6
            :garbage 16
            :uranium 10}})

(defn init-power-plant-deck
  [power-plants num-players]
  (let [[card-13 deck] (separate #(= (power-plant-number %) 13) power-plants)
        recombine #(concat card-13 % [(step-3-card)])]
    (-> deck
      (shuffle)
      (drop (num-randomly-removed-power-plants num-players))
      (recombine))))

(defn init-power-plants
  [num-players]
  {:market (take 4 power-plant-cards)
   :future (take 4 (drop 4 power-plant-cards))
   :deck (init-power-plant-deck (drop 8 power-plant-cards) num-players)})

(defn init-players
  [num-players]
  (vec (for [i (range 1 (inc num-players))]
         (map->Player {:id i
                       :money 50
                       :cities #{}
                       :power-plants {}}))))

(defn init-state
  [num-players]
  (map->Game {:id (str (java.util.UUID/randomUUID))
              :phase 1
              :step 1
              :round 1
              :resources    (init-resources)
              :power-plants (init-power-plants num-players)
              :players      (init-players num-players)
              :turns []}))

(defn inc-phase
  [state]
  (update-in state [:phase] inc))

(defn inc-step
  [state]
  (update-in state [:step] inc))

(defn inc-round
  [state]
  (update-in state [:round] inc))

(defn players
  [state]
  (get state :players))

(defn num-players
  [state]
  (count (players state)))

(defn current-player
  "Returns player who's turn it is, otherwise nil"
  [state]
  (if-let [i (first (:turns state))]
    (nth (players state) i)))

(defn update-turns
  "Updates state for next player in turn"
  [state]
  (update-in state [:turns] rest))

(defn turns-remain?
  "Returns true if turns still exist in phase, otherwise false."
  [state]
  (boolean (seq (:turns state))))

(defn resource-market
  "Returns current resource market"
  [state]
  (get-in state [:resources :market]))

(defn set-resource-market
  "Updates current resource market in state"
  [state resource-market]
  (assoc-in state [:resources :market] resource-market))

(defn resource-supply
  "Returns resource supply"
  [state]
  (get-in state [:resources :supply]))

(declare network-size)

(defn max-network-size
  "Returns the maximum number of cities a single player has built"
  [state]
  (apply max (map network-size (players state))))

(defn resource-available
  "Returns the number of units of resources currently available (regardless of price)"
  [resources resource]
  (reduce #(+ %1 (get %2 resource 0)) 0 (vals resources)))

(defn accepts-resource?
  "Returns true if the power-plant accepts the resource, otherwise false"
  [{power-plant-resource :resource} resource]
  (if (set? power-plant-resource)
    (contains? power-plant-resource resource)
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

(defn update-money
  "Updates player's money by amt"
  [player amt]
  (assoc player :money (+ (:money player 0) amt)))

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

(defn can-buy-resource?
  "Returns true if player can buy resource type, otherwise false. User must own
  power plant that accepts the resource"
  [player resource]
  (some #(accepts-resource? % resource) (power-plants player)))

(defn resource-capacities
  "Returns map from resource to amount of remaining capacity for player based on
  the power plants he owns and current resources"
  [player]
  (reduce
    (fn [m [power-plant utilization]]
      (let [avail-cap (- (* 2 (:capacity power-plant))
                         (apply + (vals utilization)))]
       (update-in m [(:resource power-plant)]
                 (fnil #(+ avail-cap %) 0))))
    {}
    (:power-plants player)))

(defn has-capacity?
  "Returns true if resources fits into capacities, otherwise false.
  resources is a map of resource to quantity."
  [capacities resources]
  (let [capacities (reduce (fn [c [r n]] (assoc c r (- (get c r 0) n)))
                           capacities resources)
        ;; Handle hybrids
        capacities (reduce
                     (fn [c [s cap]]
                       (let [[c* cap*]
                             (reduce
                               (fn [[c cap] r]
                                 (let [x (get c r)]
                                   (if (and (neg? x) (> cap x))
                                     [(assoc c r 0) (+ cap x)]
                                     [c cap])))
                               [c cap]
                               s)]
                         (assoc c* s cap*)))
                     capacities
                     (filter (comp set? key) capacities))]
    (not-any? neg? (vals capacities))))

(defn max-power-plant
  "Returns the highest power-plant number the player owns"
  [player]
  (when-let [players-plants (power-plants player)]
   (apply max (map :number players-plants))))

(defn player-order
  "Returns players after sorting for according to following rules:
  First player is player with most cities in network. If two or more players
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

(defn power-plant-order
  "Returns state after ordering the power-plants"
  [{:keys [step power-plants] :as state}]
  (let [{:keys [market future]} power-plants
        step-3-card (filter step-3-card? future)
        combined (filter (complement step-3-card?) (concat market future))
        ordered (sort-by :number combined)
        split-ind (if (= step 3) 6 4)
        [market future] (split-at split-ind ordered)]
    (-> state
      (assoc-in [:power-plants :market] market)
      (assoc-in [:power-plants :future] future))))

(defn take-power-plant
  "Returns state after removing power-plant from the current power-plant market"
  [state power-plant]
  (update-in state [:power-plants :market] (partial remove #(= % power-plant))))

(defn drop-lowest-power-plant
  "Removes lowest power-plant from market. Assumes power-plant market is in
  order. Note, no replacement is drawn."
  [state]
  (update-in state [:power-plants :market] rest))

(defn add-to-power-plant-market
  "Returns state after adding power-plant to the power plant market and
  re-ordering"
  [state power-plant]
  (-> state
    (update-in [:power-plants :future] conj power-plant)
    (power-plant-order)))

(defn handle-step-3-card
  "Returns state after handling the Step 3 card"
  [{:keys [phase] :as state} step-3-card]
  (let [state (-> state
                (update-in [:power-plants :deck] shuffle)
                (assoc :step-3-card? true))]
    (if (= phase 2)
      (add-to-power-plant-market step-3-card)
      (power-plant-order (drop-lowest-power-plant state)))))

(defn draw-power-plant
  "Returns state after moving card from power-plant deck to market and
  re-ordering"
  [state]
  (let [[draw & deck] (get-in state [:power-plants :deck])]
    (if (step-3-card? draw)
      (-> state
        (assoc-in [:power-plants :deck] deck)
        (handle-step-3-card draw))
      (-> state
        (assoc-in [:power-plants :deck] deck)
        (add-to-power-plant-market draw)))))

(defn init-turns
  [num-players reverse-order?]
  (if reverse-order?
    (reverse (range num-players))
    (range num-players)))

(defmulti prep-phase :phase)
(defmulti post-phase :phase)
(defmulti prep-step :step)
(defmulti post-step :step)
(defmulti phase-complete? :phase)
(defmulti step-complete? :step)
(defmulti do-phase :phase)

(defmethod prep-phase :default [state] state)
(defmethod post-phase :default [state] state)
(defmethod prep-step :default [state] state)
(defmethod post-step :default [state] state)
(defmethod phase-complete? :default [state] (turns-remain? state))
(defmethod step-complete? :default [state] false)

(defmethod prep-phase 1 [state]
  (assoc state :turns []))

(defmethod prep-phase 2 [state]
  (assoc state :turns (init-turns (num-players state) false)))

(defn cleanup-step-3-card
  [state]
  (-> state
    (update-in [:power-plants :future]
               (partial filter (complement step-3-card?)))
    (drop-lowest-power-plant)))

(defmethod post-phase 2 [{:keys [round step-3-card?] :as state}]
  (cond-> state
    step-3-card? (cleanup-step-3-card)
    (= round 1) (update-in state [:players] player-order)))

(defmethod prep-phase 3 [state]
  (assoc state :turns (init-turns (num-players state) true)))

(defmethod prep-phase 4 [state]
  (assoc state :turns (init-turns (num-players state) true)))

(defmethod post-phase 5 [state]
  (inc-round state))

(defmethod prep-step 2 [state]
  (-> state
    (drop-lowest-power-plant)
    (draw-power-plant)))

(defmethod prep-step 3 [state]
  (dissoc state :step-3-card?))

(defmethod step-complete? 1 [state]
  (and (= (:phase state) 4)
       (not (turns-remain? state))
       (>= (max-network-size state)
           (num-cities-trigger-step-2 (num-players state)))))

(defmethod step-complete? 2 [state]
  (:step-3-card? state false))

(defn game-over?
  "Returns true if conditions have been to end the game, otherwise false"
  [state]
  (>= (max-network-size state)
      (num-cities-trigger-end (num-players state))))

(defn tick
  [state]
  (if (game-over? state)
    state
    (if (step-complete? state)
      (recur (-> state post-step inc-step prep-step))
      (if (phase-complete? state)
        (recur (-> state post-phase inc-phase prep-phase))
        (do-phase state)))))

;; PHASE 1

(defmethod do-phase 1
  [state]
  (update-in state [:players] player-order))

;; PHASE 2

(defmethod do-phase 2
  [state]
  )

;; PHASE 3

(defn consume-resource
  "Returns [updated-resources total-cost amount-purchased]. Consumes the first
  available units of resource, computes cost"
  [resources resource amount]
  (loop [resources resources
         [price & prices] (keys resources)
         amount amount
         total 0]
    (if (and price (> amount 0))
      (if-let [stock (get-in resources [price resource])]
        (let [x (min stock amount)]
          (recur (update-in resources [price resource] - x)
                 prices
                 (- amount x)
                 (int (+ total (* price x))))))
      [resources total amount])))

(defn get-resource-purchase-input
  "Returns map of users resource purchases. Maps resource to quantity.
  Handles validation that resources can be bought by user"
  [state player]
  ;; TODO Prompt user for purchase requests
  ;; TODO Validate resources exist
  ;; (resource-available)
  ;; TODO Validate user has resource capacity
  ;; (has-capacity?)
  )

(defn purchase-resources
  "Returns update state after processing player's purchases"
  [state player purchases]
  (reduce
    (fn [state [resource amount]]
      (let [[resources cost] (consume-resource (resource-market state) resource amount)]
        (-> state
          (set-resource-market resources)
          (update-player player update-money (- cost)))))
    state
    purchases))

(defmethod do-phase 3
  [state]
  (reduce
    (fn [state player]
      (purchase-resources
        state
        player
        (get-resource-purchase-input state player)))
    state
    (reverse (players state))))

(let [state (init-state 2)
      plant1 {:number 36, :resource :coal, :capacity 3, :yield 7}
      plant2 {:number 17, :resource :uranium, :capacity 1, :yield 2}
      plant3 {:number 12, :resource #{:coal :oil}, :capacity 2, :yield 2}
      [p1 p2] (players state)
      state (-> state
              (update-player p1 add-power-plant plant1)
              (update-player p1 add-power-plant plant3)
              (update-player p1 add-resources plant1 :coal 5)
              (update-player p1 add-resources plant3 :coal 1)
              ;(update-player p1 add-resources plant3 :oil 1)
              (update-player p2 add-power-plant plant2)
              (update-player p2 add-city :norfolk))
      [p1 p2] (players state)]
  ;(pprint (get-in state [:power-plants]))
  ;(pprint p1)
  ;(pprint (resource-capacities p1))
  )

