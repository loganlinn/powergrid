(ns powergrid.messages.phase3
  (:require [powergrid.message :refer [Validated GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]
            [powergrid.util :refer [kw]]))


(defrecord BuyResourcesMessage [player-id resources]
  Validated
  (validate [this game])
  GameUpdate
  (update-game [this game] game))

(def messages
  {:buy map->BuyResourcesMessage})
