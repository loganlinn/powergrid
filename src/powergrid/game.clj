(ns powergrid.game
  (:require [powergrid.power-plants :as pp]
            [powergrid.player :as p]
            [powergrid.resource :refer [map->Resource]]
            [powergrid.util :refer [separate]]))

(defrecord Game [id phase step round resources power-plants players turns messages bank])

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

(defn turns-reverse-order?
  "Returns true if the current phase uses reverse player order, otherwise false"
  [{:keys [phase]}]
  (or (= 4 phase) (= 5 phase)))

(def step-3-card :step-3)
(defn step-3-card? [card] (= step-3-card card))

(defn init-resources []
  (let [std-pricing (for [p (range 1 9) _ (range 3)] p)
        uranium-pricing '(1 2 3 4 5 6 7 8 12 14 15 16)]
    {:coal (map->Resource {:market 24 :supply 0 :pricing std-pricing})
     :oil  (map->Resource {:market 18 :supply 6 :pricing std-pricing})
     :garbage (map->Resource {:market 6 :supply 18 :pricing std-pricing})
     :uranium (map->Resource {:market 2 :supply 10 :pricing uranium-pricing})}))

(defn- init-power-plant-deck
  [power-plants num-players]
  (let [[card-13 deck] (separate #(= (pp/plant-number %) 13) power-plants)
        recombine #(concat card-13 % [step-3-card])]
    (->> deck
      (shuffle)
      (drop (num-rand-removed-power-plants num-players))
      (recombine))))

(defn init-power-plants
  [num-players]
  {:market (pp/initial-market)
   :future (pp/initial-future)
   :deck (init-power-plant-deck (pp/initial-deck) num-players)})

(defprotocol PlayersMap
  (players-map [this]))

(extend-protocol PlayersMap
  clojure.lang.IPersistentMap
  (players-map [this] this)
  clojure.lang.Seqable
  (players-map [this] (apply hash-map (mapcat (juxt p/player-key identity) this))))

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
              :turns  '()
              :messages clojure.lang.PersistentQueue/EMPTY
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

;; PLAYERS

(defn players
  "Returns players"
  [game & {:keys [except]}]
  (let [ps (:players game {})]
    (if except
      (keep #(when-not (= except (key %)) (val %)) ps)
      (vals ps))))

(defn player
  "Returns player by id if exists, otherwise nil"
  [game id]
  (get-in [:players id]))

(defn num-players
  [game]
  (count (:players game)))

(defn color-taken?
  "Returns true if a player in game is using color"
  [game ^clojure.lang.Keyword color]
  (let [taken-colors (set (map p/color (players game)))]
    (contains? color taken-colors)))

(defn update-player
  "Returns game after updating player via (apply f player args)"
  [game player-key f & args]
  (apply update-in game [:players player-key] f args))

(defn update-players
  "Returns game after updating players via (apply f players args)"
  [game f & args]
  (assoc game :players (players-map (apply f (players game) args))))

;; TURNS

(defn turns [game] (game :turns))

(defn turns-remain?
  "Returns true if turns still exist in phase, otherwise false."
  [game]
  (boolean (seq (:turns game))))

(defn clear-turns
  "Clears turns in game"
  [game]
  (assoc game :turns nil))

(defn set-turns
  "Returns game after setting turns"
  [game turn-type]
  (if (turns-reverse-order? game)
    (reverse (keys (game :players)))
    (keys (game :players))))

(defn reserve-turn
  "Returns [updated-game turn] where turn was removed from turn queue
  of updated-game"
  [game]
  [(update-in game [:turns] rest) (first (game :turns))])

(defn remove-turn
  "Removes turn from turns in game state"
  [game turn]
  (update-in game [:turns] (partial remove #(= turn %))))

;; AUCTIONING

(defn has-auction?  [game] (contains? game :auction))
(defn cleanup-auction [game] (dissoc game :auction))

(defn set-power-plant-auction
  "Returns game after setting bidding in state" ;; TODO is player-id in turns?
  [game power-plant player-id starting-bid]
  (assoc game :auction {:plant power-plant
                        :player-id player-id ;; highest bidder
                        :price starting-bid
                        :turns (remove #(= player-id %) (game :turns))}))

(defn auction-complete?
  "Returns true if current auction has completed"
  [game]
  (empty? (get-in game [:auction :turns])))

(defn reserve-bidder
  "Returns [updated-game bidder] where bidder was removed from turn queue
  of updated-game"
  [game]
  [(update-in game [:auction :turns] rest)
   (first (get-in game [:auction :turns]))])

;; RESOURCES

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

(defn max-network-size
  "Returns the maximum number of cities a single player has built"
  [game]
  (apply max (map p/network-size (players game))))

(defn update-resource
  "Returns game after updating resource via (apply f resource args)"
  [game resource f & args]
  (apply update-in game [:resources resource] f args))

(defn receive-message
  [game msg]
  (update-in game [:messages] conj msg))

(defn reserve-message
  "Returns [game msg] where msg is next message in queue (or nil if empty) and 
  game has had the message removed from msg queue."
  [game]
  [(update-in game [:messages] pop) (peek (game :messages))])

;; POWER PLANTS

(defn valid-power-plant-market?
  [market]
  (or (= :market market) (= :future market)))

(defn update-power-plants
  "Returns game after updating power-plant markets via (apply f power-plants args)"
  [game f & args]
  (apply update-in game [:power-plants] f args))

(defn update-power-plant-market
  "Returns game after updating power-plant market via (apply f power-plant-market args)"
  ([game market f & args]
   {:pre [(valid-power-plant-market? market)]}
   (apply update-in game [:power-plants market] f args)))

(defn remove-power-plant
  "Returns game after removing power-plant from the current power-plant market"
  ([game power-plant market]
   {:pre [(valid-power-plant-market? market)]}
   (update-in game [:power-plants market] (partial remove #(= % power-plant))))
  ([game power-plant]
   (remove-power-plant game power-plant :market)))

(defn drop-lowest-power-plant
  "Removes lowest power-plant from market. Assumes power-plant market is in
  order. Note, no replacement is drawn."
  [game]
  (update-in game [:power-plants :market] rest))
