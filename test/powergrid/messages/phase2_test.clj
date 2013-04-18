(ns powergrid.messages.phase2-test
  (:require [midje.sweet :refer :all]
            [powergrid.messages.phase2 :refer :all]
            [powergrid.message :refer :all]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.power-plants :as pp]))

(defn mock-buy
  [& args]
  (let [m (map->BuyPowerPlantMessage {:player-id 1 :plant-d 3 :amt 3})]
    (if (seq args) (apply assoc m args) m)))

(fact BuyPowerPlantMessage

  (fact passable?
    (fact "1st round"
      (passable? (mock-buy) ...game...) => falsey
      (provided (g/current-round ...game...) => 1))
    (fact "not 1st round"
      (passable? (mock-buy) ...game...) => truthy
      (provided (g/current-round ...game...) => 2)))
  )

