(ns powergrid.domain.phase2
  (:require [powergrid.domain.game :as g]
            [powergrid.domain.power-plants :as pp]
            [powergrid.domain.auction :as a]
            [powergrid.domain.protocols :refer [Labeled label]]
            [powergrid.domain.messages :as msg]
            [clojure.string :as str]))

(defrecord BidPowerPlantMessage [player-id plant-id bid]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
      (if (msg/is-pass? this)
        (if-let [auction (g/auction game)]
          (str player-label " passes bidding on " (label (a/item auction)) ".")
          (str player-label " passes on power plants."))
        (str player-label " bids " bid " on " (label (pp/plant plant-id)) ".")))))

(defrecord DiscardPowerPlantMessage [player-id plant-id]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))
          pp-label (label (pp/plant plant-id))]
      (str player-label " discarded " pp-label))))

