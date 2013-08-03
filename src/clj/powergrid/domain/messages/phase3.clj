(ns ^:shared powergrid.domain.messages.phase3
  (:require [powergrid.common.protocols :refer [Labeled label]]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.game :as g]
            [powergrid.common.auction :as a]
            [powergrid.domain.messages :as msg]
            ))

(defn- label-resources [resources]
  (clojure.string/join ", " (for [[r amt] resources]
                              (str amt " " (name r)))))

(defrecord BuyResourcesMessage [player-id resources]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
     (if (msg/is-pass? this)
      (format "%s passes on buying resources." player-label)
      (format "%s buys %s." player-label (label-resources resources))))))
