(ns powergrid.messages.phase2
  (:require [powergrid.message :refer [Validated GameUpdate passable?]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(defrecord BuyPowerPlantMessage [player-id plant-id amt]
  Validated
  (validate [this game] true)
  GameUpdate
  (update-game [this game] game))

(defmethod passable? BuyPowerPlantMessage
  [game _]
  (not= (g/current-round 1)))

(defrecord BidPowerPlantMessage [player-id plant-id bid]
  Validated
  (validate [this game] true)
  GameUpdate
  (update-game [this game] game))

(defmethod passable? BidPowerPlantMessage [_ _] true)

(def messages
  {:buy map->BuyPowerPlantMessage
   :bid map->BidPowerPlantMessage})
