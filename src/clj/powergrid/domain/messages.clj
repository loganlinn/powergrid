(ns ^:shared powergrid.domain.messages
  (:require [powergrid.common.protocols :refer [Labeled label]]
            [powergrid.common.power-plants :as pp]
            [powergrid.common.game :as g]
            [powergrid.common.auction :as a]
            [powergrid.common.cities :as c]
            [powergrid.domain.messages :as msg]
            [powergrid.domain.phase5 :as phase5]
            [clojure.string :as str])
  (:refer-clojure :exclude [type]))

(def topic :topic)
(def type :type)
(def pass ::pass)
(defn is-pass? [msg] (pass msg))

;; Phase 2

(defrecord BidPowerPlantMessage [player-id plant-id bid]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
      (if (is-pass? this)
        (if-let [auction (g/auction game)]
          (format "%s passes bidding on %s." player-label (label (a/item auction)))
          (format "%s passes on power plants." player-label))
        (format "%s bids %d on %s." player-label bid (label (pp/plant plant-id)))))))

(defrecord DiscardPowerPlantMessage [player-id plant-id]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))
          pp-label (label (pp/plant plant-id))]
      (format "%s discarded %s"))))

;; Phase 3

(defn- label-resources [resources]
  (str/join ", " (for [[r amt] resources] (str amt " " (name r)))))

(defrecord BuyResourcesMessage [player-id resources]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
      (if (msg/is-pass? this)
        (format "%s passes on buying resources." player-label)
        (let [rlabels (map #(str (val %) " " (name (key %))) resources)]
          (format "%s buys %s." player-label (str/join ", " rlabels)))))))

;; Phase 4

(defrecord BuyCitiesMessage [player-id new-cities]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))]
     (if (msg/is-pass? this)
      (format "%s passes on building cities." player-label)
      (format "%s built in %s." player-label (str/join ", " (map name new-cities)))))))

;; Phase 5

(defrecord PowerCitiesMessage [player-id powered-plants]
  Labeled
  (label [this game]
    (let [player-label (label (g/player game player-id))
          player-network-size (c/network-size (g/cities game) player-id)]
     (if (msg/is-pass? this)
      (format "%s passes on powering cities." player-label)
      (format "%s powers %d %s, earns $%d."
              player-label
              (count powered-plants)
              (if (= 1 (count powered-plants)) "city" "cities")
              (phase5/total-payout player-network-size powered-plants))))))
