(ns powergrid.cities.dijkstra
  (:require [clojure.set :as s]))

(def ^:private inf (Integer/MAX_VALUE))

(defn as-graph
  "Converts a map of edges to cost to a 2d graph"
  [cs]
  (reduce (fn [g [[n1 n2] cost]]
            (-> g (assoc-in [n1 n2] cost) (assoc-in [n2 n1] cost)))
          {} cs))

(defn neighbors
  "Returns n's neighbors, optionally filtered if unvisited"
  ([g n] (get g n {}))
  ([g n uv] (select-keys (neighbors g n) uv)))

(defn update-costs
  "Returns "
  [g costs curr unvisited]
  (reduce
    (fn [c [nbr nbr-cost]] (update-in c [nbr] (partial min (+ (c curr) nbr-cost))))
    costs
    (neighbors g curr unvisited)))

(defn dijkstra
  "Returns a mapping of nodes to minimum cost from src using Dijkstra algorithm.
  Graph is a mapping of nodes to map of neighboring nodes and associated cost.
  Optionally, specify :target node to return only the min price for target"
  [g src & {:keys [target]}]
  (loop [costs (assoc (zipmap (keys g) (repeat inf)) src 0)
         curr src
         unvisited (disj (apply hash-set (keys g)) src)]
    (if (or (empty? unvisited) (= inf (costs curr)))
      costs
      (let [costs' (update-costs g costs curr unvisited)
            curr' (first (sort-by costs' unvisited))]
        (if (= target curr)
          (costs' target)
          (recur costs'
                 curr'
                 (disj unvisited curr')))))))
