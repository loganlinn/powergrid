(ns powergrid.core
  (:require [powergrid.game :refer :all]
            [powergrid.util :refer [separate]]
            [powergrid.player :as p]
            [powergrid.resource :as r]
            [powergrid.message :as msg]
            [powergrid.messages.factory :as msgs]
            [io.pedestal.service.log :as log]
            [slingshot.slingshot :refer [try+]])
  (:import [powergrid.message ValidateError]))

(defn purchase
  "Returns game after transferring amt Elektro from player to bank"
  [game player-key price]
  (-> game
      (update-player player-key p/update-money (- price))
      (update-in [:bank] (fnil + 0) price)))

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
  [game]
  (update-in game [:power-plants] power-plant-order (current-step game)))

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

(defmulti prep-phase current-phase)
(defmulti post-phase current-phase)
(defmulti prep-step current-step)
(defmulti post-step current-step)
(defmulti phase-complete? current-phase)
(defmulti step-complete? current-step)

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

(defn get-resource
  "Returns the current game of resource"
  [game resource]
  (get-in game [:resources resource]))

(defn purchase-resources
  "Returns game after processing player's purchases"
  [game player-key purchases]
  (reduce
    (fn [game [resource amt]]
      (let [price (r/resource-price (get-resource game resource) amt)]
        (-> game
            (update-resource resource r/send-resource :market amt)
            (update-player player-key r/accept-resource resource amt)
            (purchase player-key price))))
    game
    purchases))

(defn resupply-rate
  "Returns a map of resources to amounts to resupply for game"
  [game]
  (r/resupply-rate (num-players game)
                   (current-step game)
                   (resource-supply game)))

(defn resupply-resources
  "Returns game after resupplying the resource market according to rules."
  ([game]
   (resupply-resources (resupply-rate game)))
  ([game rate]
   (reduce
     (fn [game [resource amt]]
       (-> game
           (update-resource resource r/send-resource :supply amt)
           (update-resource resource r/accept-resource :market amt)))
     game
     rate)))

;; =============================================================================

(defn apply-message
  [game m]
  (when-let [msg (msgs/create-message m)]
    (when (satisfies? msg/Validated msg)
      (try+
        (msg/validate msg game)
        (if (satisfies? msg/GameUpdate msg)
          (msg/update-game msg game)
          game)
        (catch ValidateError e
          (log/error e)
          game)))))
