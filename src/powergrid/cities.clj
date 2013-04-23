(ns powergrid.cities
  (require [powergrid.cities.dijkstra :refer [dijkstra]]))

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

(defn connection-cost-base
  "Returns price to add a connection in city."
  [cities city]
  (case (int (num-owners cities city))
    0 10, 1 15, 2 20))

(defn owner?
  "Returns true if player owns a connection in city, otherwise false"
  [cities city player-id]
  (some #{player-id} (city-owners cities city)))

(defn add-connection
  "Returns cities after player builds next connection in city.
  Asserts that city has capacity for new connections and that player does not
  already occupy city"
  [cities city player-id]
  {:pre [(< (num-owners cities city) 3)
         (not (owner? cities city player-id))]}
  (update-in cities [city] conj player-id))

(defn player-cities
  "Returns collection of cities the player owns"
  [cities player-id]
  (filter #(owner? cities % player-id) (keys (owners cities))))

(defn network-size
  [cities player-id]
  (count (player-cities cities player-id)))

(defn network-sizes
  [cities]
  (frequencies (flatten (vals cities))))

(defn connection-cost
  [cities src dst]
  (dijkstra ))

(defn player-connection-cost
  [cities city player-id]
  (+ (connection-cost-base cities city)
     (apply min (map ))))
