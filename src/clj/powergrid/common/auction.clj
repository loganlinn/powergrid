(ns powergrid.common.auction)

(defrecord Auction [item player-id bidders price min-increment])

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

