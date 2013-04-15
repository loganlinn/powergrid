(ns powergrid.game
  (:require [powergrid.power-plants :refer [power-plant-cards
                                            power-plant-number]]
            [powergrid.player :as p]
            [powergrid.util :refer [separate]]))

(defrecord Game [id phase step round resources power-plants players turns bank])
(defrecord Resource [market supply pricing])

(defn num-regions-chosen
  "Returns the number of regions chosen on map"
  [num-players]
  (case (int num-players)
    (2, 3) 3
    4 4
    (5, 6) 5))

(defn num-rand-removed-power-plants
  "Returns the number of randomly removed power plants after preparing the
  power plant market"
  [num-players]
  (case (int num-players)
    (2, 3) 8
    4 4
    (5, 6) 0))

(defn num-cities-trigger-step-2
  "Returns the number of connected cities needed to trigger step 2"
  [num-players]
  (case (int num-players)
    2 10
    (3, 4, 5) 7
    6 6))

(defn max-power-plants
  "Returns the max number of player plants a player can have"
  [num-players]
  (case (int num-players)
    2 4
    (3, 4, 5, 6) 3))

(defn num-cities-trigger-end
  "Returns the number of connected cities to trigger game end"
  [num-players]
  (case (int num-players)
    2 21
    (3, 4) 17
    5 15
    6 14))

(def step-3-card (constantly :step-3))
(defn step-3-card? [card] (= :step-3 card))

(defn resource-price
  "Returns the price of purchasing amt of resource in current market.
  Asserts that amt is not larger than currently available in market."
  [{:keys [market pricing] :as resource} amt]
  {:pre [(>= market amt)]}
  (let [unavail (- (count pricing) market)]
    (apply + (take amt (drop unavail pricing)))))

(defn init-resources
  []
  (let [std-pricing (for [p (range 1 9) _ (range 3)] p)
        uranium-pricing '(1 2 3 4 5 6 7 8 12 14 15 16)]
    {:coal (->Resource 24 0 std-pricing)
     :oil  (->Resource 18 6 std-pricing)
     :garbage (->Resource 6 18 std-pricing)
     :uranium (->Resource 2 10 uranium-pricing)}))

(defn init-power-plant-deck
  [power-plants num-players]
  (let [[card-13 deck] (separate #(= (power-plant-number %) 13) power-plants)
        recombine #(concat card-13 % [(step-3-card)])]
    (->> deck
      (shuffle)
      (drop (num-rand-removed-power-plants num-players))
      (recombine))))

(defn init-power-plants
  [num-players]
  {:market (take 4 power-plant-cards)
   :future (take 4 (drop 4 power-plant-cards))
   :deck (init-power-plant-deck (drop 8 power-plant-cards) num-players)})

(defn players-map
  [players]
  (apply hash-map (mapcat (juxt p/player-key identity) players)))

(defn new-game
  "Returns new Game for vector of players"
  [players]
  (map->Game {:id (str (java.util.UUID/randomUUID))
              :phase 1
              :step 1
              :round 1
              :resources (init-resources)
              :power-plants (init-power-plants (count players))
              :players (players-map players)
              :turns []
              :bank 0}))

(defn inc-phase
  [state]
  (update-in state [:phase] inc))

(defn inc-step
  [state]
  (update-in state [:step] inc))

(defn inc-round
  [state]
  (update-in state [:round] inc))

(defn players
  [state]
  (vals (:players state {})))

(defn num-players
  [state]
  (count (:players state)))

(defn turns-remain?
  "Returns true if turns still exist in phase, otherwise false."
  [state]
  (boolean (seq (:turns state))))

(defn resource-market
  "Returns current resource market"
  [state]
  (get-in state [:resources :market]))

(defn set-resource-market
  "Updates current resource market in state"
  [state resource-market]
  (assoc-in state [:resources :market] resource-market))

(defn resource-supply
  "Returns resource supply"
  [state]
  (get-in state [:resources :supply]))

