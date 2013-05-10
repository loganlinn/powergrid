(ns powergrid.auction
  (:require [powergrid.common.auction :as common]
            [powergrid.util :refer [queue]]))

(def ->Auction common/->Auction)
(def map->Auction common/map->Auction)
(def current-bidder common/current-bidder)
(def min-bid common/min-bid)

(def ^:private defaults
  {:min-increment 1})

(defn new-auction
  "Returns a new auction"
  ([m]
   {:pre [(contains? m :item)]}
   (let [auction (map->Auction (merge defaults m))
         bidders (:bidders auction )]
     (assoc auction :bidders (queue bidders))))
  ([m initial-min-bid]
   (let [auction (new-auction m)]
     (assoc auction :price (- initial-min-bid (:min-increment auction))))))

(defn completed?
  "Returns true if the auction is in a completed state, otherwise false"
  [{:keys [bidders player-id]}]
  (or (empty? bidders)
      (and (= (count bidders) 1)
           (= (first bidders) player-id))))

(defn pass
  "Returns updated auction after passing for current bidder"
  [auction]
  (when auction
    (update-in auction [:bidders] pop)))

(defn bid
  "Returns updated auction after accepting bid from current bidder (f"
  [{:keys [bidders] :as auction} player-id bid]
  {:pre [(>= bid (min-bid auction))]}
  (assoc auction
         :price bid
         :player-id player-id
         :bidders (conj (pop bidders) (peek bidders))))
