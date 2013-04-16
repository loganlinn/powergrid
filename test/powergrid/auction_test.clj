(ns powergrid.auction-test
  (:require [powergrid.auction :refer :all]
            [midje.sweet :refer :all]))

(def empty-queue clojure.lang.PersistentQueue/EMPTY)

(fact bid
  (let [a0 (new-auction {:player-id 3 :bidders (conj empty-queue 1 2 3) :price 200})
        a1 (bid a0 1 210)]
    (:price a1) => 210
    (:player-id a1) => 1
    (= '(2 3 1) (seq (:bidders a1))) => truthy
    (completed? a1) => falsey))

(fact new-auction "defaults"
  (let [a (new-auction {:player-id 3 :bidders [1 2 3] :price 200})]
    (instance? clojure.lang.PersistentQueue (:bidders a))
    (= (conj empty-queue 1 2 3) (:bidders a))
    (number? (:price a)) => true
    (number? (:min-increment a)) => true))

(fact completed?
  (let [a (pass (new-auction {:player-id 3 :bidders (conj empty-queue 1 3) :price 200}))]
    (completed? a) => truthy))

