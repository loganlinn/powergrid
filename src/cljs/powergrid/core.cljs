(ns powergrid.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string]
            [cljs.core.async :refer [<! chan put! sliding-buffer]]))

(enable-console-print!)


#_(defn register-tag-parsers!
    "Registers the tag parsers for the powergrid types"
    []
    (register-tag-parser! "powergrid.domain.game.Game" powergrid.domain.game/map->Game)
    (register-tag-parser! "powergrid.domain.player.Player" powergrid.domain.player/map->Player)
    (register-tag-parser! "powergrid.domain.cities.Cities" powergrid.domain.cities/map->Cities)
    (register-tag-parser! "powergrid.domain.auction.Auction" powergrid.domain.auction/map->Auction)
    (register-tag-parser! "powergrid.domain.resource.Resource" powergrid.domain.resource/map->Resource)
    (register-tag-parser! "powergrid.domain.power_plants.PowerPlant" powergrid.domain.power-plants/map->PowerPlant))

(def app-state (atom {}))

(defn power-plant-card [power-plant owner]
  (reify
    om/IRender
    (render [this]
      (let [{:keys [number resource capcity yield]} power-plant]
        (dom/div #js {:className "power-plant"}
                 (clojure.string/join ["PowerPlant: "
                                       number
                                       resource
                                       capacity
                                       yield]))))))

(defn power-plant-market [app owner]
  (reify
    om/IRender
    (render [this]
      )))

(om/root power-plant-card
         {:number 3 :resource :oil :capacity 2 :yield 1}
         {:target (.getElementById js/document "app")})
