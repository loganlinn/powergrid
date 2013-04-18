(ns powergrid.messages.phase2-test
  (:require [midje.sweet :refer :all]
            [powergrid.messages.phase2 :refer :all]
            [powergrid.message :refer :all]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.power-plants :as pp]))

(defn mock-bid
  [& args]
  (let [m (map->BidPowerPlantMessage {:player-id 1 :plant-d 3 :bid 3})]
    (if (seq args) (apply assoc m args) m)))

(fact BidPowerPlantMessage
  (fact passable?
    (fact "1st round"
      (passable? (mock-bid) ...game...) => falsey
      (provided
        (g/has-auction? ...game...) => false
        (g/current-round ...game...) => 1))
    (fact "not 1st round"
      (passable? (mock-bid) ...game...) => truthy
      (provided
        (g/has-auction? ...game...) => false
        (g/current-round ...game...) => 2))))


