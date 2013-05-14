(ns powergrid.util
  (:import [clojure.lang Keyword PersistentQueue Seqable]))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn separate
  [f s]
  "Returns a vector: [ (filter f s) (filter (complement f) s) ]"
  [(filter f s) (filter (complement f) s)])

;(defmethod print-method PersistentQueue
  ;[q w]
  ;(print-method '<- w)
  ;(print-method (seq q) w)
  ;(print-method '-< w))

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
  (to-queue [q] (into PersistentQueue/EMPTY q))

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
  ([] (queue))
  ([& els] (queue els)))

(macroexpand '(ns-include "powergrid.common.cities" ->Cities map->Cities))
