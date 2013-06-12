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

(defn listen!
  [node event handler]
  (dommy/listen! node (str event) handler))

(defn trigger!
  ([node event]
   (trigger! node event nil))
  ([node event data]
   (powergrid.dom-events/trigger node (str event) data)))

(defn- bind-events!
  "Binds events component's subscribed events to specified handlers"
  [mount-node event-subscriptions]
  (let [sel-target (fn [selector]
                     (condp = selector
                       :anywhere js/document
                       :self mount-node
                       (flatten [mount-node selector])))]
    (doseq [[selector event-map] event-subscriptions
            [event-name handler] event-map]
      (listen! (sel-target selector) event-name #(handler mount-node % (.-detail %))))))

(defn mount!
  "Mounts component at mount-node.
  Unmounts any existing component at mount-node before-hand.
  Returns mount-node."
  [mount-node component]
  (when-let [event-map (:event-map component)]
    (bind-events! mount-node event-map))
  (when-let [after-mount! (:after-mount component)]
    (after-mount! mount-node)))
