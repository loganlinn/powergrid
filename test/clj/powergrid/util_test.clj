(ns powergrid.util-test
  (:require [midje.sweet :refer :all]
            [powergrid.util :refer :all])
  (:import [clojure.lang PersistentQueue]))

(fact separate
  (separate #{3} (range 5)) => ['(3) '(0 1 2 4)]
  (separate #{3 4} (range 5)) => ['(3 4) '(0 1 2)]
  (separate odd? (range 6)) => ['(1 3 5) '(0 2 4)]
  (separate even? (range 6)) => ['(0 2 4) '(1 3 5)])

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

(fact kw
  (kw nil) => nil
  (kw "blue") => :blue
  (kw "BLUE") => :blue
  (kw :blue) => :blue)
