(ns powergrid.cities
  (require [powergrid.cities.dijkstra :as d]))

(defn as-graph
  "Converts a map of edges to cost to a 2d graph"
  [cs]
  (reduce (fn [g [[n1 n2] cost]]
            (-> g (assoc-in [n1 n2] cost) (assoc-in [n2 n1] cost)))
          {} cs))

(defrecord Cities [owners connections])

(defn owners [cities] (:owners cities))
(defn connections [cities] (:connections cities))

(defn valid-city?
  "Returns true if city is a valid city within cities, otherwise false"
  [cities city]
  (contains? (connections cities) city))

(defn city-owners
  "Returns vector of player-ids who own a connection in city"
  [cities city]
  (get (owners cities) city []))

(def num-owners (comp count city-owners))

(defn owner?
  "Returns true if player owns a connection in city, otherwise false"
  [cities city player-id]
  (some #{player-id} (city-owners cities city)))

(defn owned-cities
  "Returns collection of cities the player owns"
  [cities player-id]
  (keep #(when (some #{player-id} (val %)) (key %)) (owners cities)))

(defn build-cost
  "Returns price to build in city (excludes connection cost)"
  [cities city]
  (case (int (num-owners cities city))
    0 10, 1 15, 2 20))

(defn buildable-city?
  "Returns true if player-id can build in city, otherwise false"
  [cities city player-id game-step]
  (let [owners (city-owners cities city)]
    (and (< (count owners) game-step)
         (not-any? #{player-id} owners))))

(defn add-owner
  "Returns cities after associating player-id as owner of city.
  Asserts that city has capacity for new connections and that player does not
  already occupy city"
  [cities city player-id]
  {:pre [(< (num-owners cities city) 3)
         (not (owner? cities city player-id))]}
  (update-in cities [:owners city] conj player-id))

(defn network-size
  "Returns number of cities a player owns"
  [cities player-id]
  (reduce #(if (some #{player-id} %2) (inc %1) %1) 0 (vals (owners cities))))

(defn network-sizes
  "Returns map of player-id to number of cities they own (for the players who own cities)"
  [cities]
  (frequencies (flatten (vals (owners cities)))))

(defn connection-cost
  "Returns connection cost (exlcludes building cost) between two cities given
  the connections graph, conns"
  [conns src dst]
  (d/dijkstra conns src :target dst))

(defn min-connection-cost
  "Returns the minimum connection cost (excludes building cost) to a city from
  any of player-id's current cities"
  [cities player-id dst]
  (let [conns (connections cities)]
    (apply min (map #(connection-cost conns % dst) ;; TODO pmap helpful?
                    (owned-cities cities player-id)))))

(defn purchase-cost
  "Returns cost for player-id to purchase cities in purchase. Total cost is
  calculated by purchasing cities in the order they appear.  Assumes player is
  permitted to build in each city"
  [cities player-id purchase]
  (loop [cities cities
         [city & cs] purchase
         total-cost 0]
    (if city
      (let [cost (min-connection-cost cities player-id city)]
        (recur (add-owner cities city player-id)
               cs
               (if (= cost d/inf) d/inf (+ total-cost cost))))
      total-cost)))
