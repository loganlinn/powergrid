(ns powergrid.core
  (:require [powergrid.game :refer :all]
            [powergrid.util :refer [separate]]
            [powergrid.power-plants :refer [is-hybrid? accepts-resource?]])
  (:import [powergrid.game Player Resource]))

;; TODO Remove
(use 'clojure.pprint)

(declare network-size)

(defn max-network-size
  "Returns the maximum number of cities a single player has built"
  [state]
  (apply max (map network-size (players state))))

(defn update-player
  "Returns state after updating player with f"
  [state player-key f & args]
  (apply update-in state [:players player-key] f args))

(defn network-size
  "Returns the number of cities in the player's network"
  [player]
  (count (:cities player)))

(defn power-plants
  "Returns the power-plants owned by player"
  [player]
  (keys (:power-plants player)))

(defn max-power-plant
  "Returns the highest power-plant number the player owns"
  [player]
  (when-let [players-plants (power-plants player)]
    (apply max (map :number players-plants))))

(defn update-money
  "Updates player's money by amt"
  [player amt]
  (assoc player :money (+ (:money player 0) amt)))

(defn purchase
  "Returns state after transferring amt Elektro from player to bank"
  [state player-key price]
  (-> state
      (update-player player-key update-money (- price))
      (update-in [:bank] (fnil + 0) price)))

(defn add-power-plant
  "Returns updated player after adding power-plant"
  [player power-plant]
  (assoc-in player [:power-plants power-plant] {}))

(defn owns-city?
  "Returns true if the player owns city, otherwise false"
  [player city]
  (contains? (:cities player) city))

(defn owns-power-plant?
  "Returns true if the player owns power-plant, otherwise false"
  [player power-plant]
  (contains? (:power-plants) power-plant))

(defn assign-resource
  "Returns updated player after storing resource in power plant.
  Asserts that power-plant accepts resource and player owns it."
  [player power-plant resource amount]
  {:pre [(accepts-resource? power-plant resource)
         (owns-power-plant? player power-plant)]}
  (update-in player
             [:power-plants power-plant resource]
             (fnil #(+ % amount) 0)))

(defn add-city
  "Returns updated player after adding city"
  [player city]
  (update-in player [:cities] conj city))

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

(defn player-order
  "Returns sorted players map using the following rules:
  First player is player with most cities in network. If two or more players
  are tied for the most number of cities, if the first player is the player
  among them with the largest-numbered power plant. Determine remaining player
  order using same rules"
  [players]
  (let [order-cols (juxt network-size max-power-plant)]
    (into {} (sort #(compare (order-cols (val %2))
                             (order-cols (val %1)))
                   players))))

(defn update-player-order
  "Returns state after updating player order"
  [state]
  (update-in state [:players] player-order))

(defn power-plant-order
  "Returns power-plants after re-ordering"
  [{:keys [market future] :as power-plants} step]
  (let [[step-3-card combined] (separate (complement step-3-card?) (concat market future))
        ordered (sort-by :number combined)
        [market future] (split-at (if (= step 3) 6 4) ordered)]
    (assoc power-plants
           :market market
           :future (concat future step-3-card))))

(defn update-power-plant-order
  "Returns state after ordering the power-plants"
  [{:keys [step power-plants] :as state}]
  (update-in state [:power-plants] power-plant-order state))

(defn remove-power-plant
  "Returns state after removing power-plant from the current power-plant market"
  ([state power-plant market]
   (update-in state [:power-plants market] (partial remove #(= % power-plant))))
  ([state power-plant]
   (remove-power-plant state power-plant :market)))

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
      (update-power-plant-order)))

(defn handle-step-3-card
  "Returns state after handling the Step 3 card"
  [{:keys [phase] :as state} step-3-card]
  (let [state (-> state
                  (update-in [:power-plants :deck] shuffle)
                  (assoc :step-3-card? true))]
    (if (= phase 2)
      (add-to-power-plant-market step-3-card)
      (update-power-plant-order (drop-lowest-power-plant state)))))

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

(defn reset-turns
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
  (assoc state :turns (reset-turns (num-players state) false)))

(defn post-phase-2-step-3-card
  [state]
  (-> state
      (remove-power-plant (step-3-card) :future)
      (drop-lowest-power-plant)))

(defmethod post-phase 2 [{:keys [round step-3-card?] :as state}]
  (cond-> state
    step-3-card? (post-phase-2-step-3-card)
    (= round 1) (update-player-order)))

(defmethod prep-phase 3 [state]
  (assoc state :turns (reset-turns (num-players state) true)))

(defmethod prep-phase 4 [state]
  (assoc state :turns (reset-turns (num-players state) true)))

(defmethod post-phase 5 [state]
  (inc-round state))

(defmethod prep-step 2 [state]
  (-> state
      (drop-lowest-power-plant)
      (draw-power-plant)))

(defmethod prep-step 3 [state]
  (-> state
      (dissoc :step-3-card?)
      (update-power-plant-order)))

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
  (update-player-order state))

;; PHASE 2

(defmethod do-phase 2
  [state]
  )

;; PHASE 3


(defn update-resource
  "Returns state after updating resource by applying f, args"
  [state resource f & args]
  (apply update-in [:resources resource] f args))

(defn get-resource
  "Returns the current state of resource"
  [state resource]
  (get-in state [:resources resource]))

(defmulti accept-resource
  (fn [trader dest amt] (class trader)))

(defmulti send-resource
  (fn [trader src amt] (class trader)))

(defn player-accept-resource*
  [power-plants resource amt]
  ;; TODO generalize this type of iteration?
  (loop [power-plants power-plants
         [[plant inventory] & r] power-plants
         amt amt]
    (if (and plant (pos? amt))
      (let [space-left (if (accepts-resource? plant resource)
                         (- (* 2 (:capacity plant))
                            (get inventory resource 0))
                         0)
            amt-stored (min space-left amt)]
        (recur (assoc power-plants plant (update-in inventory resource + amt-stored))
               r
               (- amt amt-stored)))
      power-plants)))

(defmethod accept-resource Player
  [player resource amt]
  ;; TODO ADD resource to pp (hybrids last)
  (update-in player [:power-plants] player-accept-resource* resource amt))

(defmethod send-resource Player
  [player [power-plant resource] amt]
  {:pre [(owns-power-plant? player power-plant)]}
  (update-in player [:power-plants power-plant resource] - amt))

(defmethod accept-resource Resource
  [resource dest amt]
  (update-in resource [dest] (fnil + 0) amt))

(defmethod send-resource Resource
  [resource dest amt]
  (update-in resource [dest] (fnil - 0) amt))

(defn purchase-resources
  "Returns state after processing player's purchases"
  [state player-key purchases]
  (reduce
    (fn [state [resource amt]]
      (let [price (resource-price (get-resource state resource) amt)]
        (-> state
            (update-resource resource send-resource :market (- amt))
            (update-player player-key accept-resource resource amt)
            (purchase player-key price))))
    state
    purchases))


(defmethod do-phase 3
  [state]
  ;; use purchase-resources
  )

;; PHASE 4

;; PHASE 5

(def resource-table
  {2 {:coal    {1 3, 2 4, 3 3}
      :oil     {1 2, 2 2, 3 4}
      :garbage {1 1, 2 2, 3 3}
      :uranium {1 1, 2 1, 3 1}}
   3 {:coal    {1 4, 2 5, 3 3}
      :oil     {1 2, 2 3, 3 4}
      :garbage {1 1, 2 2, 3 3}
      :uranium {1 1, 2 1, 3 1}}
   4 {:coal    {1 5, 2 6, 3 4}
      :oil     {1 3, 2 4, 3 5}
      :garbage {1 2, 2 3, 3 4}
      :uranium {1 1, 2 2, 3 2}}
   5 {:coal    {1 5, 2 7, 3 5}
      :oil     {1 4, 2 5, 3 6}
      :garbage {1 3, 2 3, 3 5}
      :uranium {1 2, 2 3, 3 2}}
   6 {:coal    {1 7, 2 9, 3 6}
      :oil     {1 5, 2 6, 3 7}
      :garbage {1 3, 2 5, 3 6}
      :uranium {1 2, 2 3, 3 3}}})


(defn resupply-rate
  "Returns a map of resource to amount to re-supply the resource market with,
  optionally taking into account the current resource supply"
  ([num-players step]
   (reduce (fn [m [resource rates]]
             (assoc m resource (get rates step)))
           {} (get resource-table num-players)))
  ([num-players step supply]
   (reduce (fn [m [resource rates]]
             (assoc m resource (min (get rates step) (get supply resource))))
           {} (get resource-table num-players))))

(defn resupply-resources
  "Returns state after resupplying the resource market according to rules."
  [{:keys [step] :as state}]
  (let [rate (resupply-rate (num-players state) step (resource-supply state))]
    ;; Subtract from supply, add to market
    ))

;; =====================

;(let [state (new-game [(new-player 1 nil :blue) (new-player 2 nil :black)])
      ;plant1 {:number 36, :resource :coal, :capacity 3, :yield 7}
      ;plant2 {:number 17, :resource :uranium, :capacity 1, :yield 2}
      ;plant3 {:number 12, :resource #{:coal :oil}, :capacity 2, :yield 2}
      ;[p1 p2] (players state)
      ;state (-> state
                ;(update-player p1 add-power-plant plant1)
                ;(update-player p1 add-power-plant plant3)
                ;(update-player p1 assign-resource plant1 :coal 5)
                ;(update-player p1 assign-resource plant3 :coal 1)
                ;;(update-player p1 assign-resource plant3 :oil 1)
                ;(update-player p2 add-power-plant plant2)
                ;(update-player p2 add-city :norfolk))
      ;[p1 p2] (players state)]
  ;(pprint state)
  ;(pprint (get-in state [:power-plants]))
  ;(pprint p1)
  ;(pprint (resource-capacities p1))
  ;)

