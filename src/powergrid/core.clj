(ns powergrid.core
  (:require [powergrid.game :refer :all]
            [powergrid.util :refer [separate]]
            [powergrid.player :as p]
            [powergrid.resource :as r]
            [powergrid.message :as msg]
            [powergrid.messages.factory :as msgs]
            [io.pedestal.service.log :as log]
            [slingshot.slingshot :refer [try+ throw+]])
  (:import [powergrid.message ValidationError]))

(defn player-order
  "Returns sorted players using the following rules:
  First player is player with most cities in network. If two or more players
  are tied for the most number of cities, if the first player is the player
  among them with the largest-numbered power plant. Determine remaining player
  order using same rules"
  [game players]
  (let [order-cols (juxt (partial network-size game) p/max-power-plant)]
    (sort #(compare (order-cols %2)
                    (order-cols %1))
          players)))

(defn update-player-order
  "Returns game after updating player order"
  [game]
  ;; TODO CLEANUP
  (update-players game (partial player-order game)))

(defn game-over?
  "Returns true if conditions have been to end the game, otherwise false"
  [game]
  (>= (max-network-size game)
      (num-cities-trigger-end (num-players game))))

(defn resupply-rate
  "Returns a map of resources to amounts to resupply for game"
  [game]
  (r/resupply-rate (num-players game)
                   (current-step game)
                   (resource-supply game)))

(defn resupply-resources
  "Returns game after resupplying the resource market according to rules."
  ([game]
   (resupply-resources game (resupply-rate game)))
  ([game rate]
   (reduce
     (fn [game [resource amt]]
       (-> game
           (update-resource resource r/send-resource :supply amt)
           (update-resource resource r/accept-resource :market amt)))
     game
     rate)))

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
  (clear-turns game))

(defmethod prep-phase 2 [game]
  (-> game (set-turns)))

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
  (-> game (set-turns)))

(defmethod prep-phase 4 [game]
  (-> game (set-turns)))

(defmethod post-phase 5 [game]
  (-> game
      (drop-lowest-power-plant)
      (draw-power-plant)
      (resupply-resources)
      (inc-round)))

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

;; =============================================================================

(defn next-phase
  [game]
  (-> game post-phase inc-phase prep-phase))

(defn next-step
  [game]
  (-> game post-step inc-step prep-step))

(defn update-game
  [game msg]
  (try+
    (cond-> (msg/apply-message game msg)
      (step-complete? game) (next-step)
      (phase-complete? game) (next-phase))
    (catch ValidationError e
      game)))

(comment

  (defn recursive-expansion
    [expander input]
    (let [output (expander input)]
      (if (= input output)
        input
        (recur expander output))))

  (defn update-state [current-state event]
    ;; return updated state
    )

  (defn consequences [current-state event]
    ;; return sequence of events
    )

  (defn apply-consequences [current-state event]
    (reduce update-state current-state
            (consequences current-state event)))

  (defn recursive-consequences [current-state event]
    (reduce (fn [state event]
              (recursive-consequences
                state (update-state state event)))
            current-state
            (consequences current-state event)))

  (defn chain-consequences [initial-state consequences-fns]
    (loop [state initial-state
           fs    consequences-fns
           output []]
      (if (seq fs)
        (let [events ((first fs) state)
              new-state (reduce update-state state events)]
          (recur new-state (rest fs) (into output events)))
        output)))
  )
