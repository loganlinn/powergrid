(ns ^:shared powergrid.domain.messages
  (:require [powergrid.domain.protocols :refer [Labeled label]]
            [powergrid.domain.power-plants :as pp]
            [powergrid.domain.game :as g]
            [powergrid.domain.auction :as a]
            [powergrid.domain.cities :as c]
            [clojure.string :as str])
  (:refer-clojure :exclude [type]))

(def topic :topic)
(def type :type)
(def pass ::pass)
(defn is-pass? [msg] (pass msg))
