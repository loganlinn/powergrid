(ns powergrid.messages.phase5
  (:require [powergrid.message :as msg]
            [powergrid.common.protocols :as pc]
            [powergrid.util.error :refer [fail failf]]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.cities :as c]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.resource :as r]))

(def payout-values [10 22 33 44 54 64 73 82 90 98 105 112 118 124 129 134 138 142 145 148 150])

(defn payout
  "Returns the payout amount to receive from powering num-powered cities"
  [num-powered]
  {:pre [(not (neg? num-powered))]}
  (get payout-values (min num-powered (dec (count payout-values)))))

(defn validate-sale
  "Returns true of the plant-id and resource amt combo is valid.
  Does not validate the user owns the power-plants & resources"
  [[plant-id resources]]
  (if-not (map? resources)
    (fail "Invalid resources")
    (when-let [plant (pp/plant plant-id)]
      (let [total (reduce (fnil + 0) 0 (vals resources))]
        (if (pp/consumes-resources? plant)
          (cond
            (not= (pp/capacity plant) total) (fail "Plant capacity mismatch")

            (not (every? (partial pp/accepts-resource? plant) (keys resources)))
            (fail "Invalid resources")

            (some neg? (vals resources)) (fail "Invalid resource amount"))
          (when-not (zero? total)
            (failf "Plant %d does not consume resources" plant-id)))))))

(defn can-sell?
  "Returns true if the user owns the power plant and has the valid amount of
  resources"
  [player [plant-id resources]]
  (when player
    (when-let [plant (pp/plant plant-id)]
      (and (p/owns-power-plant? player plant-id)
           (or (not (pp/consumes-resources? plant))
               (p/can-power-plant? player plant-id))))))

(defn consume-resource
  "Returns game after returning n-units or resource from player-id's plant-id to
  resource supply"
  [game player-id plant-id resource n]
  (-> game
      (g/update-player player-id pc/send-resource
                       [plant-id resource] n)
      (g/update-resource resource pc/accept-resource
                         :supply n)))

(defn flatten-sale
  "Returns sequence of [plant-id resource amt] by flattening sale (nested maps)"
  [powered-plants]
  (mapcat #(for [resources (val %)] (cons (key %) resources)) powered-plants))

(defn consume-resources
  [game player-id powered-plants]
  (reduce
    (fn [game [plant-id resource n]]
      (consume-resource game player-id plant-id resource n))
    game
    (flatten-sale powered-plants)))

(defn total-yield
  "Returns number of cities that can be powered from operating power-plants"
  [plant-ids]
  (apply + (map (comp pp/yield pp/plant) plant-ids)))

(defn total-payout
  [game player-id powered-plants]
  (let [yield (total-yield (keys powered-plants))
        net-size (c/network-size (g/cities game) player-id)]
    (payout (min yield net-size))))

;; powered-plants {plant-id {resource amt}}
(defrecord PowerCitiesMessage [player-id powered-plants]
  pc/Labeled
  (label [this game]
    (let [player-label (pc/label (g/player game player-id))]
     (if (msg/is-pass? this)
      (format "%s passes on powering cities." player-label)
      (format "%s powers %d %s, earns $%d."
              player-label
              (count powered-plants)
              (if (= 1 (count powered-plants)) "city" "cities")
              (total-payout game player-id powered-plants)))))

  msg/Message
  (turn? [_] true)
  (passable? [_ _] true)
  (update-pass [_ game logger]
    (-> game (g/transfer-money :to player-id (payout 0))))

  (validate [this game]
    (or
      (cond
        (not (and (map? powered-plants) (every? map? (vals powered-plants))))
        (fail "Invalid message")

        (not (every? (partial can-sell? (g/player game player-id)) powered-plants))
        (fail "Invalid sale"))
      (some validate-sale powered-plants)
      game))

  (update-game [this game logger]
    (-> game
        (consume-resources player-id powered-plants)
        (g/transfer-money :to player-id
                          (total-payout game player-id powered-plants)))))

(def messages
  {:sell map->PowerCitiesMessage})
