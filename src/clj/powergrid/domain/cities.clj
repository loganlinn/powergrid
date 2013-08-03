(ns ^:shared powergrid.domain.cities)

(defrecord Cities [owners connections])

(defn owners [cities] (:owners cities))

(defn connections [cities] (:connections cities))

(defn valid-city?
  "Returns true if city is a valid city within cities, otherwise false"
  [cities city]
  (contains? (connections cities) city))

(defn owned-cities
  "Returns collection of cities the player owns"
  [cities player-id]
  (keep #(when (some #{player-id} (val %)) (key %)) (owners cities)))

(defn network-size
  "Returns number of cities a player owns"
  [cities player-id]
  (reduce #(if (some #{player-id} %2) (inc %1) %1) 0 (vals (owners cities))))

(defn network-sizes
  "Returns map of player-id to number of cities they own (for the players who own cities)"
  [cities]
  (frequencies (flatten (vals (owners cities)))))
