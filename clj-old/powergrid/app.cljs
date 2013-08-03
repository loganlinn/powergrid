(ns powergrid.app
  (:require [powergrid.common.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.cities :as c]
            [powergrid.common.auction :as a]
            [powergrid.common.resource :as r]
            [powergrid.common.power-plants :as pp]
            [powergrid.util.log :refer [debug info error spy]]))

(defrecord App [game player-id])

(defn create-app [] (->App nil nil))

(defn handle-message
  [app [msg-type msg]]
  (case msg-type
    :game (assoc app :game msg)
    :player-id (assoc app :player-id msg)
    (do
      (.debug js/console "Unknown msg: " (pr-str {msg-type msg}))
      app)))

(defn handle-messages
  [app msgs]
  (reduce handle-message app msgs))
