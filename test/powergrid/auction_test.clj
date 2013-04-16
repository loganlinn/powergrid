(ns powergrid.auction-test
  (:require [powergrid.auction :refer :all]
            [midje.sweet :refer :all]))

(def empty-queue clojure.lang.PersistentQueue/EMPTY)

(fact accept-bid
  (let [i (new-auction {:player-id 3 :bidders (conj empty-queue 1 2 3) :price 200})
        f (new-auction {:player-id 1 :bidders (conj empty-queue 2 3 1) :price 210})]
    (accept-bid i 1 210) => f))

