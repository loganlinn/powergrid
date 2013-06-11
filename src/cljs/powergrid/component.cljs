(ns powergrid.component
  (:require-macros [dommy.macros])
  (:require [powergrid.protocols :as p]
            [powergrid.dom-events]
            [powergrid.util.log :refer [debug info error spy]]
            [dommy.core :as dommy]))

(defn gen-id [] (str "id_" (.getTime (js/Date.))))

(defn get-or-gen-id! [node]
  (if-let [id (dommy/attr node :id)]
    id
    (let [id (gen-id)]
      (dommy/set-attr! node :id id)
      id)))

(defprotocol PComponent
  (event-subscriptions [this])
  (mount! [this mount-node])
  (unmount! [this mount-node]))

(defn sel [component & selectors]
  (dommy.macros/sel (:mount component) selectors))

(defn sel1 [component & selectors]
  (dommy.macros/sel1 (:mount component) selectors))

(defn listen!
  [node event handler]
  (dommy/listen! node (str event) handler))

(defn trigger!
  ([node event]
   (trigger! node event nil))
  ([node event data]
   (powergrid.dom-events/trigger node (str event) data)))

(defn- bind-events
  "Binds events component's subscribed events to specified handlers"
  [component]
  (let [mount-node (:mount component)
        sel-target (fn [selector]
                     (condp = selector
                       :anywhere js/document
                       :self mount-node
                       (flatten [mount-node selector])))]
    (doseq [[selector event-map] (event-subscriptions component)
            [event handler] event-map]
      (listen! (sel-target selector) event #(handler component % (.-detail %))))))

(defn unmount-component!
  "Unmounts component mounted at mount-node, if any.
  Returns mount-node"
  [component mount-node]
  (unmount! component mount-node)
  mount-node)

(defn mount-component!
  "Mounts component at mount-node.
  Unmounts any existing component at mount-node before-hand.
  Returns mount-node."
  [component mount-node]
  (let [component (assoc component :mount mount-node)]
    (bind-events component)
    (mount! component mount-node))
  mount-node)
