(ns ^:shared powergrid.domain.phase3
  (:require [powergrid.domain.game :as g]
            [powergrid.domain.power-plants :as pp]
            [powergrid.domain.protocols :refer [Labeled label]]
            [powergrid.domain.messages :as msg]
            [clojure.string :as str]))

(defn- label-resources [resources]
  (str/join ", " (for [[r amt] resources] (str amt " " (name r)))))

(defrecord BuyResourcesMessage [player-id resources]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
      (if (msg/is-pass? this)
        (format "%s passes on buying resources." player-label)
        (let [rlabels (map #(str (val %) " " (name (key %))) resources)]
          (format "%s buys %s." player-label (str/join ", " rlabels)))))))
