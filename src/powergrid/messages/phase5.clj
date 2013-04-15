(ns powergrid.messages.phase5
  (:require [powergrid.message :refer [Validated GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(defrecord PowerCitiesMessage [player-id plant-id amt]
  Validated
  (validate [this game] true)
  GameUpdate
  (update-game [this game] game))

(def messages
  {:sell map->PowerCitiesMessage})
