(ns powergrid.service.channel
  (:require [org.httpkit.server :as s]
            [powergrid.util :refer [dissoc-in]]))

(def channels (atom {})) ; keyed by [game-id player-id]

(add-watch channels :debug (fn [k r old-val new-val]
                             (println "CHANNELS ----------------")
                             (prn new-val)
                             (println "-------------------------")))

(defn send! [channel data]
  (s/send! channel (pr-str data)))

(defn player-channel [game-id player-id]
  (get-in @channels [game-id player-id]))

(defn game-channels [game-id]
  (vals (get @channels game-id)))

(defn cleanup [game-id player-id]
  (when (and game-id player-id)
    (swap! channels dissoc-in [game-id player-id])))

(defn setup [channel game-id player-id]
  (swap! channels assoc-in [game-id player-id] channel))
