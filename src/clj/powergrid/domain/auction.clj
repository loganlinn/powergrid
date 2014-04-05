(ns ^:shared powergrid.domain.auction
  (:require [powergrid.domain.protocols :as pc]))

(defrecord Auction [item player-id bidders price min-increment])

(defn item [auction] (:item auction))
(defn player-id [auction] (:player-id auction))
(defn bidders [auction] (:bidders auction))
(defn price [auction] (:price auction))

(defn current-bidder
  "Returns the current bidder"
  [auction]
  (peek (:bidders auction)))

(defn min-bid
  "Returns the minimum bid for item currently at auction"
  [{:keys [price min-increment]}]
  (if price
    (+ price min-increment)
    0))

