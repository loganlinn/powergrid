(ns powergrid.player
  (:require [powergrid.power-plants :refer [is-hybrid? accepts-resource?]]
            [powergrid.resource :refer [ResourceTrader]]))


(defrecord Player [id ctx color money cities power-plants])

(def colors [:red :green :blue :yellow :purple :black])

(defn new-player
  "Returns new player"
  [id ctx color]
  (map->Player {:id id
                :ctx ctx
                :color color
                :money 50
                :cities #{}
                :power-plants {}}))

(defn player-key [player] (:id player))

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

(defn has-capacity?
  "Returns true if resources fits into player's capacities, otherwise false.
  resources is a map of resource to quantity."
  ([player resource amt]
   (has-capacity? player {resource amt}))
  ([player resources]
   (let [capacities (resource-capacities player)
         capacities (reduce (fn [c [r n]] (assoc c r (- (get c r 0) n)))
                            capacities resources)
         ;; Handle hybrids
         capacities (reduce
                      (fn [c [s cap]]
                        (let [[c* cap*]
                              (reduce
                                (fn [[c cap] r]
                                  (let [x (get c r)]
                                    (if (and (neg? x) (> cap x))
                                      [(assoc c r 0) (+ cap x)]
                                      [c cap])))
                                [c cap]
                                s)]
                          (assoc c* s cap*)))
                      capacities
                      (filter (comp set? key) capacities))]
     (not-any? neg? (vals capacities)))))


(declare distribute-resource)
(extend-type Player
  ResourceTrader
  (accept-resource [player resource amt]
    (update-in player [:power-plants] distribute-resource resource amt))
  (send-resource [player [power-plant resource] amt]
    {:pre [(owns-power-plant? player power-plant)
           (>= (get-in player [:power-plants power-plant resource]) amt)]}
    (update-in player [:power-plants power-plant resource] - amt)))

(defn- distribute-resource
  [power-plants resource amt]
  ;; TODO generalize this type of iteration?
  (loop [power-plants (sort-by is-hybrid? power-plants)
         [[plant inventory] & r] power-plants
         amt amt]
    (if (and plant (pos? amt))
      (let [space-left (if (accepts-resource? plant resource)
                         (- (* 2 (:capacity plant))
                            (get inventory resource 0))
                         0)
            amt-stored (min space-left amt)]
        (recur (assoc power-plants plant (update-in inventory resource + amt-stored))
               r
               (- amt amt-stored)))
      power-plants)))
