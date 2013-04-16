(ns powergrid.auction
  (:import [clojure.lang PersistentQueue]))

(defrecord Auction [item player-id bidders price min-increment])

(def ^:private defaults
  {:min-increment 1
   :price 0})

(defn- queue
  [m]
  (if-not (instance? PersistentQueue m)
    (reduce conj PersistentQueue/EMPTY (or m '()))
    m))

(defn new-auction
  "Returns a new auction"
  [m]
  (let [auction (map->Auction (merge defaults m))
        bidders (:bidders auction )]
    (assoc auction :bidders (queue bidders))))

(defn current-bidder
  "Returns the current bidder"
  [auction]
  (peek (:bidders auction)))

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
