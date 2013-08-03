(ns powergrid.domain
  (:require [cljs.reader :refer [register-tag-parser!]]
            [powergrid.domain.game :as game]
            [powergrid.domain.player :as player]
            [powergrid.domain.cities]
            [powergrid.domain.auction]
            [powergrid.domain.resource]
            [powergrid.domain.power-plants]))

(defn register-tag-parsers!
  "Registers the tag parsers for the powergrid types"
  []
  (register-tag-parser! "powergrid.domain.game.Game" powergrid.domain.game/map->Game)
  (register-tag-parser! "powergrid.domain.player.Player" powergrid.domain.player/map->Player)
  (register-tag-parser! "powergrid.domain.cities.Cities" powergrid.domain.cities/map->Cities)
  (register-tag-parser! "powergrid.domain.auction.Auction" powergrid.domain.auction/map->Auction)
  (register-tag-parser! "powergrid.domain.resource.Resource" powergrid.domain.resource/map->Resource)
  (register-tag-parser! "powergrid.domain.power_plants.PowerPlant" powergrid.domain.power-plants/map->PowerPlant))
