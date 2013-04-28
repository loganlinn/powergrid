(ns powergrid.message
  (:require [powergrid.game :as g]
            [slingshot.slingshot :refer [throw+]]))

(def topic :topic)
(def title :title)

(defprotocol Message
  (validate [msg game] "Retruns string of validation message if msg is invalid, otherwise nil")
  (update-game [update game])
  (passable? [this game] "Returns true if passing is allowed")
  (pass [this game] "Returns (modified) game from passing msg"))

(extend-protocol Message
  clojure.lang.IPersistentMap
  (validate [_ _] nil)
  (update-game [_ game] game)
  (passable? [_ _] false)
  (pass [_ game] game))

(defn base-validate
  [{:keys [player-id] :as msg} game]
  ;; TODO ensure msg is authorized (check users match)
  (cond
    (not (g/player player-id)) "Invalid player"
    (= player-id (g/current-turn game)) "Not your turn"))
