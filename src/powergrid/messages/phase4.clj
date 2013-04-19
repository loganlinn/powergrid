(ns powergrid.messages.phase4
  (:require [powergrid.message :refer [Validated GameUpdate Passable]]
            [powergrid.game :as g]
            [powergrid.cities :as c]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(defn connection-cost
  [connections cities]
  ;; TODO IMPLEMENT
  0)

(defn can-afford-cities?
  [game player-id cities]
  ;; TODO IMPLEMENT
  (let [price (connection-cost (g/connections game) cities)]
    (p/can-afford? (g/player player-id) price)))

(defrecord BuyCitiesMessage [player-id cities]
  Validated
  (validate [this game]
    (let [cities (g/cities game)
          in-city? #(c/player-in-city? cities % player-id)]
      (cond
        (empty? cities) "Invalid cities"
        (some in-city? cities) "You may only build in a city once"
        (not (can-afford-cities? game player-id cities)) "Insufficient funds"
        )))

  GameUpdate
  (update-game [this game] game)

  Passable
  (passable? [_ _] true)
  (pass [_ game] game))

(def messages
  {:buy map->BuyCitiesMessage})
