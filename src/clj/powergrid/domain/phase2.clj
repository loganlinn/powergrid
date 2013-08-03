(ns ^:shared powergrid.domain.phase2
  (:require [powergrid.common.game :as g]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.auction :as a]
            [powergrid.common.protocols :refer [Labeled label]]
            [powergrid.domain.messages :as msg]
            [clojure.string :as str]))

(defrecord BidPowerPlantMessage [player-id plant-id bid]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
      (if (msg/is-pass? this)
        (if-let [auction (g/auction game)]
          (format "%s passes bidding on %s." player-label (label (a/item auction)))
          (format "%s passes on power plants." player-label))
        (format "%s bids %d on %s." player-label bid (label (pp/plant plant-id)))))))

(defrecord DiscardPowerPlantMessage [player-id plant-id]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))
          pp-label (label (pp/plant plant-id))]
      (format "%s discarded %s"))))

