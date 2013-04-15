(ns powergrid.messages.phase5
  (:require [powergrid.message :refer [Validated GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.power-plants :as pp]
            [powergrid.resource :as r]))

;; { plant-id { resource amt } }
;; { plant-id { ecological 0 } }

(defn- valid-sale?
  "Returns true of the plant-id and resource amt combo is valid.
  Does not validate the user owns the power-plants & resources"
  [[plant-id resources]]
  (when-let [plant (pp/plant plant-id)]
    (if (pp/consumes-resources? plant)
      (every? #(and (pp/accepts-resource? plant (key %))
                    (<= (plant :capacity) (val %)))
              resources)
      (empty? resources))))

(defn- can-sell?
  "Returns true if the user owns the power plant and has the valid amount of
  resources"
  [player-id [plant-id resources]]
  (when-let [player (g/player player-id)]
    (when-let [plant (pp/plant plant-id)]
      (and (p/owns-power-plant? player plant)
           (or (not (pp/consumes-resources? plant))
               (p/has-plant-resources? player resources))))))

(defrecord PowerCitiesMessage [player-id sale]
  Validated
  (validate [this game]
    (and (every? valid-sale? sale)
         (every? (partial can-sell? player-id) sale)))

  GameUpdate
  (update-game [this game] game))

(def messages
  {:sell map->PowerCitiesMessage})
