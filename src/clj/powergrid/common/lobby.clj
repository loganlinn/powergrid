(ns powergrid.common.lobby)

(defrecord Lobby [game-id seats country seats-closed])

(def max-seats 6)

(defn game-id [lobby] (:game-id lobby))
(defn country [lobby] (:country lobby))
(defn seats-closed [lobby] (:seats-closed lobby 0))
(defn seats [lobby] (:seats lobby))

(defn seats-total [lobby]
  (- max-seats (seats-closed lobby)))

(defn seats-open [lobby]
  (- max-seats (+ (seats-closed lobby)
                  (count (seats lobby)))))

(defn seats-open? [lobby]
  (pos? (seats-open lobby)))
