(ns powergrid.components.player-bar
  (:use-macros [powergrid.macros :only [defcomponent]]
               [dommy.macros :only [node]])
  (:require [powergrid.component :as component]
            [powergrid.util.log :refer [debug info error spy]]
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
  (debug "action-to-player!!"))

(defrecord PlayerBar [mount]
  component/PComponent
  (event-subscriptions [_]
    {:anywhere {:set-turn-order set-turn-order
                :action-to-player action-to-player}})
  (render [this data]
    (dommy/append! mount (node [:div {:id (:id data)} "FOOD!"]))))
