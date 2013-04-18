(ns powergrid.messages.phase2
  (:require [powergrid.message :refer [Validated GameUpdate Passable]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.auction :as a]
            [powergrid.power-plants :as pp]
            [powergrid.resource :as r]))

(defn- new-auction
  [game plant]
  (a/new-auction
    {:item plant :bidders (g/turns game)}
    (pp/min-price plant)))

(defrecord BidPowerPlantMessage [player-id plant-id bid]
  Validated
  (validate [_ game]
    (let [auction (g/current-auction game)
          plant (pp/plant plant-id)]
      (cond
        (not plant) "Unknown plant"
        (not (g/power-plant-buyable? game plant)) "Cannot purchase that power-plant"
        (not= player-id (a/current-bidder auction)) "Not your bid"
        (or (g/has-auction? game) (= player-id (g/current-turn game))) "Not your bid"
        (not (p/can-afford? (g/player player-id) bid)) "Insufficient funds"
        (< bid (a/min-bid auction)) (str "Minimum bid is " (a/min-bid auction)))))

  Passable
  (passable? [_ game]
    (or (g/has-auction? game)
        (not= (g/current-round game) 1)))
  (pass [_ game]
    (if-let [auction (a/pass (g/current-auction game))]
      (if (a/completed? auction)
        (-> game
            (g/remove-power-plant :market power-plant)
            (g/update-player player-id p/add-power-plant power-plant)
            (g/purchase player-id price)
            (g/remove-turn player-id)
            (g/cleanup-auction))
        (g/set-auction game auction))
      (g/remove-turn game player-id)))

  GameUpdate
  (update-game [_ game]
    (let [plant (pp/plant plant-id)
          auction (-> (or (g/current-auction game)
                          (new-auction game plant))
                      (a/bid player-id bid))]
      (if (a/completed? auction)
        (finalize-auction game auction)
        (g/set-auction game auction)))))

(def messages
  {:buy map->BuyPowerPlantMessage
   :bid map->BidPowerPlantMessage})
