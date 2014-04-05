(ns powergrid.domain.phase5-test
  (:require [midje.sweet :refer :all]
            [powergrid.domain.phase5 :refer :all]))

(fact total-yield
  (fact "3 1-yield power-plants"
    (total-yield [3 4 5]) => 3)
  (fact "with ecological"
    (total-yield [11 12 13]) => 5))

