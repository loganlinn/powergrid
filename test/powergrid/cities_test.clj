(ns powergrid.cities-test
  (:require [midje.sweet :refer :all]
            [powergrid.cities :refer :all]))

(fact as-graph
  (as-graph {[:a :b] 1, [:a :c] 2, [:b :c] 3})
  => {:a {:b 1 :c 2}
      :b {:a 1 :c 3}
      :c {:a 2 :b 3}})

(tabular
  (fact owner?
    (let [cities {:owners {:boston   [1]
                           :new-york [2 1 3]
                           :buffalo  [3]}}]
      (fact (owner? cities ?player ?city) => ?expected)))
  ?player ?city     ?expected
  1       :boston   truthy
  1       :new-york truthy
  1       :buffalo  falsey
  2       :boston   falsey
  2       :new-york truthy
  2       :buffalo  falsey
  3       :boston   falsey
  3       :new-york truthy
  3       :buffalo  truthy)

(fact owned-cities
  (let [cities {:owners {:boston   [1]
                         :new-york [2 1 3]
                         :buffalo  [3]}}]
    (fact (owned-cities cities 1) => (just #{:boston :new-york}))
    (fact (owned-cities cities 2) => (just #{:new-york}))
    (fact (owned-cities cities 3) => (just #{:new-york :buffalo}))))

(fact min-connection-cost
  (let [cities {:owners {:a [1] :b [1]}
                :connections (as-graph {[:a :c] 4 [:b :c] 3})}]
    (min-connection-cost cities 1 :c) => 3))

(fact min-connection-cost
  (min-connection-cost ...cities... ...pid... ...dst...) => 10
  (provided
    (owned-cities ...cities... ...pid...) => [:a :b]
    (connections ...cities...) => ...conns...
    (connection-cost ...conns... :a ...dst...) => 10
    (connection-cost ...conns... :b ...dst...) => 12))

(fact purchase-cost
  (fact "should choose sides of square"
    (let [cities {:owners {:a [1] :b [1] :c [] :d []}
                  :connections (as-graph {[:a :c] 10 [:a :d] 14
                                          [:b :d] 10 [:b :c] 14})}]
      (purchase-cost cities 1 [:c :d]) => (+ 20 20)))
  (fact "should go through first purchase"
    (let [cities {:owners {:a [1]}
                  :connections (as-graph {[:a :c] 4 [:a :d] 8
                                          [:c :d] 2})}]
      (purchase-cost cities 1 [:c :d]) => (+ 6 20)))
  (fact "shouldn't fail when unreachable"
    (let [cities {:owners {:a [1]}
                  :connections (as-graph {[:a :x] 100
                                          [:c :y] 100})}]
      (purchase-cost cities 1 [:c]) => anything)))

(fact add-owner
  (let [before {:owners {...city... [...x...]}}
        after {:owners {...city... [...x... ...pid...]}}]
   (add-owner before ...pid... ...city...) => after))

(fact network-size
  (let [cities {:owners {:a [1] :b [2 3 1] :c [2] :d [3 1 2]}}]
    (network-size cities 1) => 3
    (network-size cities 2) => 3
    (network-size cities 3) => 2
    (network-sizes cities) => {1 3, 2 3, 3 2}))

(fact buildable-city?
  (fact "by ownership"
    (buildable-city? {:owners {:city [1]}} :city 1 3) => falsey)
  (fact "by phase"
    (buildable-city? {:owners {:city [2]}} :city 1 1) => falsey
    (buildable-city? {:owners {:city [2 3]}} :city 1 2) => falsey
    (buildable-city? {:owners {:city [2 3 4]}} :city 1 3) => falsey
    (buildable-city? {:owners {:city [2 3]}} :city 1 3) => truthy
    (buildable-city? {:owners {:city [2]}} :city 1 2) => truthy
    (buildable-city? {:owners {:city []}} :city 1 1) => truthy))
