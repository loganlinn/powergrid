(ns powergrid.core-test
  (:require [midje.sweet :refer :all]
            [powergrid.core :refer :all]
            [powergrid.game :refer [player-key players-map]]))

(fact player-order
  (vals (player-order (players-map [...Dale... ...Anna... ...Angelika... ...Valerie...])))
  => [...Anna... ...Dale... ...Angelika... ...Valerie...]
  (provided
    (player-key ...Dale...)     => 3
    (player-key ...Anna...)     => 1
    (player-key ...Angelika...) => 2
    (player-key ...Valerie...)  => 4
    (network-size ...Dale...)     => 5
    (network-size ...Anna...)     => 6
    (network-size ...Angelika...) => 5
    (network-size ...Valerie....) => 4
    (max-power-plant ...Dale...)     => 17
    (max-power-plant ...Anna...)     => ...dont-care...
    (max-power-plant ...Angelika...) => 15
    (max-power-plant ...Valerie...)  => ...dont-care...))

(fact accepts-resource?
 (accepts-resource? {:number 20, :resource :coal, :capacity 3, :yield 5} :coal) => true
 (accepts-resource? {:number 20, :resource :coal, :capacity 3, :yield 5} :oil) => false
 (accepts-resource? {:number 29, :resource #{:coal :oil}, :capacity 1, :yield 4} :coal) => true
 (accepts-resource? {:number 29, :resource #{:coal :oil}, :capacity 1, :yield 4} :oil) => true
 (accepts-resource? {:number 29, :resource #{:coal :oil}, :capacity 1, :yield 4} :garbage) => false)
