(ns powergrid.messages.phase5-test
  (:require [midje.sweet :refer :all]
            [powergrid.messages.phase5 :refer :all]
            [powergrid.message :refer :all]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.power-plants :as pp]))

(fact valid-sale?
  (fact "ecological power-plants"
    (valid-sale? [13 {}]) => truthy
    (valid-sale? [13 {anything 0}]) => truthy
    (valid-sale? [50 {anything 1}]) => falsey)
  (fact "invalid numbers"
    (valid-sale? [20 {:coal -1}]) => falsey)
  (fact "incorrect resource"
    (fact
      (valid-sale? [...p... {...r... 3}]) => falsey
      (provided
        (pp/plant ...p...) => ...plant...
        (pp/capacity ...plant...) => 3
        (pp/accepts-resource? ...plant... ...r...) => false))
    (fact
      (valid-sale? [...p... {...r1... 1, ...r2... 1}]) => falsey
      (provided
        (pp/plant ...p...) => ...plant...
        (pp/capacity ...plant...) => 2
        (pp/accepts-resource? ...plant... ...r1...) => true
        (pp/accepts-resource? ...plant... ...r2...) => false))))

(fact can-sell?
  (fact "invalid player"
    (can-sell? ...pid... [anything anything]) => falsey
    (provided
      (g/player ...pid...) => nil))
  (fact "invalid plant"
    (can-sell? ...pid... [...ppid... anything]) => falsey
    (provided
      (g/player ...pid...) => ...p...
      (pp/plant ...ppid..) => nil))
  (fact "non-owner"
    (can-sell? ...pid... [...ppid... anything]) => falsey
    (provided
      (g/player ...pid...) => ...p...
      (pp/plant ...ppid..) => ...pp...
      (p/owns-power-plant? ...p... ...pp...) => false))
  (fact "insufficient resources on power-plant"
    (can-sell? ...pid... [...ppid... anything]) => falsey
    (provided
      (g/player ...pid...) => ...p...
      (pp/plant ...ppid..) => ...pp...
      (pp/consumes-resources? ...pp..) => true
      (p/owns-power-plant? ...p... ...pp...) => true
      (p/can-power-plant? ...p... ...pp...) => false)))
