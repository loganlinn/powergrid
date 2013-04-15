(ns powergrid.messages.phase3
  (:require [powergrid.message :refer [Message GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]
            [powergrid.util :refer [kw]]))


(defrecord BuyMessage [player-id resources]
  GameUpdate
  (update-game [this game] game))

(def messages
  {:buy BuyMessage
   :pass nil})
