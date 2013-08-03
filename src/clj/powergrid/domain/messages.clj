(ns ^:shared powergrid.domain.messages
  (:refer-clojure :exclude [type]))

(def topic :topic)
(def type :type)
(def pass ::pass)
(defn is-pass? [msg] (pass msg))

