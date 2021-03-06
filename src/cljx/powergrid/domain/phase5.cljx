(ns powergrid.domain.phase5
  (:require [powergrid.domain.game :as g]
            [powergrid.domain.cities :as c]
            [powergrid.domain.power-plants :as pp]
            [powergrid.domain.protocols :refer [Labeled label]]
            [powergrid.domain.messages :as msg]
            [clojure.string :as str]))

(def payout-values [10 22 33 44 54 64 73 82 90 98 105 112 118 124 129 134 138 142 145 148 150])

(defn payout
  "Returns the payout amount to receive from powering num-powered cities"
  [num-powered]
  (get payout-values (min num-powered (dec (count payout-values)))))

(defn total-yield
  "Returns number of cities that can be powered from operating power-plants"
  [plant-ids]
  (apply + (map (comp pp/yield pp/plant) plant-ids)))

(defn total-payout
  [player-network-size powered-plants]
  (let [yield (total-yield (keys powered-plants))]
    (payout (min yield player-network-size))))

(defrecord PowerCitiesMessage [player-id powered-plants]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))
          player-network-size (c/network-size (g/cities game) player-id)]
      (if (msg/is-pass? this)
        (str player-label " passes on powering cities.")
        (str player-label " powers " (count powered-plants) " "
             (if (= 1 (count powered-plants)) "city" "cities")
             ", earns $"
             (total-payout player-network-size powered-plants)
             ".")))))
