(ns ^:shared powergrid.domain.phase5
  (:require [powergrid.common.power-plants :as pp]))

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
