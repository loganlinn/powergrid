(ns powergrid.messages.phase2-test
  (:require [midje.sweet :refer :all]
            [powergrid.message :as msg]
            [powergrid.domain.messages :refer [map->BidPowerPlantMessage]]
            [powergrid.messages.phase2 :refer :all]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.auction :as a]
            [powergrid.common.power-plants :as pp]))

(defn mock-bid
  [& args]
  (let [m (map->BidPowerPlantMessage {:player-id 1 :plant-d 3 :bid 3})]
    (if (seq args) (apply assoc m args) m)))

(fact BidPowerPlantMessage
  (fact msg/passable?
    (fact "1st round"
      (msg/passable? (mock-bid) ...game...) => falsey
      (provided
        (g/has-auction? ...game...) => false
        (g/current-round ...game...) => 1))
    (fact "not 1st round"
      (msg/passable? (mock-bid) ...game...) => truthy
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

(fact get-or-create-auction
  (fact "new auction"
    (get-or-create-auction ...game... ...plant-id...) => ...new-auction...
    (provided
      (g/auction ...game...) => nil
      (new-auction ...game... ...plant-id...) => ...new-auction...))
  (fact "existing auction"
    (get-or-create-auction ...game... ...plant-id...) => ...auction...
    (provided
      (g/auction ...game...) => ...auction...)))


