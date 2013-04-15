(ns powergrid.game
  (:require [powergrid.power-plants :refer [power-plant-cards
                                            power-plant-number]]
            [powergrid.player :as p]
            [powergrid.resource :refer [map->Resource]]
            [powergrid.util :refer [separate]]))

(defrecord Game [id phase step round resources power-plants players turns bank])

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

(defn init-resources
  []
  (let [std-pricing (for [p (range 1 9) _ (range 3)] p)
        uranium-pricing '(1 2 3 4 5 6 7 8 12 14 15 16)]
    {:coal (map->Resource {:market 24 :supply 0 :pricing std-pricing})
     :oil  (map->Resource {:market 18 :supply 6 :pricing std-pricing})
     :garbage (map->Resource {:market 6 :supply 18 :pricing std-pricing})
     :uranium (map->Resource {:market 2 :supply 10 :pricing uranium-pricing})}))

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

(defn current-step  [game] (:step game))
(defn current-phase [game] (:phase game))
(defn current-round [game] (:round game))

(defn inc-phase
  [game]
  (update-in game [:phase] inc))

(defn inc-step
  [game]
  (update-in game [:step] inc))

(defn inc-round
  [game]
  (update-in game [:round] inc))

(defn players
  [game]
  (vals (:players game {})))

(defn num-players
  [game]
  (count (:players game)))

(defn turns-remain?
  "Returns true if turns still exist in phase, otherwise false."
  [game]
  (boolean (seq (:turns game))))

(defn resource-market
  "Returns current resource market"
  [game]
  (get-in game [:resources :market]))

(defn set-resource-market
  "Updates current resource market in game"
  [game resource-market]
  (assoc-in game [:resources :market] resource-market))

(defn resource-supply
  "Returns resource supply"
  [game]
  (get-in game [:resources :supply]))
