(ns powergrid.core
  (:require [powergrid.game :refer :all]
            [powergrid.util :refer [separate]]
            [powergrid.player :as p]
            [powergrid.resource :refer [ResourceTrader] :as r])
  (:import [powergrid.game Resource]
           [powergrid.player Player]))

;; TODO Remove
(use 'clojure.pprint)

(defn max-network-size
  "Returns the maximum number of cities a single player has built"
  [game]
  (apply max (map p/network-size (players game))))

(defn update-player
  "Returns game after updating player with f"
  [game player-key f & args]
  (apply update-in game [:players player-key] f args))

(defn purchase
  "Returns game after transferring amt Elektro from player to bank"
  [game player-key price]
  (-> game
      (update-player player-key p/update-money (- price))
      (update-in [:bank] (fnil + 0) price)))

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
  (let [order-cols (juxt p/network-size p/max-power-plant)]
    (into {} (sort #(compare (order-cols (val %2))
                             (order-cols (val %1)))
                   players))))

(defn update-player-order
  "Returns game after updating player order"
  [game]
  (update-in game [:players] player-order))

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
  "Returns game after ordering the power-plants"
  [{:keys [step power-plants] :as game}]
  (update-in game [:power-plants] power-plant-order game))

(defn remove-power-plant
  "Returns game after removing power-plant from the current power-plant market"
  ([game power-plant market]
   (update-in game [:power-plants market] (partial remove #(= % power-plant))))
  ([game power-plant]
   (remove-power-plant game power-plant :market)))

(defn drop-lowest-power-plant
  "Removes lowest power-plant from market. Assumes power-plant market is in
  order. Note, no replacement is drawn."
  [game]
  (update-in game [:power-plants :market] rest))

(defn add-to-power-plant-market
  "Returns game after adding power-plant to the power plant market and
  re-ordering"
  [game power-plant]
  (-> game
      (update-in [:power-plants :future] conj power-plant)
      (update-power-plant-order)))

(defn handle-step-3-card
  "Returns game after handling the Step 3 card"
  [{:keys [phase] :as game} step-3-card]
  (let [game (-> game
                 (update-in [:power-plants :deck] shuffle)
                 (assoc :step-3-card? true))]
    (if (= phase 2)
      (add-to-power-plant-market step-3-card)
      (update-power-plant-order (drop-lowest-power-plant game)))))

(defn draw-power-plant
  "Returns game after moving card from power-plant deck to market and
  re-ordering"
  [game]
  (let [[draw & deck] (get-in game [:power-plants :deck])]
    (if (step-3-card? draw)
      (-> game
          (assoc-in [:power-plants :deck] deck)
          (handle-step-3-card draw))
      (-> game
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

(defmethod prep-phase :default [game] game)
(defmethod post-phase :default [game] game)
(defmethod prep-step :default [game] game)
(defmethod post-step :default [game] game)
(defmethod phase-complete? :default [game] (turns-remain? game))
(defmethod step-complete? :default [game] false)

(defmethod prep-phase 1 [game]
  (assoc game :turns []))

(defmethod prep-phase 2 [game]
  (assoc game :turns (reset-turns (num-players game) false)))

(defn post-phase-2-step-3-card
  [game]
  (-> game
      (remove-power-plant (step-3-card) :future)
      (drop-lowest-power-plant)))

(defmethod post-phase 2 [{:keys [round step-3-card?] :as game}]
  (cond-> game
    step-3-card? (post-phase-2-step-3-card)
    (= round 1) (update-player-order)))

(defmethod prep-phase 3 [game]
  (assoc game :turns (reset-turns (num-players game) true)))

(defmethod prep-phase 4 [game]
  (assoc game :turns (reset-turns (num-players game) true)))

(defmethod post-phase 5 [game]
  (inc-round game))

(defmethod prep-step 2 [game]
  (-> game
      (drop-lowest-power-plant)
      (draw-power-plant)))

(defmethod prep-step 3 [game]
  (-> game
      (dissoc :step-3-card?)
      (update-power-plant-order)))

(defmethod step-complete? 1 [game]
  (and (= (:phase game) 4)
       (not (turns-remain? game))
       (>= (max-network-size game)
           (num-cities-trigger-step-2 (num-players game)))))

(defmethod step-complete? 2 [game]
  (:step-3-card? game false))

(defn game-over?
  "Returns true if conditions have been to end the game, otherwise false"
  [game]
  (>= (max-network-size game)
      (num-cities-trigger-end (num-players game))))

(defn tick
  [game]
  (if (game-over? game)
    game
    (if (step-complete? game)
      (recur (-> game post-step inc-step prep-step))
      (if (phase-complete? game)
        (recur (-> game post-phase inc-phase prep-phase))
        (do-phase game)))))

;; PHASE 1

(defmethod do-phase 1
  [game]
  (update-player-order game))

;; PHASE 2

(defmethod do-phase 2
  [game]
  )

;; PHASE 3


(defn update-resource
  "Returns game after updating resource by applying f, args"
  [game resource f & args]
  (apply update-in [:resources resource] f args))

(defn get-resource
  "Returns the current game of resource"
  [game resource]
  (get-in game [:resources resource]))

(extend-type Resource
  ResourceTrader
  (accept-resource [resource dest amt]
    (update-in resource [dest] (fnil + 0) amt))
  (send-resource [resource dest amt]
    (update-in resource [dest] (fnil - 0) amt)))

(defn purchase-resources
  "Returns game after processing player's purchases"
  [game player-key purchases]
  (reduce
    (fn [game [resource amt]]
      (let [price (resource-price (get-resource game resource) amt)]
        (-> game
            (update-resource resource r/send-resource :market (- amt))
            (update-player player-key r/accept-resource resource amt)
            (purchase player-key price))))
    game
    purchases))


(defmethod do-phase 3
  [game]
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
  "Returns game after resupplying the resource market according to rules."
  [{:keys [step] :as game}]
  (let [rate (resupply-rate (num-players game) step (resource-supply game))]
    ;; Subtract from supply, add to market
    ))

;; =====================

#_(let [game (new-game [(p/new-player 1 nil :blue) (p/new-player 2 nil :black)])
        plant1 {:number 36, :resource :coal, :capacity 3, :yield 7}
        plant2 {:number 17, :resource :uranium, :capacity 1, :yield 2}
        plant3 {:number 12, :resource #{:coal :oil}, :capacity 2, :yield 2}
        [p1 p2] (players game)
        game (-> game
                 (update-player p1 p/add-power-plant plant1)
                 (update-player p1 p/add-power-plant plant3)
                 (update-player p1 p/assign-resource plant1 :coal 5)
                 (update-player p1 p/assign-resource plant3 :coal 1)
                 (update-player p2 p/add-power-plant plant2)
                 (update-player p2 p/add-city :norfolk))
        [p1 p2] (players game)]
    (pprint game)
    (pprint (get-in game [:power-plants]))
    (pprint p1)
    (pprint (p/resource-capacities p1)))

