(ns powergrid.domain.phase4
  (:require [powergrid.domain.game :as g]
            [powergrid.domain.power-plants :as pp]
            [powergrid.domain.protocols :refer [Labeled label]]
            [powergrid.domain.messages :as msg]
            [clojure.string :as str]))

(defrecord BuyCitiesMessage [player-id new-cities]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
     (if (msg/is-pass? this)
      (str player-label " passes on building cities.")
      (str player-label " built in " (->> new-cities (map name) (str/join ", ")) ".")))))
