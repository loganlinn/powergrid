(ns ^:shared powergrid.domain.phase4
  (:require [powergrid.common.game :as g]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.protocols :refer [Labeled label]]
            [powergrid.domain.messages :as msg]
            [clojure.string :as str]))

(defrecord BuyCitiesMessage [player-id new-cities]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
     (if (msg/is-pass? this)
      (format "%s passes on building cities." player-label)
      (format "%s built in %s." player-label (->> new-cities (map name) (str/join ", ")))))))
