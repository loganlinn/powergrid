(ns powergrid.messages.phase3
  (:require [powergrid.message :refer [Validated GameUpdate Passable]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]
            [powergrid.util :refer [kw]]))


(defrecord BuyResourcesMessage [player-id resources]
  Validated
  (validate [this game]
    (cond
      (empty? resources) "Invalid resources specified"
      (every? r/types (keys resources)) "Invalid resources specified"
      (every? pos? (vals resources)) "Invalid resource amount"
      (= player-id (g/current-turn game)) "Not your turn"
      (not (p/has-capacity? (g/player player-id) resources)) "Insufficient power-plant capacity"
      (not (g/contains-resource? game resources)) "Insufficient resources in market"
      ))

  GameUpdate
  (update-game [this game]
    game)

  Passable
  (passable? [_ _] true)
  (pass [_ game] game))

(def messages
  {:buy map->BuyResourcesMessage})
