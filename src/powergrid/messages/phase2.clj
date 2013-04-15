(ns powergrid.messages.phase2
  (:require [powergrid.message :refer [Validated GameUpdate passable?]]
            [powergrid.game :as g]
            [powergrid.player :as p]
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
    (let [player (g/player player-id)
          plant (pp/plant plant-id)]
      (if (g/auction-needed? game)
        (-> game
            (g/init-power-plant-auction))
        (-> game
            (purchase-power-plant plant player amt)
            )))))

(defmethod passable? BuyPowerPlantMessage
  [game _]
  (not= (g/current-round 1)))

(defrecord BidPowerPlantMessage [player-id plant-id bid]
  Validated
  (validate [this game] true)
  GameUpdate
  (update-game [this game] game
    ))

(defmethod passable? BidPowerPlantMessage [_ _] true)

(def messages
  {:buy map->BuyPowerPlantMessage
   :bid map->BidPowerPlantMessage})
