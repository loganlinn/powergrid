(ns powergrid.message
  (:require [powergrid.game :as g]
            [slingshot.slingshot :refer [throw+]]))

(def topic :topic)
(def title :title)

(defrecord ValidateError [msg silent?])

(defprotocol Validated
  (validate [msg game] "Retruns string of validation message if msg is invalid, otherwise nil"))

(defprotocol GameUpdate
  (update-game [update game]))

(defprotocol Passable
  (passable? [this game] "Returns true if passing is allowed")
  (pass [this game] "Returns (modified) game from passing msg"))

(extend-protocol Passable
  clojure.lang.IPersistentMap
  (passable? [_ _] false)
  (pass [_ game] game))

(defn base-validate
  [{:keys [player-id] :as msg} game]
  ;; TODO ensure msg is authorized (check users match)
  (cond
    (not (g/player player-id)) "Invalid player"))

(defn throw-validation-errors
  [msg game]
  (when-let [error (or (base-validate msg game) (validate msg game))]
    (throw+ (->ValidateError error))))
