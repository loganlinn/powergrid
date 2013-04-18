(ns powergrid.messages.phase3
  (:require [powergrid.message :refer [Validated GameUpdate Passable]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]))


(defn total-price
  [game resources]
  (apply + (for [[r n] resources]
             (-> (g/resource game r) (r/resource-price n)))))

(defrecord BuyResourcesMessage [player-id resources]
  Validated
  (validate [this game]
    (let [player (g/player player-id)]
     (cond
      (empty? resources) "Invalid resources specified"
      (every? r/types (keys resources)) "Invalid resources specified"
      (every? pos? (vals resources)) "Invalid resource amount"
      (= player-id (g/current-turn game)) "Not your turn"
      (not (p/has-capacity? player resources)) "Insufficient power-plant capacity"
      (not (g/contains-resource? game resources)) "Insufficient resources in market"
      (p/can-afford? player (total-price game resources)) "Insufficient funds")))

  GameUpdate
  (update-game [this game]
    ;; remove from market
    ;; send to player
    ;; deduct money from player
    game)

  Passable
  (passable? [_ _] true)
  (pass [_ game] game))

(def messages
  {:buy map->BuyResourcesMessage})
