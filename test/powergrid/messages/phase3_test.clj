(ns powergrid.messages.phase3-test
  (:refer-clojure :exclude [type])
  (:require [midje.sweet :refer :all]
            [powergrid.messages.phase3 :refer :all]
            [powergrid.message :refer :all]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.resource :as r]
            [powergrid.common.power-plants :as pp]))

(fact total-price
  (total-price ...game... {:coal 5 :oil 3}) => 15
  (provided
    (g/resource ...game... :coal) => ...coal...
    (g/resource ...game... :oil) => ...oil...
    (r/resource-price ...coal... 5) => 10
    (r/resource-price ...oil... 3) => 5))


(fact "passable"
  (passable? (map->BuyResourcesMessage {}) ...game...) => truthy
  (update-pass (map->BuyResourcesMessage {}) ...game...) => ..game...)
