(ns powergrid.cities-test
  (:require [midje.sweet :refer :all]
            [powergrid.cities :refer :all]))

(fact as-graph
  (as-graph {[:a :b] 1, [:a :c] 2, [:b :c] 3})
  => {:a {:b 1 :c 2}
      :b {:a 1 :c 3}
      :c {:a 2 :b 3}})

(fact player-owns-city?
  (let [cities {:boston [1]
                :new-york [2 1 3]
                :buffalo [3]}]
    (fact (player-owns-city? cities 1 :boston) => truthy)
    (fact (player-owns-city? cities 1 :new-york) => truthy)
    (fact (player-owns-city? cities 1 :buffalo) => falsey)
    (fact (player-owns-city? cities 2 :boston) => falsey)
    (fact (player-owns-city? cities 2 :new-york) => truthy)
    (fact (player-owns-city? cities 2 :buffalo) => falsey)
    (fact (player-owns-city? cities 3 :boston) => falsey)
    (fact (player-owns-city? cities 3 :new-york) => truthy)
    (fact (player-owns-city? cities 3 :buffalo) => truthy)))

(fact player-cities
  (let [cities {:boston [1]
                :new-york [2 1 3]
                :buffalo [3]}]
    (fact (player-cities cities 1) => (just [:boston :new-york] :in-any-order))
    (fact (player-cities cities 2) => (just [:new-york] :in-any-order))
    (fact (player-cities cities 3) => (just [:new-york :buffalo] :in-any-order))))
