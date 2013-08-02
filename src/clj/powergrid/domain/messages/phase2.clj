(ns ^:shared powergrid.domain.messages.phase2
  (:require [powergrid.common.protocols :refer [label]]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.game :as g]
            [powergrid.common.auction :as a]
            [powergrid.domain.messages :as msg]
            ))

(defrecord BidPowerPlantMessage [player-id plant-id bid]
  powergrid.common.protocols/Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
      (if (msg/is-pass? this)
        (if-let [auction (g/auction game)]
          (format "%s passes bidding on %s." player-label (label (a/item auction)))
          (format "%s passes on power plants." player-label))
        (format "%s bids %d on %s." player-label bid (label (pp/plant plant-id)))))))
