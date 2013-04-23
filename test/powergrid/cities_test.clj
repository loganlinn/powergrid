(ns powergrid.cities-test
  (:require [midje.sweet :refer :all]
            [powergrid.cities :refer :all]))

(fact as-graph
  (as-graph {[:a :b] 1, [:a :c] 2, [:b :c] 3})
  => {:a {:b 1 :c 2}
      :b {:a 1 :c 3}
      :c {:a 2 :b 3}})
