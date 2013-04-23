(ns powergrid.core-test
  (:require [midje.sweet :refer :all]
            [powergrid.core :refer :all]
            [powergrid.player :as p]
            [powergrid.game :as g]))

(fact player-order
  (vals (player-order ...game... (g/players-map [...Dale... ...Anna... ...Angelika... ...Valerie...])))
  => [...Anna... ...Dale... ...Angelika... ...Valerie...]
  (provided
    (p/id ...Dale...)     => 3
    (p/id ...Anna...)     => 1
    (p/id ...Angelika...) => 2
    (p/id ...Valerie...)  => 4
    (g/network-size ...game... ...Dale...)     => 5
    (g/network-size ...game... ...Anna...)     => 6
    (g/network-size ...game... ...Angelika...) => 5
    (g/network-size ...game... ...Valerie....) => 4
    (p/max-power-plant ...Dale...)     => 17
    (p/max-power-plant ...Anna...)     => ...dont-care...
    (p/max-power-plant ...Angelika...) => 15
    (p/max-power-plant ...Valerie...)  => ...dont-care...))

