(ns powergrid.components.player-icon
  (:use-macros [dommy.macros :only [node deftemplate]])
  (:require [powergrid.component :as component]
            [powergrid.util.log :refer [debug info error spy]]
            [powergrid.common.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.power-plants :as pp]
            [dommy.core :as dommy]))


(defn- pid
  "Returns player id associated with player-icon component"
  [player-icon]
  (p/id (:player player-icon)))

(defn- icon-class
  "Returns unique icon class for this player-icon component"
  [player-id]
  (str ".player-" player-id))

(defn- icon-node
  "Returns node for this player icon"
  [player-icon]
  (component/sel1 player-icon (icon-class (pid player-icon))))

(defn on-player-join
  [player-icon event {:keys [player-id]}]
  (when (= (pid player-icon) player-id)
    (dommy/toggle-class! (icon-node player-icon) "online" true)))

(defn on-player-leave
  [player-icon event {:keys [player-id]}]
  (when (= (pid player-icon) player-id)
    (dommy/remove-class! (icon-node player-icon) "online")))

(defn on-hover
  [player-icon event event-data]
  (dommy/add-class! (component/sel1 player-icon :.player-details) "active"))

(deftemplate player-details-tpl [player cities]
  [:div.player-details
   [:span.money (str "$" (p/money player))]
   [:ul.power-plants
    (map (fn [plant] [:li plant]) ;; TODO display resources on power-plant
         (p/power-plants player))]
   [:ul.cities
    (map (fn [city] [:li (name city)])
         cities)]])

(deftemplate player-icon-tpl [player cities]
  [:div.player-icon.offline
   {:class (str "player-" (p/id player))}
   [:span.online-indicator]
   ;; TODO visual indicators for power-plant types
   ])

(def component
  {:event-map {:anywhere {:player-join on-player-join
                          :player-leave on-player-leave}}})
