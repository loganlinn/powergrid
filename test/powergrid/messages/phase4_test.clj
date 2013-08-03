(ns powergrid.messages.phase4-test
  (:require [midje.sweet :refer :all]
            [powergrid.domain.messages :refer [map->BuyCitiesMessage]]
            [powergrid.messages.phase4 :refer :all]
            [powergrid.message :as msg]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.power-plants :as pp]))


(fact "passable"
  (msg/passable? (map->BuyCitiesMessage {}) ...game...) => truthy
  (msg/update-pass (map->BuyCitiesMessage {}) ...game... msg/nil-logger) => ..game...)
