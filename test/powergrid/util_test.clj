(ns powergrid.util-test
  (:require [midje.sweet :refer :all]
            [powergrid.util :refer :all])
  (:import [clojure.lang PersistentQueue]))

(fact queue
  (queue nil) => nil
  (queue) => PersistentQueue/EMPTY
  (queue []) => PersistentQueue/EMPTY
  (queue '()) => PersistentQueue/EMPTY
  (queue (conj PersistentQueue/EMPTY :a)) => (conj PersistentQueue/EMPTY :a)
  (peek (queue [1 2])) => 1
  (peek (queue '(1 2))) => 1
  (pop (queue [1 2])) => (conj PersistentQueue/EMPTY 2)
  (pop (queue '(1 2))) => (conj PersistentQueue/EMPTY 2))

(fact make-queue
  (make-queue) => PersistentQueue/EMPTY
  (make-queue 1 2 3) => (queue [1 2 3]))
