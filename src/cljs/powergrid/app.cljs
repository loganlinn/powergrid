(ns powergrid.app
  (:require [powergrid.common.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.cities :as c]
            [powergrid.common.auction :as a]
            [powergrid.common.resource :as r]
            [powergrid.common.power-plants :as pp]
            [powergrid.templates :as templates]
            [powergrid.util.log :refer [debug info]]
            [powergrid.country :as country]
            [powergrid.country.usa]))

(defrecord App [input game player-id])

(defn create-app [] (->App [] nil nil))

(defn accept-message
  [app msg]
  (debug :accept-message app msg)
  (update-in app [:input] conj msg))

(defmulti handle-message (fn [app msg-type msg] msg-type))

(defmethod handle-message :default
  [app msg-type msg]
  (info :unknown-message {msg-type msg})
  app)

(defmethod handle-message :game
  [app _ game]
  (assoc app :game game))

(defmethod handle-message :player-id
  [app _ player-id]
  (assoc app :player-id player-id))

(defn handle-messages
  [app messages]
  (reduce
    (fn [app [msg-type msg]]
      (handle-message app msg-type msg))
    app messages))
