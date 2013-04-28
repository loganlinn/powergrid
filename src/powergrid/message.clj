(ns powergrid.message
  (:require [powergrid.game :as g]
            [slingshot.slingshot :refer [throw+]]))

(def topic :topic)
(def title :title)

(defprotocol Message
  (validate [msg game] "Retruns string of validation message if msg is invalid, otherwise nil")
  (update-game [update game])
  (passable? [this game] "Returns true if passing is allowed, otherwise false")
  (pass? [this] "Returns true if this message represents a pass, otherwise false")
  (pass [this game] "Returns (modified) game from passing msg"))

(extend-protocol Message
  clojure.lang.IPersistentMap
  (validate [_ _] nil)
  (update-game [_ game] game)
  (passable? [_ _] false)
  (pass? [this] (::pass? this))
  (pass [_ game] game))

(defn base-validate
  "Default set of validation rules. Returns error message if fails validation,
  otherwise nil"
  [{:keys [player-id] :as msg} game]
  (cond
    (not (g/player player-id)) "Invalid player"
    (= player-id (g/current-turn game)) "Not your turn"))

(defn handle-error
  [msg err]
  (println "ERROR:" err msg))

(defn apply-message
  "Returns game after applying message"
  [game msg]
  {:pre [(satisfies? Message msg)]}
  (if (and (passable? msg game) (pass? msg))
    (pass msg game)
    (if-let [err (or (base-validate msg game) (validate msg game))]
      (handle-error msg err)
      (update-game msg game))))

