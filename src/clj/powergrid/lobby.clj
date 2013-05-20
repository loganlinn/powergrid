(ns powergrid.lobby
  (:require [powergrid.common.lobby :as common]))

(def ->Lobby common/->Lobby)
(def map->Lobby common/map->Lobby)
(def max-seats common/max-seats)
(def country common/country)
(def seats common/seats)
(def seats-closed common/seats-closed)
(def seats-total common/seats-total)
(def seats-open common/seats-open)
(def seats-open? common/seats-open?)

(defn new-lobby [game-id]
  (map->Lobby {:game-id game-id
               :seats {}
               :seats-closed 0}))

