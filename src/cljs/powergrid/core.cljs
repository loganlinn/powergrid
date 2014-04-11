(ns powergrid.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string]
            [cljs.reader]
            [cljs.core.async :refer [<! chan put! sliding-buffer]]
            [powergrid.domain.game :as game]
            [powergrid.domain.player :as player]
            [powergrid.domain.cities :as cities]
            [powergrid.domain.auction :as auction]
            [powergrid.domain.resource :as resource]
            [powergrid.domain.power-plants :as power-plants]
            [powergrid.domain.country.usa]
            [powergrid.style]
            [powergrid.ui.power-plants :as power-plants-ui]
            [powergrid.ui.resources :as resources-ui]
            [powergrid.ui.auction :as auction-ui]
            [powergrid.ui.players :as players-ui]
            [powergrid.ui.cities :as cities-ui]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Components

(defn game-view [game owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [phase step]} game
            action-player-id (game/action-player-id game)
            has-action? (= (:player-id game) action-player-id)]
        (dom/div #js {:id "game" :className (when has-action? "has-action")}
                 (dom/div #js {:className "cities"}
                          (om/build cities-ui/cities-view
                                    (:cities game)))

                 (dom/div nil
                          (dom/h3 nil (str "Step " step ", Phase " phase ": " (game/phase-title phase))))

                 (dom/div #js {:className "players"}
                          (dom/h3 nil "Players")
                          (om/build players-ui/players-view
                                    {:players (:players game)
                                     :turn-order (:turn-order game)
                                     :action-player-id action-player-id}))
                 (dom/div #js {:className "power-plants"}
                          (dom/h3 nil "Power Plant Market")
                          (om/build power-plants-ui/power-plant-market
                                    (:power-plants game)))
                 (when-let [auction (:auction game)]
                   (dom/div nil
                            (om/build auction-ui/auction-view auction)))
                 (dom/div #js {:className "resources"}
                          (dom/h3 nil "Resource Market")
                          (om/build resources-ui/resource-market
                                    (:resources game)))
                 )))))

(defn app-menu []
  (dom/div #js {:className "pure-menu pure-menu-horizontal pure-menu-open"}
           (dom/a #js {:href "#" :className "pure-menu-heading"} "Powergrid")
           (dom/ul nil
                   (dom/li nil (dom/a #js {:href "#"} "New Game"))
                   (dom/li nil (dom/a #js {:href "#"} "Games"))
                   (dom/li nil (dom/a #js {:href "#"} "Logout")))))

(defn app-view [app owner]
  (dom/div #js {:className "pure-g"}
           (dom/div #js {:className "pure-u-1"}
                    (app-menu))
           (dom/div #js {:className "pure-u-1"}
                    (when-let [game (:game app)]
                      (om/build game-view game)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Initialization

(cljs.reader/register-tag-parser! "powergrid.domain.game.Game" powergrid.domain.game/map->Game)
(cljs.reader/register-tag-parser! "powergrid.domain.player.Player" powergrid.domain.player/map->Player)
(cljs.reader/register-tag-parser! "powergrid.domain.cities.Cities" powergrid.domain.cities/map->Cities)
(cljs.reader/register-tag-parser! "powergrid.domain.auction.Auction" powergrid.domain.auction/map->Auction)
(cljs.reader/register-tag-parser! "powergrid.domain.resource.Resource" powergrid.domain.resource/map->Resource)
(cljs.reader/register-tag-parser! "powergrid.domain.power_plants.PowerPlant" powergrid.domain.power-plants/map->PowerPlant)

(def app-state
  (atom {:game {:player-id :blue
                :country :usa
                :phase 1
                :step 1
                :round 1
                :power-plants {:market (power-plants/initial-market)
                               :future (power-plants/initial-future)}
                :resources (resource/initial-resources)
                :players {:blue (player/new-player "logan" :blue)
                          :red (player/new-player "maeby" :red)}
                :turn-order [:blue :red]
                :turns (list :blue :red)
                :cities (cities/map->Cities
                         {:owners {}
                          :connections (cities/as-graph powergrid.domain.country.usa/connections)})}}))

(om/root app-view app-state
         {:target (.getElementById js/document "app")})
