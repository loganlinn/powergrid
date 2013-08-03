(ns powergrid.common
  (:require [cljs.reader :refer [register-tag-parser!]]
            [powergrid.common.game :as game]
            [powergrid.common.player :as player]
            [powergrid.common.cities]
            [powergrid.common.auction]
            [powergrid.common.resource]
            [powergrid.common.power-plants]))

(defn register-tag-parsers!
  "Registers the tag parsers for the powergrid types"
  []
  (register-tag-parser! "powergrid.common.game.Game" powergrid.common.game/map->Game)
  (register-tag-parser! "powergrid.common.player.Player" powergrid.common.player/map->Player)
  (register-tag-parser! "powergrid.common.cities.Cities" powergrid.common.cities/map->Cities)
  (register-tag-parser! "powergrid.common.auction.Auction" powergrid.common.auction/map->Auction)
  (register-tag-parser! "powergrid.common.resource.Resource" powergrid.common.resource/map->Resource)
  (register-tag-parser! "powergrid.common.power_plants.PowerPlant" powergrid.common.power-plants/map->PowerPlant))
