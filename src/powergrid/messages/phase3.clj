(ns powergrid.messages.phase3
  (:require [powergrid.message :refer [Validated GameUpdate Passable]]
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
      (g/purchase player-id (resource-price game resource n))))

(defrecord BuyResourcesMessage [player-id resources]
  Validated
  (validate [this game]
    (let [player (g/player player-id)]
      (cond
        (empty? resources) "Invalid resources specified"
        (every? r/types (keys resources)) "Invalid resources specified"
        (every? (not neg?) (vals resources)) "Invalid resource amount"
        (= player-id (g/current-turn game)) "Not your turn"
        (not (p/has-capacity? player resources)) "Insufficient power-plant capacity"
        (not (g/contains-resource? game resources)) "Insufficient resources in market"
        (p/can-afford? player (total-price game resources)) "Insufficient funds")))

  GameUpdate
  (update-game [this game]
    (reduce (fn [game [r n]] (buy-resource game player-id r n))
            game
            resources))

  Passable
  (passable? [_ _] true)
  (pass [_ game] game))

(def messages
  {:buy map->BuyResourcesMessage})
