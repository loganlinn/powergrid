(ns powergrid.messages.phase4
  (:require [powergrid.domain.phase4]
            [powergrid.message :as msg]
            [powergrid.util.error :refer [fail]]
            [powergrid.game :as g]
            [powergrid.cities :as c]
            [powergrid.domain.player :as p]
            [powergrid.domain.resource :as r])
  (:import [powergrid.domain.phase4 BuyCitiesMessage]))

(defn can-build-city?
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
  (let [player (g/player game player-id)
        cost (purchase-cost game player-id new-cities)]
    (p/can-afford? player cost)))

(defn own-cities
  "Returns game after adding player-id as owner to all new-cities"
  [game player-id new-cities]
  (reduce #(g/update-cities %1 c/add-owner player-id %2) game new-cities))

(extend-type BuyCitiesMessage
  msg/Message
  (turn? [_] true)
  (passable? [_ _] true)
  (update-pass [_ game logger] game)
  (validate [{:keys [player-id new-cities]} game]
    (cond
      (not (coll? new-cities)) (fail "Invalid purchase")

      (not (every? (partial can-build-city? game player-id) new-cities))
      (fail "Invalid city")

      (not (can-afford-cities? game player-id new-cities))
      (fail "Insufficient funds")

      :else game))

  (update-game [{:keys [player-id new-cities]} game logger]
    (let [cost (purchase-cost game player-id new-cities)]
      (-> game
          (own-cities player-id new-cities)
          (g/transfer-money :from player-id cost)))))

(def messages
  {:buy powergrid.domain.phase4/map->BuyCitiesMessage})
