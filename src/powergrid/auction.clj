(ns powergrid.auction
  (:import [clojure.lang PersistentQueue]))

(defrecord Auction [item player-id bidders price min-increment])

(def ^:private defaults
  {:min-increment 1
   :price 0})

(defn- queue
  [m]
  (if (isa? PersistentQueue)
    m
    (reduce conj PersistentQueue/EMPTY m)))

(defn new-auction
  "Returns a new auction"
  [m]
  (let [auction (map->Auction (merge defaults m))
        bidders (auction :bidders)]
    (assoc auction :bidders (queue bidders))))

(defn bidders-remain?
  "Returns true if current auction has completed"
  [auction]
  (empty? (:bidders auction)))

(defn min-bid
  "Returns the minimum bid for item currently at auction"
  [{:keys [price min-increment]}]
  (+ price min-increment))

(defn completed?
  "Returns true if the auction is in a completed state, otherwise false"
  [{:keys [bidders player-id]}]
  (or (empty? bidders)
      (and (= (count bidders) 1)
           (= (first bidders) player-id))))

(defn accept-bid
  "Returns updated auction after accepting bid from current bidder (f"
  [{:keys [bidders] :as auction} player-id bid]
  {:pre [(>= bid (min-bid auction))
         (= player-id (peek bidders))]}
  (-> auction
      (assoc :price bid)
      (assoc :player-id player-id)
      (assoc :bidders (conj (pop bidders) (peek bidders)))))
