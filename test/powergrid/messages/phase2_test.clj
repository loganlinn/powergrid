(ns powergrid.messages.phase2-test
  (:require [midje.sweet :refer :all]
            [powergrid.messages.phase2 :refer :all]
            [powergrid.message :refer :all]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.auction :as a]
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

(fact new-auction
  (fact "min bid starts at price of power-plant"
    (a/min-bid (new-auction ...game... 25)) => 25)
  (fact "sets item"
    (:item (new-auction ...game... 3)) => (pp/plant 3))
  (fact "bidders taken from game turns"
    (:bidders (new-auction ...game... 3)) => '(1 2 3)
    (provided
      (g/turns ...game...) => '(1 2 3))))

(fact auction
  (fact "new auction"
    (auction ...game... ...plant-id...) => ...new-auction...
    (provided
      (g/current-auction ...game...) => nil
      (new-auction ...game... ...plant-id...) => ...new-auction...))
  (fact "existing auction"
    (auction ...game... ...plant-id...) => ...auction...
    (provided
      (g/current-auction ...game...) => ...auction...)))
