(ns powergrid.messages.phase4
  (:require [powergrid.message :refer [Message]]
            [powergrid.game :as g]
            [powergrid.cities :as c]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(defn valid-city?
  "Returns true if player can build in city, otherwise false"
  [game player-id city]
  (let [cities (g/cities game)
        step (g/current-step game)]
    (and (c/valid-city? cities city)
         (c/buildable-city? cities city player-id step))))

(defn purchase-cost
  "Returns the cost for player to purchase new-cities"
  [game player-id new-cities]
  (c/purchase-cost (g/cities game) player-id new-cities))

(defn can-afford-cities?
  "Returns true if player can afford to purchase connections to new-cities.
  Cost is calculated by purchasing cheapest connection to each city in new-cities
  sequentially"
  [game player-id new-cities]
  (let [player (g/player player-id)
        cost (purchase-cost game player-id new-cities)]
    (p/can-afford? player cost)))

(defn own-cities
  "Returns game after adding player-id as owner to all new-cities"
  [game player-id new-cities]
  (reduce #(g/update-cities %1 c/add-owner player-id %2) game new-cities))

(defrecord BuyCitiesMessage [player-id new-cities]
  Message
  (turn? [_] true)
  (validate [this game]
    (cond
      (not (every? (partial valid-city? game player-id) new-cities)) "Invalid city"
      (not (can-afford-cities? game player-id new-cities)) "Insufficient funds"))

  (update-game [this game]
    (let [cost (purchase-cost game player-id new-cities)]
      (-> game
          (own-cities player-id new-cities)
          (g/transfer-money :from player-id cost))))

  (passable? [_ _] true)
  (update-pass [_ game] game))

(def messages
  {:buy map->BuyCitiesMessage})
