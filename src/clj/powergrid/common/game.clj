(ns ^:shared powergrid.common.game
  (:require [powergrid.common.power-plants :as pp]
            [powergrid.common.player :as p]
            [powergrid.common.auction :as a]
            [powergrid.common.cities :as c]))

(defrecord Game [id phase step round turns turn-order resources power-plants cities players auction bank])

(defn id [game] (:id game))
(defn current-step  [game] (:step game))
(defn current-phase [game] (:phase game))
(defn current-round [game] (:round game))
(defn turns [game] (:turns game))
(defn turn-order [game] (:turn-order game))
(defn resources [game] (:resources game))
;(defn power-plants [game] (:power-plants game))
(defn cities [game] (:cities game))
;(defn players [game] (:players game))
(defn auction [game] (:auction game))
(defn bank [game] (:bank game))

;; PLAYERS

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

;; TURNS

(defn current-turn
  "Returns id of player who's turn it currently is"
  [game]
  (first (turns game)))

(defn ^boolean turns-remain?
  "Returns true if turns still exist in phase, otherwise false."
  [game]
  (boolean (seq (:turns game))))

;; AUCTIONING

(defn ^boolean has-auction? [game] (boolean (auction game)))

;; RESOURCES

(defn resource [game resource] (get-in game [:resources resource]))

(defn map-resources
  "Returns mapping from resources to result of (f resource)"
  [game f]
  (into {} (for [[t r] (:resources game)] [t (f r)])))

(defn ^boolean contains-resource?
  "Returns true if there is at least amt of resource in the resource market"
  ([game rtype amt]
   {:pre [(not (neg? amt))]}
   (>= (:market (resource game rtype) 0) amt)))

(defn ^boolean contains-resources?
  "Returns true if game's resource market has every {resource amt} pair in
  resources, otherwise false"
  [game resources]
  (every? #(contains-resource? game (key %) (val %)) resources))

;; CITIES

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

;; MISC

(defn action-player-id
  "Returns player id who has current game action"
  [game]
  (if-let [a (auction game)]
    (a/current-bidder a)
    (current-turn game)))

(defn phase-title
  [phase]
  (case phase
    1 "Determine Player Order"
    2 "Auction Power Plants"
    3 "Buying Resources"
    4 "Building"
    5 "Bureaucracy"))
