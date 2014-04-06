(ns powergrid.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string]
            [cljs.reader]
            [cljs.core.async :refer [<! chan put! sliding-buffer]]
            [powergrid.domain.game]
            [powergrid.domain.player]
            [powergrid.domain.cities]
            [powergrid.domain.auction]
            [powergrid.domain.resource]
            [powergrid.domain.power-plants]
            [powergrid.ui.power-plants :as power-plants-ui]
            [powergrid.ui.resources :as resources-ui]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Components

(defn game-view [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "game"}
               (dom/div #js {:id "power-plants"}
                        (dom/h3 nil "Power Plants")
                        (om/build power-plants-ui/power-plant-market (:power-plants data)))
               (dom/div #js {:id "resources"}
                        (dom/h3 nil "Resources")
                        (om/build resources-ui/resource-market (:resources data)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Initialization

(cljs.reader/register-tag-parser! "powergrid.domain.game.Game" powergrid.domain.game/map->Game)
(cljs.reader/register-tag-parser! "powergrid.domain.player.Player" powergrid.domain.player/map->Player)
(cljs.reader/register-tag-parser! "powergrid.domain.cities.Cities" powergrid.domain.cities/map->Cities)
(cljs.reader/register-tag-parser! "powergrid.domain.auction.Auction" powergrid.domain.auction/map->Auction)
(cljs.reader/register-tag-parser! "powergrid.domain.resource.Resource" powergrid.domain.resource/map->Resource)
(cljs.reader/register-tag-parser! "powergrid.domain.power_plants.PowerPlant" powergrid.domain.power-plants/map->PowerPlant)

(def app-state
  (atom {:power-plants
         {:market (powergrid.domain.power-plants/initial-market)
          :future (powergrid.domain.power-plants/initial-future)}
         :resources
         (powergrid.domain.resource/initial-resources)}))

(om/root game-view app-state
         {:target (.getElementById js/document "app")})
