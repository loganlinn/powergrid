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

(defn select [component & selectors]
  (sel (:mount component) selectors))

(defn listen!
  [node event handler]
  (dommy/listen! node (str event) handler))

(defn trigger!
  ([node event]
   (trigger! node event nil))
  ([node event data]
   (powergrid.dom-events/trigger node (str event) data)))

(defn event-handler
  "Returns event handler with closure on component to invoke the
  component's callbacks with"
  [component handler]
  (fn [evt]
    (handler component evt (.-detail evt))))

(defn- bind-events
  "Binds events component's subscribed events to specified handlers"
  [component]
  (let [mount (:mount component)
        sel-target (fn [selector]
                     (condp = selector
                       :anywhere js/document
                       :self mount
                       (flatten [mount selector])))]
    (doseq [[selector event-map] (event-subscriptions component)
            [event handler] event-map]
      (listen! (sel-target selector) event (event-handler component handler))
      )))

(defn mount-component!
  "Creates and initializes component, returns mount"
  ([component-ctor mount data]
   (let [component (component-ctor mount)]
     (render component data)
     (bind-events component)
     mount))
  ([component-ctor mount]
   (mount-component! component-ctor mount {})))

(comment
  (mount-component! ->PlayerBar (sel1 :#player-bar) {:id 123}))
