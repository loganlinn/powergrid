(ns ^:shared powergrid.domain.messages
  (:require [powergrid.common.protocols :refer [Labeled label]]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.game :as g]
            [powergrid.common.auction :as a]
            [powergrid.common.cities :as c]
            [clojure.string :as str])
  (:refer-clojure :exclude [type]))

(def topic :topic)
(def type :type)
(def pass ::pass)
(defn is-pass? [msg] (pass msg))
