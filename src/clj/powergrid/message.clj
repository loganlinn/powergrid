(ns powergrid.message
  (:refer-clojure :exclude [type])
  (:require [powergrid.domain.messages]
            [potemkin :refer [import-vars]]
            [powergrid.game :as g]
            [powergrid.domain.protocols :as pc]
            [powergrid.util.error :refer [fail failf error-m]]
            [powergrid.util.log :refer [debugf]]
            [clojure.algo.monads :refer [with-monad m-chain]]))

(import-vars
  [powergrid.domain.messages
   topic
   type
   pass
   is-pass?])

(defprotocol Message
  (turn? [this] "Returns true if turns should be advanced after hanlding message")
  (passable? [this game] "Returns true if passing is allowed, otherwise false")
  (update-pass [this game logger] "Returns game from passing message. Invoked when passable? is truthy")
  (validate [this game] "Validates non-pass messages. Retruns Failure if message is invalid, otherwise game")
  (update-game [this game logger] "Returns game after applying valid message. Invoked when passable? is falsey"))

(defn expected-topic [game]
  (case (int (g/current-phase game))
    2 :phase2
    3 :phase3
    4 :phase4
    5 :phase5))

(defn- invalid-topic? [msg game]
  (if (not= (expected-topic game) (topic msg))
    (fail "Unexpected message (phase)")))

(defn- invalid-player? [msg game]
  (if (not (g/player game (:player-id msg)))
    (fail "Invalid player")))

(defn- invalid-turn? [msg game]
  (if (and (turn? msg) (not= (:player-id msg) (g/current-turn game)))
    (failf "Not your turn (waiting for %s)" (g/current-turn game))))

(defn- invalid-pass?
  "If msg is pass, returns Failure if invalid or game if valid, otherwise nil"
  [msg game]
  (if (is-pass? msg)
    (if (passable? msg game)
      game ; end validation
      (fail "Cannot pass"))))

(defn- validate-msg
  "Returns a Failure if msg is invalid, otherwise a game"
  [game msg]
  (or
    (invalid-pass? msg game)
    (invalid-topic? msg game)
    (invalid-player? msg game)
    (invalid-turn? msg game)
    (validate msg game)
    (fail "Failed to validate message")))

(defn- advance-turns
  "Advance game's turns if msg is a turn"
  [game msg]
  (if (turn? msg) (g/advance-turns game) game))

;; TODO pass logger to update-* (logger game "message")
(defn log-logger
  "Game event logger that logs to application log"
  [game msg]
  (debugf "[%s@%d,%d] %s"
          (:id game)
          (g/current-phase game)
          (g/current-step game)
          msg)
  game)

(defn nil-logger [_ _])

(defn apply-message
  "Attempts to advance game state with msg. Returns [game err] tuple."
  [game msg logger]
  {:pre [(satisfies? Message msg)]}
  (let [update-fn (if (is-pass? msg) update-pass update-game)
        msg-label (pc/label msg game)
        msg-apply (with-monad error-m
                    (m-chain [#(validate-msg % msg)
                              #(update-fn msg % logger)
                              #(advance-turns % msg)]))
        result (msg-apply game)]
    (logger game msg-label)
    result))

