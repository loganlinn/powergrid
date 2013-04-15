(ns powergrid.player
  (:require [powergrid.power-plants :refer [accepts-resource?]]))

(defn network-size
  "Returns the number of cities in the player's network"
  [player]
  (count (:cities player)))

(defn power-plants
  "Returns the power-plants owned by player"
  [player]
  (keys (:power-plants player)))

(defn max-power-plant
  "Returns the highest power-plant number the player owns"
  [player]
  (when-let [players-plants (power-plants player)]
    (apply max (map :number players-plants))))

(defn update-money
  "Updates player's money by amt"
  [player amt]
  (assoc player :money (+ (:money player 0) amt)))

(defn money
  "Returns amount of money the player currently has"
  [player]
  (:money player 0))

(defn can-afford?
  "Returns true if player can afford amt, otherwise false"
  [player amt]
  (>= (money player) amt))

(defn can-buy-resource?
  "Returns true if player can buy resource type, otherwise false. User must own
  power plant that accepts the resource"
  [player resource]
  (some #(accepts-resource? % resource) (power-plants player)))

(defn add-power-plant
  "Returns updated player after adding power-plant"
  [player power-plant]
  (assoc-in player [:power-plants power-plant] {}))

(defn owns-city?
  "Returns true if the player owns city, otherwise false"
  [player city]
  (contains? (:cities player) city))

(defn owns-power-plant?
  "Returns true if the player owns power-plant, otherwise false"
  [player power-plant]
  (contains? (:power-plants) power-plant))

(defn assign-resource
  "Returns updated player after storing resource in power plant.
  Asserts that power-plant accepts resource and player owns it."
  [player power-plant resource amount]
  {:pre [(accepts-resource? power-plant resource)
         (owns-power-plant? player power-plant)]}
  (update-in player
             [:power-plants power-plant resource]
             (fnil #(+ % amount) 0)))

(defn add-city
  "Returns updated player after adding city"
  [player city]
  (update-in player [:cities] conj city))

(defn resource-capacities
  "Returns map from resource to amount of remaining capacity for player based on
  the power plants he owns and current resources"
  [player]
  (reduce
    (fn [m [power-plant utilization]]
      (let [avail-cap (- (* 2 (:capacity power-plant))
                         (apply + (vals utilization)))]
        (update-in m [(:resource power-plant)]
                   (fnil #(+ avail-cap %) 0))))
    {}
    (:power-plants player)))

