(ns powergrid.util)

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
