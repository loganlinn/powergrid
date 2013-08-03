(ns powergrid.messages.phase3
  (:require [powergrid.domain.phase3]
            [powergrid.message :as msg]
            [powergrid.domain.protocols :as pc]
            [powergrid.util.error :refer [fail]]
            [powergrid.game :as g]
            [powergrid.domain.player :as p]
            [powergrid.domain.resource :as r])
  (:import [powergrid.domain.phase3 BuyResourcesMessage]))

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
      (g/update-resource resource pc/send-resource :market n)
      (g/update-player player-id pc/accept-resource resource n)
      (g/transfer-money :from player-id (resource-price game resource n))))

(extend-type BuyResourcesMessage
  msg/Message
  (turn? [_] true)
  (passable? [_ _] true)
  (update-pass [_ game logger] game)

  (validate [{:keys [player-id resources]} game]
    (let [player (g/player game player-id)]
      (cond
        (or (not (map? resources)) (empty? resources))
        (fail "Invalid resources")

        (not (every? r/types (keys resources)))
        (fail "Unknown resource(s) specified")

        (some neg? (vals resources)) (fail "Invalid resource amount")

        (not (p/has-capacity? player resources))
        (fail "Insufficient power-plant capacity")

        (not (g/contains-resources? game resources))
        (fail "Insufficient resources in market")

        (not (p/can-afford? player (total-price game resources)))
        (fail "Insufficient funds")

        :else game)))

  (update-game [{:keys [player-id resources]} game logger]
    (reduce (fn [game [r n]] (buy-resource game player-id r n))
            game
            resources)))

(def messages
  {:buy powergrid.domain.phase3/map->BuyResourcesMessage})
