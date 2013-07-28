(ns powergrid.service.channel
  (:require [org.httpkit.server :as s]
            [powergrid.util.log :refer [debug]]
            [powergrid.util :refer [dissoc-in]]))

(defn send-msg!
  [channel msg]
  (s/send! channel (pr-str msg)))

(defn send-error!
  [channel err-msg]
  (send-msg! channel {:error err-msg}))

(defn player-channel
  [channels game-id player-id]
  (get-in @channels [game-id player-id]))

(defn game-channels
  "Returns map of player-id to channel for game"
  [channels game-id]
  (get @channels game-id {}))

(defn cleanup
  [channels game-id player-id]
  (when (and game-id player-id)
    (swap! channels dissoc-in [game-id player-id])))

(defn setup
  [channels channel game-id player-id]
  (swap! channels assoc-in [game-id player-id] channel))

(defn broadcast-msg!
  "Sends message to all channels associated with game"
  [channels game-id msg]
  (doseq [[_ channel] (game-channels channels game-id)]
    (send-msg! channel msg)))
