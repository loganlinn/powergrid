(ns powergrid.lobby
  (:require [potemkin :refer [import-vars]]
            [powergrid.domain.lobby]))

(import-vars
  [powergrid.domain.lobby
   ->Lobby
   map->Lobby
   max-seats
   country
   seats
   seats-closed
   seats-total
   seats-open
   seats-open?])

(defn new-lobby [game-id]
  (map->Lobby {:game-id game-id
               :seats {}
               :seats-closed 0}))

