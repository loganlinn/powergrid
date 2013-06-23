(ns powergrid.message
  (:refer-clojure :exclude [type])
  (:require [powergrid.game :as g]
            [powergrid.util.error :refer [fail error-m]]
            [clojure.algo.monads :refer [with-monad m-chain]]
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
  [{:keys [player-id] :as msg} game]
  (cond
    (not (g/player game player-id)) (fail "Invalid player")
    (or (nil? (expected-topic game))
        (not= (topic msg) (expected-topic game))) (fail "Unexpected message (phase)")
    (and (turn? msg) (not= player-id (g/current-turn game))) (fail "Not your turn")
    :else game))

(defn- validate-msg
  [msg game]
  (if (is-pass? msg)
    (if (passable? msg game)
      game
      (fail "Cannot pass"))
    (base-validate msg game)))

(defn- advance-turns
  [msg game]
  (if (turn? msg)
    (g/advance-turns game)
    game))

(defn apply-message
  "Attempts to advance game state with msg. Returns [game err] tuple."
  [game msg]
  {:pre [(satisfies? Message msg)]}
  (let [update-fn (if (is-pass? msg) update-pass update-game)
        msg-apply (with-monad error-m
                    (m-chain [(partial validate-msg msg)
                              (partial update-fn msg)
                              (partial advance-turns msg)]))]
    (msg-apply game)))

