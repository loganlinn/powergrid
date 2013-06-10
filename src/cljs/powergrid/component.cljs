(ns powergrid.component
  (:use-macros [dommy.macros :only [node sel sel1]])
  (:require [powergrid.protocols :as p]
            [powergrid.dom-events]
            [powergrid.util.log :refer [debug info error spy]]
            [dommy.core :as dommy]
            [dommy.template]))

(defrecord Mixin [protocol methods])

(defprotocol PComponent
  (event-subscriptions [this])
  (render [this data]))

(defn select [component selector]
  (dommy/sel [(:mount component) selector]))

(defn listen!
  [node event handler]
  (dommy/listen! node (str event) handler))

(defn trigger!
  ([node event]
   (trigger! node event nil))
  ([node event data]
   (powergrid.dom-events/trigger node (str event) data)))

(defn- bind-component-events [component]
  (let [mount (:mount component)
        sel-target (fn [selector]
                     (condp = selector
                       :anywhere js/document
                       :self mount
                       (flatten [mount selector])))]
    (doseq [[selector event-map] (event-subscriptions component)
            [event handler] event-map]
      (debug (sel-target selector))
      (listen! (sel-target selector) event handler)
      )))

(defn mount-component
  ([component-ctor mount data]
   (let [component (component-ctor mount)]
     (render component data)
     (bind-component-events component)
     nil))
  ([component-ctor mount]
   (mount-component component-ctor mount {})))

(comment
  (mount-component map->PlayerBar (sel1 :#player-bar)))
