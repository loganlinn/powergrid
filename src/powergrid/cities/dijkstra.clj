(ns powergrid.cities.dijkstra
  (:require [clojure.set :as s]))

(def ^:private inf (Integer/MAX_VALUE))

(defn edges->graph
  "Converts a map of edges to cost to a 2d graph"
  [cs]
  (reduce (fn [g [[n1 n2] cost]]
            (-> g (assoc-in [n1 n2] cost) (assoc-in [n2 n1] cost)))
          {} cs))

(defn nodes [g] (keys g))

(defn neighbors
  "Returns n's neighbors, optionally filtered if unvisited"
  ([g n] (get g n {}))
  ([g n uv] (select-keys (neighbors g n) uv)))

(defn edge-cost [g n1 n2] (get-in g [n1 n2]))

(defn update-neighbor-cost
  [curr costs [nbr nbr-cost]]
  (update-in costs [nbr] (partial min (+ (curr costs) nbr-cost))))

(defn dijkstra
  [g src & {:keys [target]}]
  (loop [costs (assoc (zipmap (nodes g) (repeat inf)) src 0)
         curr src
         unvisited (disj (apply hash-set (nodes g)) src)]
    ;; sorted set?
    (let [nbrs (neighbors g curr unvisited)
          costs' (reduce (partial update-neighbor-cost curr) costs nbrs)
          curr' (first (sort-by #(% costs') (keys nbrs)))
          unvisited' (disj unvisited curr)]
      (if (or (empty? unvisited' ) (= target curr) (= inf (curr' costs')))
        costs'
        (recur costs' curr' unvisited')))))

