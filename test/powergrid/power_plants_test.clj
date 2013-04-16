(ns powergrid.power-plants-test
  (:require [midje.sweet :refer :all]
            [powergrid.power-plants :refer :all]
            [powergrid.player :as p]
            [powergrid.game :as g]))

(fact accepts-resource?
  (accepts-resource? {:number 20, :resource :coal, :capacity 3, :yield 5} :coal) => true
  (accepts-resource? {:number 20, :resource :coal, :capacity 3, :yield 5} :oil) => false
  (accepts-resource? {:number 29, :resource #{:coal :oil}, :capacity 1, :yield 4} :coal) => true
  (accepts-resource? {:number 29, :resource #{:coal :oil}, :capacity 1, :yield 4} :oil) => true
  (accepts-resource? {:number 29, :resource #{:coal :oil}, :capacity 1, :yield 4} :garbage) => false)
