(ns powergrid.util
  (:import [clojure.lang PersistentQueue Seqable]))

(defn separate
  [f s]
  "Returns a vector: [ (filter f s) (filter (complement f) s) ]"
  [(filter f s) (filter (complement f) s)])

(defprotocol Keywordable
  (kw [this]))

(extend-protocol Keywordable
  String
  (kw [this] (-> this clojure.string/lower-case keyword))
  clojure.lang.Keyword
  (kw [this] this)
  nil
  (kw [_] nil))

(defmethod print-method clojure.lang.PersistentQueue
  [q w]
  (print-method '<- w)
  (print-method (seq q) w)
  (print-method '-< w))

(defprotocol Coersions
  (to-queue [q]))

(extend-protocol Coersions
  nil
  (to-queue [q] nil)

  PersistentQueue
  (to-queue [q] q)

  Seqable
  (to-queue [q] (reduce conj PersistentQueue/EMPTY q)))

(defn queue
  "Coerces q into a PersistentQueue"
  ([] PersistentQueue/EMPTY)
  ([q] (to-queue q)))

(defn make-queue
  "Returns a queue with els"
  [& els]
  (reduce conj PersistentQueue/EMPTY els))

