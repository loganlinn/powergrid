(ns powergrid.core
  (:require [powergrid.util :refer [separate]]
            [powergrid.util.error :as error]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.resource :as r]
            [powergrid.message :as msg]
            [powergrid.messages.factory :as msgs]))

(defn game-over?
  "Returns true if conditions have been to end the game, otherwise false"
  [game]
  (>= (g/max-network-size game)
      (g/num-cities-trigger-end (g/num-players game))))

(defn resupply-rate
  "Returns a map of resources to amounts to resupply for game"
  [game]
  (r/resupply-rate (g/num-players game)
                   (g/current-step game)
                   (g/resource-supply game)))

(defn resupply-resources
  "Returns game after resupplying the resource market according to rules."
  ([game]
   (resupply-resources game (resupply-rate game)))
  ([game rate]
   (reduce
     (fn [game [resource amt]]
       (-> game
           (g/update-resource resource r/send-resource :supply amt)
           (g/update-resource resource r/accept-resource :market amt)))
     game
     rate)))

(defmulti prep-phase g/current-phase)
(defmulti post-phase g/current-phase)
(defmulti prep-step g/current-step)
(defmulti post-step g/current-step)
(defmulti phase-complete? g/current-phase)
(defmulti step-complete? g/current-step)

(defmethod prep-phase :default [game] game)
(defmethod post-phase :default [game] game)
(defmethod prep-step :default [game] game)
(defmethod post-step :default [game] game)

(defmethod phase-complete? :default [game] (not (g/turns-remain? game)))

(defmethod phase-complete? :phase2 [game]
  (let [max-plants (g/max-power-plants game)]
    (and (not (g/turns-remain? game))
         (not-any? #(> (count (p/power-plants %)) max-plants)
                   (g/players game)))))

(defmethod step-complete? :default [game] false)

(defmethod prep-phase 1 [{:keys [round] :as game}]
  (cond-> game
    (not= round 1) g/update-turn-order
    true g/clear-turns))

(defmethod prep-phase 2 [game]
  (g/reset-turns game))

(defn post-phase-2-step-3-card
  [game]
  (-> game
      (g/remove-power-plant (g/step-3-card) :future)
      (g/drop-lowest-power-plant)))

(defmethod post-phase 2 [{:keys [step-3-card?] :as game}]
  (cond-> game
    step-3-card? (post-phase-2-step-3-card)))

(defmethod prep-phase 3 [{:keys [round] :as game}]
  (cond-> game
    (= round 1) (g/update-turn-order)
    true (g/reset-turns)))

(defmethod prep-phase 4 [game]
  (-> game (g/reset-turns)))

(defmethod prep-phase 5 [game]
  (-> game (g/reset-turns)))

(defmethod post-phase 5 [game]
  (-> game
      (g/drop-lowest-power-plant)
      (g/draw-power-plant)
      (resupply-resources)
      (g/inc-round)))

(defmethod prep-step 2 [game]
  (-> game
      (g/drop-lowest-power-plant)
      (g/draw-power-plant)))

(defmethod prep-step 3 [game]
  (-> game
      (dissoc :step-3-card?)
      (g/update-power-plant-order)))

(defmethod step-complete? 1 [game]
  (and (= (:phase game) 4)
       (not (g/turns-remain? game))
       (>= (g/max-network-size game)
           (g/num-cities-trigger-step-2 (g/num-players game)))))

(defmethod step-complete? 2 [game]
  (:step-3-card? game false))

;; =============================================================================

(defn next-phase
  [game]
  (-> game post-phase g/inc-phase prep-phase))

(defn next-step
  [game]
  (-> game post-step g/inc-step prep-step))

(defn tick-phase
  [game]
  (if (phase-complete? game) (recur (next-phase game)) game))

(defn tick-step
  [game]
  (if (step-complete? game) (recur (next-step game)) game))

(defn tick
  [game]
  (-> game tick-step tick-phase))

(def ^:dynamic *default-error-fn* nil)
(def ^:dynamic *default-success-fn* nil)

(defn update-game
  [game msg & {success-fn :success error-fn :error
               :or {error-fn *default-error-fn* success-fn *default-success-fn*}}]
  (let [result (msg/apply-message game msg)]
    (if (error/has-failed? result)
      (do
        (if error-fn (error-fn game msg (:message result)))
        game)
      (let [game* (tick result)]
        (if success-fn (success-fn game game* msg))
        game*))))
