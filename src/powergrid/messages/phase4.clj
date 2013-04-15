(ns powergrid.messages.phase4
  (:require [powergrid.message :refer [Validated ->ValidateError GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(defrecord BuyCitiesMessage [player-id cities]
  Validated
  (validate [this game])
  GameUpdate
  (update-game [this game] game))

(defrecord TrashCityMessage [player-id city]
  Validated
  (validate [this game])
  GameUpdate
  (update-game [this game] game))

(def messages
  {:buy map->BuyCitiesMessage
   :trash nil
   :pass nil
   :end nil})
