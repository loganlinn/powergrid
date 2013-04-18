(ns powergrid.messages.phase4
  (:require [powergrid.message :refer [Validated GameUpdate Passable]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(defrecord BuyCitiesMessage [player-id cities]
  Validated
  (validate [this game])

  GameUpdate
  (update-game [this game] game)

  Passable
  (passable? [_ _] true)
  (pass [_ game] game))

(def messages
  {:buy map->BuyCitiesMessage})
