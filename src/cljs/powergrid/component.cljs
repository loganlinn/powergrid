(ns powergrid.component
  (:use-macros [dommy.macros :only [node sel sel1]])
  (:require [powergrid.protocols :as p]
            [powergrid.dom]
            [dommy.template]))

(defprotocol PComponent
  (event-subscriptions [this])
  (render [this data]))

(defn base-event-subscriptions [componenet mount]
  {:self {:render (fn [] (render component))}})

(defn bind-component-events [component]
  (let [mount (:mount component)
        target (fn [selector]
                 (condp = selector
                   :anywhere document
                   :self mount
                   :else (flatten [mount selector])))]
    (doseq [[selector event-map] (merge base-event-subscriptions
                                        (event-subscriptions component))
            [event handler] event-map]
      (dommy/listen! (target selector) event handler))))

(defn mount-component
  ([component-ctor mount data]
   (let [component (component-ctor mount)]
     (render component data)
     (bind-component-events component)
     nil))
  ([parent-mount component-ctor mount]
   (mount-component component-ctor mount {})))

(defn select [component selector]
  (dommy/sel [(:mount component) selector]))

(comment
  (mount-component map->PlayerBar (sel1 :#player-bar)))
