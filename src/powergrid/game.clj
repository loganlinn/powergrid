(ns powergrid.game
  (:require [powergrid.power-plants :as pp]
            [powergrid.player :as p]
            [powergrid.auction :as a]
            [powergrid.resource :refer [map->Resource]]
            [powergrid.util :refer [separate queue]]))

(defrecord Game [id phase step round resources power-plants players turns auction messages bank])

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

(defprotocol PlayersMap
  (players-map [this]))

(extend-protocol PlayersMap
  clojure.lang.IPersistentMap
  (players-map [this] this)
  clojure.lang.Seqable
  (players-map [this] (apply hash-map (mapcat (juxt p/id identity) this))))

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
              :messages {:in (queue)
                         :out (queue)}
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
  [game player-id f & args]
  (apply update-in game [:players player-id] f args))

(defn update-players
  "Returns game after updating players via (apply f players args)"
  [game f & args]
  (assoc game :players (players-map (apply f (players game) args))))

(defn purchase
  "Returns game after transferring amt Elektro from player to bank"
  [game player-id price]
  (-> game
      (update-player player-id p/update-money (- price))
      (update-in [:bank] (fnil + 0) price)))

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

(defn set-turns
  "Returns game after setting turns"
  [game turn-type]
  (if (turns-reverse-order? game)
    (reverse (keys (game :players)))
    (keys (game :players))))

(defn remove-turn
  "Removes turn from turns in game state"
  [game player-id]
  (update-in game [:turns] (partial remove #{player-id})))

;; AUCTIONING


(defn has-auction?  [game] (contains? game :auction))

(defn cleanup-auction [game] (dissoc game :auction))

(defn current-auction [game] (game :auction))

(defn set-auction [game auction] (assoc game :auction auction))

(defn init-power-plant-auction
  "Returns game after setting bidding in state"
  [game power-plant player-id starting-bid]
  (assoc game :auction
         (a/new-auction {:item power-plant
                         :player-id player-id
                         :price starting-bid
                         :bidders (remove #{player-id} (game :turns))})))
(defn auction-needed?
  "Returns true if a auction is necessary to buy power-plant"
  [game]
  (turns-remain? game))

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
  (update-in game [:messages :in] conj msg))

(defn reserve-message
  "Returns [game msg] where msg is next message in queue (or nil if empty) and
  game has had the message removed from msg queue."
  [game]
  [(update-in game [:messages :in] pop) (peek (get-in game [:messages :in]))])

(defn send-message
  [game msg]
  (update-in game [:messages :out] conj msg))

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
      (update-power-plant-market game :future conj power-plant)
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

