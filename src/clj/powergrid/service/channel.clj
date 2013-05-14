(ns powergrid.service.channel
  (:require [org.httpkit.server :as s]
            [powergrid.util :refer [dissoc-in]]))

(def channels (atom {})) ; keyed by [game-id player-id]

(add-watch channels :debug (fn [k r old-value new-value]
                             (println "---------------- channels")
                             (prn new-value)
                             (println "----------------")))

(defn send! [channel data]
  (s/send! channel (pr-str data)))

(defn get-channel [game-id player-id]
  (get-in @channels [game-id player-id]))

(defn cleanup [game-id player-id]
  (when (and game-id player-id)
    (swap! channels dissoc-in [game-id player-id])))

(defn setup [channel game-id player-id]
  (swap! channels assoc-in [game-id player-id] channel))

(defn broadcast
  [game-id data & {:keys [except]}]
  (doseq [[player-id chan] (get @channels game-id)]
    (if (or (not except) (not= player-id except))
     (send! chan data))))
