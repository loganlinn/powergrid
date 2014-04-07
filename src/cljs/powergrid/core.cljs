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
            [powergrid.ui.resources :as resources-ui]
            [powergrid.ui.auction :as auction-ui]
            [powergrid.ui.players :as players-ui]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Components

(defn game-view [game owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "game"}
               (dom/div nil
                        (dom/h3 nil (str "Phase: " (:phase game)))
                        (dom/h3 nil (str "Step: " (:step game))))
               (dom/div #js {:id "players"}
                        (dom/h3 nil "Players")
                        (om/build players-ui/players-view (select-keys game [:players :turn-order])))
               (dom/div #js {:id "power-plants"}
                        (dom/h3 nil "Power Plants")
                        (om/build power-plants-ui/power-plant-market (:power-plants game)))
               (when-let [auction (:auction game)]
                 (dom/div #js {:id "auction"}
                          (om/build auction-ui/auction-view auction)))
               (dom/div #js {:id "resources"}
                        (dom/h3 nil "Resources")
                        (om/build resources-ui/resource-market (:resources game)))))))

(defn app-view [app owner]
  (dom/div nil
           (when-let [game (:game app)]
             (om/build game-view game))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Initialization

(cljs.reader/register-tag-parser! "powergrid.domain.game.Game" powergrid.domain.game/map->Game)
(cljs.reader/register-tag-parser! "powergrid.domain.player.Player" powergrid.domain.player/map->Player)
(cljs.reader/register-tag-parser! "powergrid.domain.cities.Cities" powergrid.domain.cities/map->Cities)
(cljs.reader/register-tag-parser! "powergrid.domain.auction.Auction" powergrid.domain.auction/map->Auction)
(cljs.reader/register-tag-parser! "powergrid.domain.resource.Resource" powergrid.domain.resource/map->Resource)
(cljs.reader/register-tag-parser! "powergrid.domain.power_plants.PowerPlant" powergrid.domain.power-plants/map->PowerPlant)

(def app-state
  (atom {:game {:phase 1
                :step 1
                :round 1
                :power-plants {:market (powergrid.domain.power-plants/initial-market)
                               :future (powergrid.domain.power-plants/initial-future)}
                :resources (powergrid.domain.resource/initial-resources)
                :players {:blue (powergrid.domain.player/new-player "logan" :blue)
                          :red (powergrid.domain.player/new-player "maeby" :red)}
                :turn-order [:blue :red]}}))

(om/root app-view app-state
         {:target (.getElementById js/document "app")})
