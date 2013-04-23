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
  true)

(defrecord BuyCitiesMessage [player-id cities]
  Validated
  (validate [this game]
    (let [in-city? #(c/owner? (g/cities game) % player-id)]
      (cond
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
