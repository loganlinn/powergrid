(ns powergrid.cities-test
  (:require [midje.sweet :refer :all]
            [powergrid.cities :refer :all]))

(fact as-graph
  (as-graph {[:a :b] 1, [:a :c] 2, [:b :c] 3})
  => {:a {:b 1 :c 2}
      :b {:a 1 :c 3}
      :c {:a 2 :b 3}})

(tabular
  (fact player-owns-city?
    (let [cities {:boston [1]
                  :new-york [2 1 3]
                  :buffalo [3]}]
      (fact (player-owns-city? cities ?player ?city) => ?expected)))
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

(fact player-cities
  (let [cities {:boston [1]
                :new-york [2 1 3]
                :buffalo [3]}]
    (fact (player-cities cities 1) => (just #{:boston :new-york}))
    (fact (player-cities cities 2) => (just #{:new-york}))
    (fact (player-cities cities 3) => (just #{:new-york :buffalo}))))
