(ns powergrid.game-test
  (:require [midje.sweet :refer :all]
            [powergrid.game :refer :all]
            [powergrid.common.player :as p]
            [powergrid.cities :as c]
            [powergrid.common.power-plants :as pp]
            ))

(fact player-id-order
  (player-id-order ...game...) => (just [2 1 3 4])
  (provided
    (p/id ...Dale...)     => 1
    (p/id ...Anna...)     => 2
    (p/id ...Angelika...) => 3
    (p/id ...Valerie...)  => 4
    (players ...game...) => [...Dale...
                                 ...Anna...
                                 ...Angelika...
                                 ...Valerie...]
    (network-size ...game... 1) => 5
    (network-size ...game... 2) => 6
    (network-size ...game... 3) => 5
    (network-size ...game... 4) => 4
    (p/max-power-plant ...Dale...)     => 17
    (p/max-power-plant ...Anna...)     => ...dont-care...
    (p/max-power-plant ...Angelika...) => 15
    (p/max-power-plant ...Valerie...)  => ...dont-care...))

