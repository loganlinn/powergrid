(ns powergrid.message
  (:require [powergrid.game :as g]
            [slingshot.slingshot :refer [throw+]]))

(def topic :topic)
(def title :title)

(defprotocol Message
  (turn? [this] "Returns true if turns should be advanced after hanlding message")
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
  (pass [_ game] game)
  (turn? [_] false))

(defn base-validate
  "Default set of validation rules. Returns error message if fails validation,
  otherwise nil"
  [{:keys [player-id] :as msg} game]
  (cond
    (not (g/player player-id)) "Invalid player"
    (= player-id (g/current-turn game)) "Not your turn"))

(defrecord ValidationError [message])

(defn- apply-message*
  [f msg game]
  (cond-> (f msg game)
    (turn? msg) g/advance-turns))

(defn apply-message
  "Returns game after applying message. Throws exception if message fails validation"
  [game msg]
  {:pre [(satisfies? Message msg)]}
  (if (and (passable? msg game) (pass? msg))
    (apply-message* pass msg game)
    (if-let [err (or (base-validate msg game) (validate msg game))]
      (throw+ (->ValidationError err))
      (apply-message* update-game msg game))))

