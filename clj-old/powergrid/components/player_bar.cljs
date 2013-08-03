(ns powergrid.components.player-bar
  (:use-macros [dommy.macros :only [node deftemplate]])
  (:require [powergrid.component :as component]
            [powergrid.util.log :refer [debug info error spy]]
            [powergrid.common.game :as g]
            [powergrid.common.player :as p]
            [powergrid.components.player-icon :as player-icon]
            [dommy.core :as dommy]))

(def ^:private attributes
  {:player-icon :.player-icon
   :action-to-array :.action-to-arrow})

(defn select-player-icon
  [player-bar player-id]
  (component/sel1 player-bar (str ".player-" (name player-id))))

(defn set-turn-order [node event event-data])

(defn action-to-player [n event {:keys [player-id] :as event-data}]
  (debug "action-to-player" [n event event-data])
  (if-let [curr (select-player-icon n player-id)]
    (dommy/add-class! curr "has-action")))

(defn- turn-order-map
  "Returns maping from player-id to index in player order"
  [game]
  (into {} (map-indexed #(vector %2 %1) (:turn-order game))))

(deftemplate player-bar-tpl [game]
  (sort-by (comp (turn-order-map game) p/id)
           (g/players game)))

(def component
  {:event-map {:anywhere {:set-turn-order set-turn-order
                          :action-to-player action-to-player}}})
