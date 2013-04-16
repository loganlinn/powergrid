(ns powergrid.messages.phase2
  (:require [powergrid.message :refer [Validated GameUpdate Passable]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.auction :as a]
            [powergrid.power-plants :as pp]
            [powergrid.resource :as r]))

(defn purchase-power-plant
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
        (-> game
            (g/init-power-plant-auction plant player-id amt))
        (-> game
            (purchase-power-plant plant player-id amt)))))
  Passable
  (passable? [this game] (not= (g/current-round 1)))
  (pass [this game]
    (g/remove-turn game player-id)))

;; auctions always end on a pass

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
            (g/remove-turn (:player auction))
            (purchase-power-plant (pp/plant plant-id)
                                  (:player auction)
                                  (:price auction))
            (g/cleanup-auction))
        (g/set-auction game auction))
      game)))


(def messages
  {:buy map->BuyPowerPlantMessage
   :bid map->BidPowerPlantMessage})
