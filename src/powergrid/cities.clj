(ns powergrid.cities)

(comment
  {:city [player-id-1 player-id-2 player-id-3]})

(defn as-graph
  "Converts a map of edges to cost to a 2d graph"
  [cs]
  (reduce (fn [g [[n1 n2] cost]]
            (-> g (assoc-in [n1 n2] cost) (assoc-in [n2 n1] cost)))
          {} cs))

(defn connections
  "Returns vector of player-ids with connections in city"
  [cities city]
  (get cities city []))

(defn num-connections
  "Returns the number of connections current in city"
  [cities city]
  (count (get cities city)))

(defn connection-cost
  "Returns price to add a connection in city."
  [cities city]
  (case (int (num-connections cities city))
    0 10, 1 15, 2 20))

(defn player-in-city?
  "Returns true if player owns a connection in city, otherwise false"
  [cities city player-id]
  (some #{player-id} (connections cities city)))

(defn add-connection
  "Returns cities after player builds next connection in city.
  Asserts that city has capacity for new connections and that player does not
  already occupy city"
  [cities city player-id]
  {:pre [(< (num-connections cities city) 3)
         (not (player-in-city? cities city player-id))]}
  (update-in cities [city] conj player-id))

(defn connection-cost
  [cities city player-id]
  ;; TODO IMPLEMENT
  0)
