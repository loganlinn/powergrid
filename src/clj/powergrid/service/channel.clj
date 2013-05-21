(ns powergrid.service.channel
  (:require [org.httpkit.server :as s]
            [powergrid.util.log :refer [debug]]
            [powergrid.util :refer [dissoc-in]]))

(def channels (atom {})) ; keyed by [game-id player-id]

(add-watch channels :debug (fn [k r old-val new-val] (debug :channels new-val)))

(defn player-channel [game-id player-id]
  (get-in @channels [game-id player-id]))

(defn player-ids-online [game-id]
  (keys (get @channels game-id)))

(defn game-channels [game-id]
  (vals (get @channels game-id)))

(defn cleanup [game-id player-id]
  (when (and game-id player-id)
    (swap! channels dissoc-in [game-id player-id])))

(defn setup [channel game-id player-id]
  (swap! channels assoc-in [game-id player-id] channel))

(defn send-msg! [channel msg] (s/send! channel (pr-str msg)))

(defn send-error! [channel err-msg] (send-msg! channel {:error err-msg}))

(defn broadcast-msg!
  "Sends message to all channels associated with game"
  [game-id msg]
  (doseq [channel (game-channels game-id)]
    (send-msg! channel msg)))
