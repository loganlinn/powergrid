(ns powergrid.messages.phase2
  (:require [powergrid.message :refer [Validated GameUpdate Passable]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.auction :as a]
            [powergrid.power-plants :as pp]
            [powergrid.resource :as r]))

(defn purchase-power-plant
  "Returns game after performing the necssary tasks for player-id to purchase
  power-plant. Asserts that power-plant is in the market."
  [game power-plant player-id amt]
  {:pre [(g/power-plant-buyable? game power-plant)]}
  (-> game
      (g/remove-power-plant :market power-plant)
      (g/update-player player-id p/add-power-plant power-plant)
      (g/purchase player-id amt)))

(defrecord BuyPowerPlantMessage [player-id plant-id amt]
  Validated
  (validate [this game]
    (let [plant (pp/plant plant-id)]
      (cond
        (not plant) "Unknown power-plant"
        (not (g/power-plant-buyable? game plant)) "Cannot purchase that power-plant"
        (< amt (pp/min-price plant)) (str "Minimum price is " (pp/min-price plant))
        (not (p/can-afford? (g/player player-id) amt)) "Insufficient funds")))

  GameUpdate
  (update-game [this game]
    (let [plant (pp/plant plant-id)]
      (if (g/auction-needed? game)
        (let [auction (a/new-auction {:item plant
                                      :player-id player-id
                                      :price amt
                                      :bidders (remove #{player-id} (g/turns game))})]
          (-> game
              (g/set-auction auction)
              (g/expect-message BidPowerPlantMessage
                                (a/current-bidder auction)
                                {:plant-id plant-id})))
        (-> game
            (purchase-power-plant plant player-id amt)
            (g/advance-turns)))))

  Passable
  (passable? [this game] (not= (g/current-round 1)))
  (pass [this game]
    (g/remove-turn game player-id)))

(defrecord BidPowerPlantMessage [player-id plant-id bid]
  Validated
  (validate [this game]
    (let [plant (pp/plant plant-id)
          auction (g/current-auction game)]
      (cond
        (not plant) "Unknown plant"
        (not auction) "Invalid auction"
        (not= (auction :item) plant) "Invalid plant"
        (not= player-id (a/current-bidder auction)) "Not your bid"
        (< bid (a/min-bid auction)) (str "Minimum bid is " (a/min-bid auction)))))

  GameUpdate
  (update-game [this game]
    game)

  Passable
  (passable? [_ _] true)
  (pass [this game]
    (if-let [auction (a/pass (g/current-auction game))]
      (if (a/completed? auction)
        (-> game
            (purchase-power-plant (pp/plant plant-id)
                                  (:player auction)
                                  (:price auction))
            (g/remove-turn (:player auction))
            (g/cleanup-auction))
        (g/set-auction game auction))
      game)))

(def messages
  {:buy map->BuyPowerPlantMessage
   :bid map->BidPowerPlantMessage})
