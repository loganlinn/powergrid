(ns powergrid.messages.phase2
  (:require [powergrid.domain.phase2]
            [powergrid.message :as msg]
            [powergrid.common.protocols :refer [label]]
            [powergrid.util.error :refer [fail failf]]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.auction :as a]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.resource :as r])
  (:import [powergrid.domain.phase2 BidPowerPlantMessage DiscardPowerPlantMessage]))

(defn new-auction
  "Returns new auction new auction for power-plant"
  [game plant-id]
  (let [plant (pp/plant plant-id)]
    (a/new-auction
      {:item plant :bidders (g/turns game)}
      (pp/min-price plant))))

(defn get-or-create-auction
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

(extend-type BidPowerPlantMessage
  msg/Message
  (turn? [_] false)
  (passable? [_ game]
    (or (g/has-auction? game)
        (not= (g/current-round game) 1)))
  (update-pass [_ game logger]
    (if-let [auction (a/pass (g/auction game))]
      (if (a/completed? auction)
        (complete-auction game auction)
        (g/set-auction game auction))
      game))
  (validate [{:keys [player-id plant-id bid]} game]
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
        (failf "Minimum bid is %d" (a/min-bid auction))

        (if (not auction) (< bid plant-id))
        (failf "Minimum bid is %d" plant-id)

        :else game)))
  (update-game [{:keys [player-id plant-id bid]} game logger]
    (let [plant (pp/plant plant-id)
          auction (-> (get-or-create-auction game plant-id)
                      (a/bid player-id bid))]
      (if (a/completed? auction)
        (-> game
            (complete-auction auction)
            (logger (format "Auction complete. %s bought %s for $%d"
                            (label (g/player game (a/player-id auction)))
                            (label (a/item auction))
                            (a/price auction))))
        (g/set-auction game auction)))))

;; =========

(extend-type DiscardPowerPlantMessage
  msg/Message
  (turn? [_] false)
  (passable? [_ game] false)
  (validate [{:keys [player-id plant-id]} game]
    (let [plant (pp/plant plant-id)
          player (g/player game player-id)]
      (cond
        (not plant) (fail "Invalid power plant")
        (not player) (fail "Invalid player")
        (not (p/owns-power-plant? player plant-id)) (fail "Invalid power plant")
        :else game)))
  (update-game [{:keys [player-id plant-id]} game logger]
    (g/update-player game player-id p/remove-power-plant plant-id)))

;; =========

(def messages
  {:bid powergrid.domain.phase2/map->BidPowerPlantMessage
   :discard powergrid.domain.phase2/map->DiscardPowerPlantMessage})
