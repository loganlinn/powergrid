(ns powergrid.messages.phase5
  (:require [powergrid.message :refer [Validated GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.cities :as c]
            [powergrid.power-plants :as pp]
            [powergrid.resource :as r]))

(def payout-values [10 22 33 44 54 64 73 82 90 98 105 112 118 124 129 134 138 142 145 148 150])

(defn payout
  "Returns the payout amount to receive from powering num-powered cities"
  [num-powered]
  {:pre [(not (neg? num-powered))]}
  (if (> num-powered (dec (count payout-values)))
    (last payout-values)
    (get payout-values num-powered)))

(defn valid-sale?
  "Returns true of the plant-id and resource amt combo is valid.
  Does not validate the user owns the power-plants & resources"
  [[plant-id resources]]
  {:pre [(map? resources)]}
  (when-let [plant (pp/plant plant-id)]
    (let [total (reduce (fnil + 0) 0 (vals resources))]
      (if (pp/consumes-resources? plant)
        (and (= (pp/capacity plant) total)
             (every? (partial pp/accepts-resource? plant) (keys resources))
             (every? (complement neg?) (vals resources)))
        (zero? total)))))

(defn can-sell?
  "Returns true if the user owns the power plant and has the valid amount of
  resources"
  [player-id [plant-id resources]]
  (when-let [player (g/player player-id)]
    (when-let [plant (pp/plant plant-id)]
      (and (p/owns-power-plant? player plant)
           (or (not (pp/consumes-resources? plant))
               (p/can-power-plant? player plant))))))

(defn consume-resource
  "Returns game after returning n-units or resource from player-id's plant-id to
  resource supply"
  [game player-id plant-id resource n]
  (-> game
      (g/update-player player-id r/send-resource [(pp/plant plant-id) resource] n)
      (g/update-resource resource r/accept-resource :supply n)))

(defn flatten-sale
  "Returns sequence of [plant-id resource amt] by flattening sale (nested maps)"
  [powered-cities]
  (mapcat #(for [resources (val %)] (cons (key %) resources)) powered-cities))

(defn consume-resources
  [game player-id powered-cities]
  (reduce
    (fn [game [plant-id resource n]]
      (consume-resource game player-id resource n))
    game
    (flatten-sale powered-cities)))

(defn total-yield
  "Returns number of cities that can be powered from operating power-plants"
  [plant-ids]
  (apply + (map (comp pp/yield pp/plant) plant-ids)))

(defn total-payout
  [game player-id powered-cities]
  (let [yield (total-yield (keys powered-cities))
        net-size (c/network-size (g/cities game) player-id)]
    (payout (min yield net-size))))

(defrecord PowerCitiesMessage [player-id powered-cities]
  Validated
  (validate [this game]
    (cond
      (not (and (map? powered-cities)
                (every? map? (vals powered-cities)))) "Invalid message"
      (every? valid-sale? powered-cities) "Invalid sale"
      (every? (partial can-sell? player-id) powered-cities) "Invalid sale"))

  GameUpdate
  (update-game [this game]
    (-> game
        (consume-resources player-id powered-cities)
        (g/transfer-money :to player-id (total-payout game player-id powered-cities)))))

(def messages
  {:sell map->PowerCitiesMessage})
