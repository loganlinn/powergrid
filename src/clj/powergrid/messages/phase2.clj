(ns powergrid.messages.phase2
  (:require [powergrid.message :refer [Message]]
            [powergrid.util.error :refer [fail]]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.auction :as a]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.resource :as r]))

(defn new-auction
  "Returns new auction new auction for power-plant"
  [game plant-id]
  (let [plant (pp/plant plant-id)]
    (a/new-auction
      {:item plant :bidders (g/turns game)}
      (pp/min-price plant))))

(defn auction
  "Returns current auction if it exists, otherwise a new auction"
  [game plant-id]
  (or (g/auction game)
      (new-auction game plant-id)))

(defn complete-auction
  "Updates and returns game for a finshed auction"
  [game {:keys [item price player-id]}]
  (-> game
      (g/remove-power-plant item :market)
      (g/draw-power-plant)
      (g/update-player player-id p/add-power-plant (pp/id item))
      (g/transfer-money :from player-id price)
      (g/remove-turn player-id)
      (g/cleanup-auction)))

(defrecord BidPowerPlantMessage [player-id plant-id bid]
  Message
  (turn? [_] false)

  (passable? [_ game]
    (or (g/has-auction? game)
        (not= (g/current-round game) 1)))
  (update-pass [_ game]
    (if-let [auction (a/pass (g/auction game))]
      (if (a/completed? auction)
        (complete-auction game auction)
        (g/set-auction game auction))
      game))

  (validate [_ game]
    (let [auction (g/auction game)
          plant (pp/plant plant-id)]
      (cond
        (not bid) (fail "Invalid bid")
        (not plant) (fail "Unknown plant")

        (not (g/power-plant-buyable? game plant))
        (fail "Cannot purchase that power-plant")

        (not= player-id (if auction (a/current-bidder auction)
                          (g/current-turn game)))
        (fail "Not your bid")

        (not (p/can-afford? (g/player game player-id) bid))
        (fail "Insufficient funds")

        (if auction (< bid (a/min-bid auction)))
        (fail (str "Minimum bid is " (a/min-bid auction)))

        (if (not auction) (< bid plant-id))
        (fail (str "Minimum bid is " plant-id))

        :else game)))

  (update-game [_ game]
    (let [plant (pp/plant plant-id)
          auction (a/bid (auction game plant-id) player-id bid)]
      (if (a/completed? auction)
        (complete-auction game auction)
        (g/set-auction game auction)))))

(def messages
  {:bid map->BidPowerPlantMessage})
