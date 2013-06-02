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

(defn validate-msg
  "Default set of validation rules. Returns error message if fails validation,
  otherwise nil"
  [{:keys [player-id] :as msg} game]
  (if (is-pass? msg)
    (when-not (passable? msg game)
      "Cannot pass")
    (cond
      ;; general msg validation
      (not (g/player game player-id)) "Invalid player"
      (or (nil? (expected-topic game))
          (not= (topic msg) (expected-topic game))) "Unexpected message (phase)"
      (and (turn? msg) (not= player-id (g/current-turn game))) (str "Not your turn" player-id)
      ;; msg specific validation
      :else (validate msg game))))

(defn apply-message
  "Attempts to advance game state with msg. Returns [game err] tuple."
  [game msg]
  {:pre [(satisfies? Message msg)]}
  (if-let [err (validate-msg msg game)]
    [game err]
    [(cond->> game
       (is-pass? msg) (update-pass msg)
       (not (is-pass? msg)) (update-game msg)
       (turn? msg) g/advance-turns)
     nil]))

