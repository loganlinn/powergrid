(ns powergrid.cities.dijkstra-test
  (:require [midje.sweet :refer :all]
            [powergrid.cities.dijkstra :refer :all]
            [powergrid.cities.usa :as usa]))

(fact as-graph
  (as-graph {[:a :b] 1, [:a :c] 2, [:b :c] 3})
  => {:a {:b 1 :c 2}
      :b {:a 1 :c 3}
      :c {:a 2 :b 3}})

(fact dijkstra
  (fact "simple graph"
    (let [g (as-graph {[:a :b] 1, [:a :c] 2, [:b :c] 3})]
      (dijkstra g :a) => {:a 0 :b 1 :c 2}))

  (fact "wikipedia example"
    (let [g (as-graph {[1 2] 7
                       [1 3] 9
                       [1 6] 14
                       [2 3] 10
                       [2 4] 15
                       [3 4] 11
                       [3 6] 2
                       [4 5] 6
                       [5 6] 9})]
      (dijkstra g 1) => {1 0
                         2 7
                         3 9
                         4 20
                         5 20
                         6 11})))
