(ns powergrid.messages.phase3
  (:require [powergrid.message :refer [Message]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(defn resource-price
  "Returns the current price for n-units of resource"
  [game resource n]
  (-> (g/resource game resource) (r/resource-price n)))

(defn total-price
  [game resources]
  (reduce (fn [tot [r n]]
            (+ tot (resource-price game r n)))
          0
          resources))

(defn buy-resource
  "Updates and returns game after buying n-units of resource"
  [game player-id resource n]
  (-> game
      (g/update-resource resource r/send-resource :market n)
      (g/update-player player-id r/accept-resource resource n)
      (g/transfer-money :from player-id (resource-price game resource n))))

(defrecord BuyResourcesMessage [player-id resources]
  Message
  (turn? [_] true)
  (passable? [_ _] true)
  (update-pass [_ game] game)

  (validate [this game]
    (let [player (g/player player-id)]
      (cond
        (or (not (map? resources))
            (empty? resources)) "Invalid resources"
        (not (every? r/types (keys resources))) "Unknown resource(s) specified"
        (some neg? (vals resources)) "Invalid resource amount"
        (= player-id (g/current-turn game)) "Not your turn"
        (not (p/has-capacity? player resources)) "Insufficient power-plant capacity"
        (not (g/contains-resource? game resources)) "Insufficient resources in market"
        (p/can-afford? player (total-price game resources)) "Insufficient funds")))

  (update-game [this game]
    (reduce (fn [game [r n]] (buy-resource game player-id r n))
            game
            resources)))

(def messages
  {:buy map->BuyResourcesMessage})
