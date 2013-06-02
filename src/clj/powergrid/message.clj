(ns powergrid.message
  (:refer-clojure :exclude [type])
  (:require [powergrid.game :as g]
            [slingshot.slingshot :refer [throw+]]))

(def topic :topic)
(def type :type)
(def pass ::pass)

(defprotocol Message
  (turn? [this] "Returns true if turns should be advanced after hanlding message")
  (passable? [this game] "Returns true if passing is allowed, otherwise false")
  (update-pass [this game] "Returns (modified) game from passing msg")
  (validate [msg game] "Retruns string of validation message if msg is invalid, otherwise nil")
  (update-game [update game]))

(extend-protocol Message
  clojure.lang.IPersistentMap
  (turn? [_] false)
  (passable? [_ _] false)
  (update-pass [_ game] game)
  (validate [_ _] nil)
  (update-game [_ game] game))

(defn is-pass? [msg] (pass msg))

(defn- expected-topic
  [game]
  (case (g/current-phase game)
    2 :phase2
    3 :phase3
    4 :phase4
    5 :phase5))

(defn base-validate
  "Default set of validation rules. Returns error message if fails validation,
  otherwise nil"
  [{:keys [player-id] :as msg} game]
  (cond
    (not (g/player game player-id)) "Invalid player"
    (or (nil? (expected-topic game))
        (not= (topic msg) (expected-topic game))) "Unexpected message (phase)"
    (and (turn? msg) (not= player-id (g/current-turn game))) (str "Not your turn" player-id)))

(defrecord ValidationError [message])

(defn- handle-turns
  "Returns game after updating turns as needed"
  [game msg]
  (if (turn? msg)
    (g/advance-turns game)
    game))

(defn apply-message
  "Returns game after applying message. Throws exception if message fails validation"
  [game msg]
  {:pre [(satisfies? Message msg)]}
  (if (and (passable? msg game) (is-pass? msg))
    (handle-turns (update-pass msg game) msg)
    (if-let [err (or (base-validate msg game) (validate msg game))]
      (do (println "ValidationError:" err)
          (throw+ (->ValidationError err)))
      (handle-turns (update-game msg game) msg))))

