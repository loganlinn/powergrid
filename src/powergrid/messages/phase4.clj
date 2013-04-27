(ns powergrid.messages.phase4
  (:require [powergrid.message :refer [Validated GameUpdate Passable]]
            [powergrid.game :as g]
            [powergrid.cities :as c]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(defn can-afford-cities?
  [game player-id cities]
  (let [player (g/player player-id)
        cost (c/purchase-cost (g/cities game) player-id cities)]
    (p/can-afford? cost)))

(defn valid-city?
  [game player-id city]
  (let [cities (g/cities game)]
    (and (c/valid-city? cities city)
         (c/buildable-city? cities city player-id (g/current-step game)))))

(defrecord BuyCitiesMessage [player-id new-cities]
  Validated
  (validate [this game]
    (let [cities (g/cities game)
          in-city? #(c/owner? (g/cities game) % player-id)]
      (cond
        (not (every? (partial valid-city? game player-id) new-cities)) "Invalid city"
        (not (can-afford-cities? game player-id new-cities)) "Insufficient funds"
        )))

  GameUpdate
  (update-game [this game] game)

  Passable
  (passable? [_ _] true)
  (pass [_ game] game))

(def messages
  {:buy map->BuyCitiesMessage})
