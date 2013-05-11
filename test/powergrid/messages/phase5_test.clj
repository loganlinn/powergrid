(ns powergrid.messages.phase5-test
  (:require [midje.sweet :refer :all]
            [powergrid.messages.phase5 :refer :all]
            [powergrid.message :refer [passable? update-pass]]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.power-plants :as pp]))

(fact validate-sale
  (fact "ecological power-plants"
    (validate-sale [13 {}]) => falsey
    (validate-sale [13 {anything 0}]) => falsey
    (validate-sale [50 {anything 1}]) => truthy)
  (fact "invalid numbers"
    (validate-sale [20 {:coal -1}]) => truthy)
  (fact "incorrect resource"
    (fact
      (validate-sale [...p... {...r... 3}]) => truthy
      (provided
        (pp/plant ...p...) => ...plant...
        (pp/capacity ...plant...) => 3
        (pp/accepts-resource? ...plant... ...r...) => false))
    (fact
      (validate-sale [...p... {...r1... 1, ...r2... 1}]) => truthy
      (provided
        (pp/plant ...p...) => ...plant...
        (pp/capacity ...plant...) => 2
        (pp/accepts-resource? ...plant... ...r1...) => true
        (pp/accepts-resource? ...plant... ...r2...) => false))))

(fact can-sell?
  (fact "invalid player"
    (can-sell? nil [anything anything]) => falsey)
  (fact "invalid plant"
    (can-sell? ...p... [...ppid... anything]) => falsey
    (provided
      (pp/plant ...ppid..) => nil))
  (fact "non-owner"
    (can-sell? ...p... [...ppid... anything]) => falsey
    (provided
      (pp/plant ...ppid..) => ...pp...
      (p/owns-power-plant? ...p... ...ppid...) => false))
  (fact "insufficient resources on power-plant"
    (can-sell? ...p... [...ppid... anything]) => falsey
    (provided
      (pp/plant ...ppid..) => ...pp...
      (pp/consumes-resources? ...pp..) => true
      (p/owns-power-plant? ...p... ...ppid...) => true
      (p/can-power-plant? ...p... ...ppid...) => false)))

(fact flatten-sale
  (flatten-sale {10 {:coal 1 :oil 1} 11 {:trash 1}})
  => (just #{[10 :coal 1] [10 :oil 1] [11 :trash 1]}))

(fact total-yield
  (fact "3 1-yield power-plants"
    (total-yield [3 4 5]) => 3)
  (fact "with ecological"
    (total-yield [11 12 13]) => 5))
