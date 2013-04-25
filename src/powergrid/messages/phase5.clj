(ns powergrid.messages.phase5
  (:require [powergrid.message :refer [Validated GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.power-plants :as pp]
            [powergrid.resource :as r]))

(def payout-values [10 22 33 44 54 64 73 82 90 98 105 112 118 124 129 134 138 142 145 148 150])

(defn payout
  "Returns the payout amount to receive from powering num-powered cities"
  [num-powered]
  {:pre [(not (neg? num-powered))]}
  (if (> num-powered (dec (count payout-values)))
    (last payout-values)
    (get payout-values num-powered)))

(defn invalid-sale?
  "Returns true of the plant-id and resource amt combo is valid.
  Does not validate the user owns the power-plants & resources"
  [[plant-id resources]]
  (if-let [plant (pp/plant plant-id)]
    (let [total (reduce (fnil + 0 0) (vals resources))]
      (if (pp/consumes-resources? plant)
        (or (not= total (:capacity plant))
            (some #(not (pp/accepts-resource? plant %)) (keys resources))
            (some neg? (vals resources)))
        (not= total 0)))
    true))

(defn can-sell?
  "Returns true if the user owns the power plant and has the valid amount of
  resources"
  [player-id [plant-id resources]]
  (when-let [player (g/player player-id)]
    (when-let [plant (pp/plant plant-id)]
      (and (p/owns-power-plant? player plant)
           (or (not (pp/consumes-resources? plant))
               (p/can-power-plant? player plant))))))

(defrecord PowerCitiesMessage [player-id sale]
  Validated
  (validate [this game]
    (cond
      (some invalid-sale? sale) "Invalid sale"
      (every? (partial can-sell? player-id) sale) "Invalid sale"))

  GameUpdate
  (update-game [this game] game))

(def messages
  {:sell map->PowerCitiesMessage})
