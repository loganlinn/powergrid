(ns powergrid.messages.phase4-test
  (:require [midje.sweet :refer :all]
            [powergrid.domain.phase4 :refer [map->BuyCitiesMessage]]
            [powergrid.messages.phase4 :refer :all]
            [powergrid.message :as msg]
            [powergrid.game :as g]
            [powergrid.domain.player :as p]
            [powergrid.domain.power-plants :as pp]))

(fact "passable"
  (msg/passable? (map->BuyCitiesMessage {}) ...game...) => truthy
  (msg/update-pass (map->BuyCitiesMessage {}) ...game... msg/nil-logger) => ..game...)
