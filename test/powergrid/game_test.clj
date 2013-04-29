(ns powergrid.game-test
  (:require [midje.sweet :refer :all]
            [powergrid.game :refer :all]
            [powergrid.player :as p]
            [powergrid.cities :as c]
            [powergrid.power-plants :as pp]
            ))

(fact sorted-players-map
  (seq (sorted-players-map ...game...)) => (seq {2 ...Anna...
                                                 1 ...Dale...
                                                 3 ...Angelika...
                                                 4 ...Valerie...})
  (provided
    (players-map ...game...) => {1 ...Dale...
                                 2 ...Anna...
                                 3 ...Angelika...
                                 4 ...Valerie...}
    (network-size ...game... 1) => 5
    (network-size ...game... 2) => 6
    (network-size ...game... 3) => 5
    (network-size ...game... 4) => 4
    (p/max-power-plant ...Dale...)     => 17
    (p/max-power-plant ...Anna...)     => ...dont-care...
    (p/max-power-plant ...Angelika...) => 15
    (p/max-power-plant ...Valerie...)  => ...dont-care...))

