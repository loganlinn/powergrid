(ns powergrid.util
  (:import [clojure.lang Keyword PersistentQueue Seqable]))

(defn separate
  [f s]
  "Returns a vector: [ (filter f s) (filter (complement f) s) ]"
  [(filter f s) (filter (complement f) s)])

(defmethod print-method PersistentQueue
  [q w]
  (print-method '<- w)
  (print-method (seq q) w)
  (print-method '-< w))

(defprotocol Coersions
  (to-queue [q])
  (kw [s]))

(extend-protocol Coersions
  nil
  (to-queue [q] nil)
  (kw [s] nil)

  PersistentQueue
  (to-queue [q] q)

  Seqable
  (to-queue [q] (reduce conj PersistentQueue/EMPTY q))

  String
  (kw [s] (-> s clojure.string/lower-case keyword))

  Keyword
  (kw [s] s))

(defn queue
  "Coerces q into a PersistentQueue"
  ([] PersistentQueue/EMPTY)
  ([q] (to-queue q)))

(defn make-queue
  "Returns a queue with els"
  [& els]
  (reduce conj PersistentQueue/EMPTY els))

