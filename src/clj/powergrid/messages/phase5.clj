(ns powergrid.messages.phase5
  (:require [powergrid.domain.phase5 :as phase5]
            [powergrid.message :as msg]
            [powergrid.common.protocols :as pc]
            [powergrid.util.error :refer [fail failf]]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.cities :as c]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.resource :as r])
  (:import [powergrid.domain.phase5 PowerCitiesMessage]))

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


(extend-type PowerCitiesMessage
  msg/Message
  (turn? [_] true)
  (passable? [_ _] true)
  (update-pass [{:keys [player-id]} game logger]
    (-> game (g/transfer-money :to player-id (phase5/payout 0))))

  (validate [{:keys [player-id powered-plants]} game]
    (or
      (cond
        (not (and (map? powered-plants) (every? map? (vals powered-plants))))
        (fail "Invalid message")

        (not (every? (partial can-sell? (g/player game player-id)) powered-plants))
        (fail "Invalid sale"))
      (some validate-sale powered-plants)
      game))

  (update-game [{:keys [player-id powered-plants]} game logger]
    (let [player-network-size (c/network-size (g/cities game) player-id)]
     (-> game
        (consume-resources player-id powered-plants)
        (g/transfer-money :to player-id
                          (phase5/total-payout player-network-size powered-plants))))))

(def messages
  {:sell powergrid.domain.phase5/map->PowerCitiesMessage})
