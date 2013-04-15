(ns powergrid.messages.phase3
  (:require [powergrid.message :refer [Validated GameUpdate passible?]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]
            [powergrid.util :refer [kw]]))


(defrecord BuyResourcesMessage [player-id resources]
  Validated
  (validate [this game] true)
  GameUpdate
  (update-game [this game] game))

(defmethod passible? BuyResourcesMessage [_ _] true)

(def messages
  {:buy map->BuyResourcesMessage})
