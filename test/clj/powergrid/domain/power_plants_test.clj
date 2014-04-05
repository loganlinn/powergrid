(ns powergrid.domain.power-plants-test
  (:require [midje.sweet :refer :all]
            [powergrid.domain.power-plants :refer :all]
            [powergrid.domain.player :as p]
            [powergrid.game :as g]))

(fact initial-market
  (map :number (initial-market)) => [3 4 5 6])

(fact future-market
  (map :number (initial-future)) => [7 8 9 10])

(fact initial-deck
  (some (set (range 3 11)) (initial-deck)) => falsey
  (count (initial-deck)) => 34)

(fact plant
  (plant 3) => (get power-plants 3)
  (plant 50) => (get power-plants 50))

(fact is-hybrid?
  (is-hybrid? (plant 5)) => truthy
  (is-hybrid? (plant 3)) => falsey)

(fact "sort-by is-hybrid?"
  (fact (first (sort-by is-hybrid? [...pp1... ...pp2... ...pp3...])) => ...pp1...
    (provided
      (is-hybrid? ...pp1...) => false
      (is-hybrid? ...pp2...) => true
      (is-hybrid? ...pp3...) => true))
  (fact (last (sort-by is-hybrid? [...pp1... ...pp2... ...pp3...])) => ...pp1...
    (provided
      (is-hybrid? ...pp1...) => true
      (is-hybrid? ...pp2...) => false
      (is-hybrid? ...pp3...) => false)))

(fact consumes-resources?
  (consumes-resources? (plant 3)) => truthy
  (consumes-resources? (plant 5)) => truthy
  (consumes-resources? (plant 13)) => falsey
  (consumes-resources? (plant 50)) => falsey)

(fact min-price
  (min-price (plant 3)) => 3
  (min-price (plant 50)) => 50)

(tabular
  (fact accepts-resource?
    (accepts-resource? ?plant ?resource) => ?expected)

  ?plant     ?resource   ?expected
  (plant 4)  :coal       truthy
  (plant 4)  :oil        falsey
  (plant 12) :coal       truthy
  (plant 12) :oil        truthy
  (plant 12) :garbage    falsey
  (plant 6)  :garbage    truthy
  (plant 13) :oil        falsey
  (plant 13) :ecological falsey
  (plant 50) :coal       falsey
  (plant 50) :fusion     falsey)
