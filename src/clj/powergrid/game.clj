(ns powergrid.game
  (:require [powergrid.common.power-plants :as pp]
            [powergrid.common.player :as p]
            [powergrid.auction :as a]
            [powergrid.common.resource :as r]
            [powergrid.cities :as c]
            [powergrid.cities.usa :as usa]
            [powergrid.util :refer [separate queue]]
            [robert.hooke :as hook]))

(defrecord Game [id phase step round turns turn-order resources power-plants cities players auction bank])

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
  (or (= 3 phase) (= 4 phase)))

(def step-3-card :step-3)
(defn step-3-card? [card] (= step-3-card card))

(defn init-resources []
  (let [std-pricing (for [p (range 1 9) _ (range 3)] p)
        uranium-pricing '(1 2 3 4 5 6 7 8 12 14 15 16)]
    {:coal (r/map->Resource {:market 24 :supply 0 :pricing std-pricing})
     :oil  (r/map->Resource {:market 18 :supply 6 :pricing std-pricing})
     :garbage (r/map->Resource {:market 6 :supply 18 :pricing std-pricing})
     :uranium (r/map->Resource {:market 2 :supply 10 :pricing uranium-pricing})}))

(defn- init-power-plant-deck
  [power-plants num-players]
  (let [[card-13 deck] (separate #{(pp/plant 13)} power-plants)
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

(defn new-game
  "Returns new Game for vector of players"
  [players]
  (map->Game {:id (str (java.util.UUID/randomUUID))
              :phase 1
              :step 1
              :round 1
              :resources (init-resources)
              :power-plants (init-power-plants (count players))
              :players (into {} (map (juxt p/id identity) players))
              :turns  '()
              :turn-order (shuffle (map p/id players))
              :cities (c/map->Cities {:owners (zipmap usa/cities (repeat []))
                                      :connections (c/as-graph usa/connections)})
              :bank 0}))

(defn current-step  [game] (:step game))
(defn current-phase [game] (:phase game))
(defn current-round [game] (:round game))

(defn inc-phase
  [game]
  (update-in game [:phase] #(if (< % 5) (inc %) 1)))

(defn inc-step
  [game]
  {:pre [(< (current-step game) 3)]}
  (update-in game [:step] inc))

(defn inc-round
  [game]
  (update-in game [:round] inc))

;; PLAYERS

(defn players-map
  "Returns map of player-id to player from game"
  [game]
  (:players game))

(defn players
  "Returns players"
  [game & {:keys [except]}]
  (let [ps (:players game {})]
    (if except
      (keep #(when-not (= except (key %)) (val %)) ps)
      (vals ps))))

(defn map-players
  "Returns a mapping from player-id to result of (f player)"
  [game f]
  (into {} (for [p (players game)] [(p/id p) (f p)])))

(defn player
  "Returns player by id if exists, otherwise nil"
  [game id]
  (get-in game [:players id]))

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
  [game player-id f & args]
  (apply update-in game [:players player-id] f args))

(defn transfer-money
  "Returns game after transfering money between player and bank. Second argument
  specifies whether transfer is to or from player"
  [game dir player-id amt]
  {:pre [(or (= dir :to) (= dir :from))]}
  (let [amt (if (= dir :from) (- amt) amt)]
    (-> game
        (update-player player-id p/update-money (+ amt))
        (update-in [:bank] (fnil - 0) amt))))

(declare network-size)

(defn player-id-order
  "Returns player ids sorted using the following rules:
  First player is player with most cities in network. If two or more players
  are tied for the most number of cities, if the first player is the player
  among them with the largest-numbered power plant. Determine remaining player
  order using same rules"
  [game]
  (let [c (juxt #(network-size game (p/id %)) p/max-power-plant p/id)]
    (map p/id (sort #(compare (c %2) (c %1)) (players game)))))

;; TURNS

(defn turns [game] (:turns game))

(defn current-turn
  "Returns id of player who's turn it currently is"
  [game]
  (first (turns game)))

(defn turns-remain?
  "Returns true if turns still exist in phase, otherwise false."
  [game]
  (boolean (seq (:turns game))))

(defn clear-turns
  "Clears turns in game"
  [game]
  (assoc game :turns nil))

(defn update-turn-order
  "Returns game after updating turn-order according to rules"
  [game]
  (assoc game :turn-order (player-id-order game)))

(defn reset-turns
  "Returns game after setting turns to current turn-order.
  Note: turn order is not (re)calculated."
  [{:keys [turn-order] :as game}]
  (assoc game :turns (if (turns-reverse-order? game)
                       (reverse turn-order)
                       turn-order)))

(defn remove-turn
  "Removes turn from turns in game state"
  [game player-id]
  (update-in game [:turns] (partial remove #{player-id})))

(defn advance-turns
  "Removes the current player from the turns list"
  [game]
  (update-in game [:turns] rest))

;; AUCTIONING


(defn has-auction?  [game] (:auction game))

(defn cleanup-auction [game] (dissoc game :auction))

(defn current-auction [game] (:auction game))

(defn set-auction [game auction] (assoc game :auction auction))

(defn auction-needed?
  "Returns true if a auction is necessary to buy power-plant"
  [game]
  (turns-remain? game))

;; RESOURCES

(defn map-resources
  "Returns mapping from resources to result of (f resource)"
  [game f]
  (into {} (for [[t r] (:resources game)] [t (f r)])))

(defn resource
  [game resource]
  {:pre [(r/types resource)]}
  (get-in game [:resources resource]))

(defn contains-resource?
  "Returns true if there is at least amt of resource in the resource market"
  ([game rtype amt]
   {:pre [(not (neg? amt))]}
   (>= (:market (resource game rtype) 0) amt)))

(defn contains-resources?
  [game resources]
  (every? #(contains-resource? game (key %) (val %)) resources))

(defn resource-supply
  "Returns map of resource to amount left in supply"
  [{:keys [resources]}]
  (zipmap (keys resources) (map :supply (vals resources))))

(defn update-resource
  "Returns game after updating resource via (apply f resource args)"
  [game resource f & args]
  (apply update-in game [:resources resource] f args))

;; POWER PLANTS

(defn valid-power-plant-market?
  [market]
  (or (= :market market) (= :future market)))

(defn power-plants
  "Returns the current or future power plant market"
  ([game]
   (power-plants game :market))
  ([game market]
   {:pre [(valid-power-plant-market? market)]}
   (get-in game [:power-plants market])))

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
   (update-in game [:power-plants market] (partial remove #{power-plant})))
  ([game power-plant]
   (remove-power-plant game power-plant :market)))

(defn drop-lowest-power-plant
  "Removes lowest power-plant from market. Assumes power-plant market is in
  order. Note, no replacement is drawn."
  [game]
  (update-in game [:power-plants :market] rest))

(defn power-plant-buyable?
  [game power-plant]
  (some #{power-plant} (power-plants game :market)))

;; TODO move to powergrid.power_plants
(defn power-plant-order
  "Returns power-plants after re-ordering"
  [{:keys [market future] :as power-plants} step]
  (let [[step-3-card combined] (separate (complement step-3-card?) (concat market future))
        ordered (sort-by :number combined)
        [market future] (split-at (if (= step 3) 6 4) ordered)]
    (assoc power-plants
           :market market
           :future (concat future step-3-card))))

(defn update-power-plant-order
  "Returns game after ordering the power-plants"
  [game]
  (update-power-plants game power-plant-order (current-step game)))

(defn add-to-power-plant-market
  "Returns game after adding power-plant to the power plant market and
  re-ordering"
  [game power-plant]
  (-> game
      (update-power-plant-market :future conj power-plant)
      (update-power-plant-order)))

(defn- handle-step-3-card
  "Returns game after handling the Step 3 card"
  [{:keys [phase] :as game} step-3-card]
  (let [game (-> game
                 (update-in [:power-plants :deck] shuffle)
                 (assoc :step-3-card? true))]
    (if (= phase 2)
      (add-to-power-plant-market step-3-card)
      (-> game
          (drop-lowest-power-plant)
          (update-power-plant-order)))))

(defn draw-power-plant
  "Returns game after moving card from power-plant deck to market and
  re-ordering"
  [game]
  (let [[draw & deck] (get-in game [:power-plants :deck])]
    (if (step-3-card? draw)
      (-> game
          (assoc-in [:power-plants :deck] deck)
          (handle-step-3-card draw))
      (-> game
          (assoc-in [:power-plants :deck] deck)
          (add-to-power-plant-market draw)))))

;; CITIES

(defn cities
  "Returns cities for game"
  [game]
  (:cities game))

(defn network-size
  "Returns the number of cities player owns"
  [game player-id]
  (c/network-size (cities game) player-id))

(defn max-network-size
  "Returns the maximum number of cities a single player has built"
  [game]
  (if-let [sizes (seq (vals (c/network-sizes (cities game))))]
   (apply max sizes)
    0))

(defn max-city-connections
  "Returns the maximum number of connections allowed in a city based on current
  step of game"
  [game]
  (current-step game))

(defn update-cities
  "Returns game after updating cities via (apply f cities args)"
  [game f & args]
  (apply update-in game [:cities] f args))