(ns powergrid.cities.dijkstra-test
  (:require [midje.sweet :refer :all]
            [powergrid.cities :refer [as-graph]]
            [powergrid.cities.dijkstra :refer :all]
            [powergrid.cities.usa :as usa]))

(fact dijkstra
  (fact "simple graph"
    (let [g (as-graph {[:a :b] 1, [:a :c] 2, [:b :c] 3})]
      (dijkstra g :a) => {:a 0 :b 1 :c 2}))

  (fact "http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm"
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
                         6 11}))

  (fact "http://www.cs.auckland.ac.nz/~jmor159/PLDS210/dij-op.html"
    (let [g {:s {:y 7 :u 10 :x 5}
             :x {:y 2 :s 5 :u 3 :v 9}
             :y {:x 2 :s 7 :v 6}
             :u {:s 10 :x 2 :v 1}
             :v {:u 1 :x 5 :y 4}}]
      (dijkstra g :s) => {:s 0 :x 5 :y 7 :u 8 :v 9}
      (dijkstra g :s :target :x) => 5
      (dijkstra g :y :target :v) => 6
      (dijkstra g :v :target :y) => 4
      (dijkstra g :v :target :x) => 3))

  (fact "http://www.youtube.com/watch?v=Ef8RzA9C_bA"
    (let [g (as-graph {[:a :b] 3 [:a :c] 8 [:a :d] 12
                       [:b :c] 4 [:b :e] 2
                       [:c :d] 3 [:c :f] 9 [:c :g] 12 [:c :e] 1
                       [:d :f] 5
                       [:e :g] 14
                       [:g :f] 3})]
      (dijkstra g :a :target :b) => 3
      (future-fact "dijkstra tests")))

  (fact "usa board"
    (let [g (as-graph usa/connections)]
      (dijkstra g :boston :target :new-york) => 3
      (dijkstra g :raleigh :target :washington) => 8
      (dijkstra g :atlanta :target :kansas-city) => 18
      (dijkstra g :houston :target :duluth) => 34)))
