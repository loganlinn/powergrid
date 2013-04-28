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

(extend-type clojure.lang.IPersistentMap
  Validated
  (validate [_ _] nil)

  Passable
  (passable? [_ _] false)
  (pass [_ game] game)

  GameUpdate
  (update-game [_ game] game))

(defn base-validate
  [{:keys [player-id] :as msg} game]
  ;; TODO ensure msg is authorized (check users match)
  (cond
    (not (g/player player-id)) "Invalid player"
    (= player-id (g/current-turn game)) "Not your turn"))

(defn throw-validation-errors
  [msg game]
  (when-let [error (or (base-validate msg game) (validate msg game))]
    (throw+ (->ValidateError error))))
