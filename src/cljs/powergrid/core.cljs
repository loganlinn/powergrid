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
            [powergrid.domain.power-plants]))

(enable-console-print!)


(def app-state (atom {}))

(defn resource-name [r]
  (if (set? r)
    (clojure.string/join "-" (sort (map name r)))
    (name r)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Components

(defn power-plant-card [data owner]
  (reify
    om/IRender
    (render [this]
      (let [{:keys [number resource capacity yield]} data]
        (dom/div #js {:className (str "power-plant " (resource-name resource) " pp-" number)}
                 (dom/span #js {:className "pp-number"} number)
                 (dom/span #js {:className "pp-capacity"} capacity)
                 (dom/span #js {:className "pp-yield"} yield))))))

(defn power-plant-market [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "power-plants"}
               "Current Power Plants"
               (apply dom/div #js {:className "present-market"}
                      (om/build-all power-plant-card (:market data)
                                    {:key :number}))
               "Future Power Plants"
               (apply dom/div #js {:className "future-market"}
                      (om/build-all power-plant-card (:future data)
                                    {:key :number}))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Initialization

(cljs.reader/register-tag-parser! "powergrid.domain.game.Game" powergrid.domain.game/map->Game)
(cljs.reader/register-tag-parser! "powergrid.domain.player.Player" powergrid.domain.player/map->Player)
(cljs.reader/register-tag-parser! "powergrid.domain.cities.Cities" powergrid.domain.cities/map->Cities)
(cljs.reader/register-tag-parser! "powergrid.domain.auction.Auction" powergrid.domain.auction/map->Auction)
(cljs.reader/register-tag-parser! "powergrid.domain.resource.Resource" powergrid.domain.resource/map->Resource)
(cljs.reader/register-tag-parser! "powergrid.domain.power_plants.PowerPlant" powergrid.domain.power-plants/map->PowerPlant)

(swap! app-state assoc :power-plants
       {:market (powergrid.domain.power-plants/initial-market)
        :future (powergrid.domain.power-plants/initial-future)})

(om/root power-plant-market
         (:power-plants @app-state)
         {:target (.getElementById js/document "app")})
