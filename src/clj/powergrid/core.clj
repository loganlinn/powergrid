(ns powergrid.core
  (:require [powergrid.util :refer [separate]]
            [powergrid.game :as g]
            [powergrid.domain.protocols :as pc]
            [powergrid.domain.player :as p]
            [powergrid.domain.resource :as r]
            [powergrid.message :as msg]
            [powergrid.messages.factory :as msgs]
            [powergrid.util.error :refer [has-failed? error-m fail failf]]
            [clojure.algo.monads :refer [with-monad domonad m-chain]]))

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
           (g/update-resource resource pc/send-resource :supply amt)
           (g/update-resource resource pc/accept-resource :market amt)))
     game
     rate)))

(defmulti pre-phase g/current-phase)
(defmulti post-phase g/current-phase)
(defmulti prep-step g/current-step)
(defmulti post-step g/current-step)
(defmulti phase-complete? g/current-phase)
(defmulti step-complete? g/current-step)

(defmethod pre-phase :default [game] game)
(defmethod post-phase :default [game] game)
(defmethod prep-step :default [game] game)
(defmethod post-step :default [game] game)

(defmethod phase-complete? :default [game] (not (g/turns-remain? game)))
(defmethod step-complete? :default [game] false)

(defmethod pre-phase 1 [{:keys [round] :as game}]
  (cond-> game
    (not= round 1) g/update-turn-order
    true g/clear-turns))

(defmethod pre-phase 2 [game]
  (g/reset-turns game))

(defn post-phase-2-step-3-card
  [game]
  (-> game
      (g/remove-power-plant (g/step-3-card) :future)
      (g/drop-lowest-power-plant)))

(defn- too-many-plants?
  [max-plants player]
  (> (count (p/power-plants player)) max-plants))

(defmethod post-phase 2 [{:keys [step-3-card?] :as game}]
  (if-let [plr (some (partial too-many-plants? (g/max-power-plants game))
                     (g/players game))]
    (failf "Player %s needs to discard a power-plant" (name (p/id plr)))
    (if step-3-card?
      (post-phase-2-step-3-card game)
      game)))

(defmethod pre-phase 3 [{:keys [round] :as game}]
  (cond-> game
    (= round 1) (g/update-turn-order)
    true (g/reset-turns)))

(defmethod pre-phase 4 [game]
  (-> game (g/reset-turns)))

(defmethod pre-phase 5 [game]
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

(with-monad error-m
  (def next-phase (m-chain [post-phase g/inc-phase pre-phase]))
  (def next-step (m-chain [post-step g/inc-step prep-step]))

  (defn tick-phase [game]
    (if (phase-complete? game) (recur (next-phase game)) game))

  (defn tick-step [game]
    (if (step-complete? game) (recur (next-step game)) game))

  (def tick (m-chain [tick-step tick-phase])))

(def ^:dynamic *default-error-fn* nil)
(def ^:dynamic *default-success-fn* nil)
(def ^:dynamic *default-logger* msg/log-logger)

;; TODO cleanup
(defn update-game
  [game msg & {success-fn :success error-fn :error logger :logger
               :or {error-fn *default-error-fn*
                    success-fn *default-success-fn*
                    logger *default-logger*}}]
  (domonad error-m
    [result (tick (msg/apply-message game msg logger))]
    (if (has-failed? result)
      (do
        (if error-fn (error-fn game msg (:message result)))
        game)
      (do
        (if success-fn (success-fn game result msg))
        result))))
