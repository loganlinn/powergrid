(ns powergrid.player
  (:require [powergrid.power-plants :as pp]
            [powergrid.resource :refer [ResourceTrader]]))

(defrecord Player [id ctx color money cities power-plants])

(def colors #{:red :green :blue :yellow :purple :black})

(defn new-player
  "Returns new player"
  [id ctx color]
  (map->Player {:id id
                :ctx ctx
                :color color
                :money 50
                :cities #{}
                :power-plants {}}))

(defn id [player] (:id player))

(def color :color)

(defn valid-color? [c] (contains? colors c))

(defn set-color
  [player color]
  {:pre [(valid-color? color)]}
  (assoc player :color color))

(defn network-size
  "Returns the number of cities in the player's network"
  [player]
  ;; TODO REMOVE, need to ask cities for this
  (count (:cities player)))

(defn power-plants
  "Returns the power-plants owned by player"
  [player]
  (keys (:power-plants player)))

(defn power-plant-resources
  "Returns a map of resource-type to amt on player's power-plant."
  [player power-plant]
  (get-in player [:power-plants power-plant] {}))

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
  (some #(pp/accepts-resource? % resource) (power-plants player)))

(defn add-power-plant
  "Returns updated player after adding power-plant"
  [player power-plant]
  (assoc-in player [:power-plants power-plant] {}))

(defn owns-power-plant?
  "Returns true if the player owns power-plant, otherwise false"
  [player power-plant]
  (contains? (:power-plants player) power-plant))

(defn can-power-plant?
  "Returns true if player has sufficient resources on plant to power it, if any
  are needed. Will always return true for green power-plants, assuming player owns it"
  [player power-plant]
  (when (owns-power-plant? player power-plant)
    (or (not (pp/consumes-resources? power-plant))
        (>= (reduce + (vals (power-plant-resources player power-plant)))
            (pp/capacity power-plant)))))

(defn add-power-plant-resources
  "Returns updated player after storing resource in power plant.
  Asserts that power-plant accepts resource and player owns it."
  [player power-plant resource amount]
  {:pre [(pp/accepts-resource? power-plant resource)
         (owns-power-plant? player power-plant)]}
  (update-in player
             [:power-plants power-plant resource]
             (fnil #(+ % amount) 0)))

(defn resource-capacities
  "Returns map from resource to amount of remaining capacity for player based on
  the power plants he owns and current resources"
  [player]
  (reduce
    (fn [m [power-plant utilization]]
      (let [avail-cap (- (pp/max-capacity power-plant)
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
    (add-power-plant-resources player power-plant resource (- amt))))

(defn- distribute-resource
  [power-plants resource amt]
  ;; TODO generalize this type of iteration?
  (loop [power-plants power-plants
         [[plant inventory] & r] (sort-by (comp pp/is-hybrid? key) power-plants)
         amt amt]
    (if (and plant (pos? amt))
      (let [space-left (if (pp/accepts-resource? plant resource)
                         (- (pp/max-capacity plant)
                            (get inventory resource 0))
                         0)
            amt-stored (min space-left amt)]
        (recur (assoc power-plants plant
                      (update-in inventory [resource] (fnil + 0) amt-stored))
               r
               (- amt amt-stored)))
      power-plants)))
