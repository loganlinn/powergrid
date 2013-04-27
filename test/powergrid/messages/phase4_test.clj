(ns powergrid.messages.phase4-test
  (:require [midje.sweet :refer :all]
            [powergrid.messages.phase4 :refer :all]
            [powergrid.message :refer :all]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.power-plants :as pp]))


(fact "passable"
  (passable? (map->BuyCitiesMessage {}) ...game...) => truthy
  (pass (map->BuyCitiesMessage {}) ...game...) => ..game...)
