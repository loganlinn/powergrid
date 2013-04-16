(ns powergrid.auction-test
  (:require [powergrid.auction :refer :all]
            [midje.sweet :refer :all]))

(fact new-auction "defaults"
  (let [a (new-auction {:player-id 3 :bidders [1 2 3] :price 200})]
    (instance? clojure.lang.PersistentQueue (:bidders a))
    (= (conj empty-queue 1 2 3) (:bidders a))
    (number? (:price a)) => true
    (number? (:min-increment a)) => true))

(fact bid
  (let [a0 (new-auction {:player-id 3 :bidders [1 2 3] :price 200})
        a1 (bid a0 1 210)]
    (:price a1) => 210
    (:player-id a1) => 1
    (= '(2 3 1) (seq (:bidders a1))) => truthy
    (completed? a1) => falsey
    (current-bidder a0) => 1
    (current-bidder a1) => 2))

(fact pass
  (let [a0 (new-auction {:player-id 3 :bidders [1 2 3] :price 200})
        a1 (pass a0)]
    (:price a1) => 200
    (:player-id a1) => 3
    (= '(2 3) (seq (:bidders a1))) => truthy
    (completed? a1) => falsey
    (current-bidder a0) => 1
    (current-bidder a1) => 2))

(fact completed?
  (let [a (pass (new-auction {:player-id 3 :bidders [1 3] :price 200}))]
    (completed? a) => truthy))

(fact min-bid
  (min-bid {:price 100 :min-increment 1}) => 101
  (min-bid {:price 100 :min-increment 100}) => 200)
