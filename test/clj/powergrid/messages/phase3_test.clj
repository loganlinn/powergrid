(ns powergrid.messages.phase3-test
  (:refer-clojure :exclude [type])
  (:require [midje.sweet :refer :all]
            [powergrid.domain.phase3 :refer [map->BuyResourcesMessage]]
            [powergrid.messages.phase3 :refer :all]
            [powergrid.message :as msg]
            [powergrid.game :as g]
            [powergrid.domain.player :as p]
            [powergrid.domain.resource :as r]
            [powergrid.domain.power-plants :as pp]))

(fact total-price
  (total-price ...game... {:coal 5 :oil 3}) => 15
  (provided
    (g/resource ...game... :coal) => ...coal...
    (g/resource ...game... :oil) => ...oil...
    (r/resource-price ...coal... 5) => 10
    (r/resource-price ...oil... 3) => 5))


(fact "passable"
  (msg/passable? (map->BuyResourcesMessage {}) ...game...) => truthy
  (msg/update-pass (map->BuyResourcesMessage {}) ...game... msg/nil-logger) => ..game...)
