(ns powergrid.components.player-bar
  (:use-macros [dommy.macros :only [node deftemplate]])
  (:require [powergrid.component :as component]
            [powergrid.util.log :refer [debug info error spy]]
            [powergrid.common.game :as g]
            [powergrid.common.player :as p]
            [dommy.core :as dommy]
            [dommy.template]))

(def ^:private attributes
  {:player-icon :.player-icon
   :action-to-array :.action-to-arrow})

;(defn- player-icons [player-bar]
  ;(component/select player-bar :.player-icon)
  ;)

(defn set-turn-order [player-bar event {:keys [player-ids]}]
  (let [player-nodes (component/select player-bar :player-icon)]))

(defn action-to-player [player-bar event event-data]
  (debug [player-bar event event-data])
  ;(debug (component/select player-bar :.row))
  (debug "action-to-player!!"))

(defn- turn-order-map
  "Returns maping from player-id to index in player order"
  [game]
  (into {} (map-indexed #(vector %2 %1) (:turn-order game))))

(deftemplate player-bar-tpl [game]
  (sort-by (comp (turn-order-map game) p/id)
           (g/players game)))

(defrecord PlayerBar [mount]
  component/PComponent
  (event-subscriptions [_]
    {:anywhere {:set-turn-order set-turn-order
                :action-to-player action-to-player}})
  (render [this {:keys [game]}]
    (dommy/append! mount (player-bar-tpl game))))
