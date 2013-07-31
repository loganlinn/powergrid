(ns powergrid-client.simulated.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.log :as log]
            [cljs.reader :refer [register-tag-parser!]]
            [cljs.core.async :as async :refer [chan >! <!]]
            [powergrid-client.websocket :refer [websocket-chans]])
  (:require-macros [cljs.core.async.macros :as m :refer [go]]
                   [powergrid-client.util.macros :refer [go-loop]]))

(def ws-uri (str "ws://" (.-hostname (.-location js/window)) ":8484/game/1/ws"))
(def ws-chans (atom nil))

(defrecord WebsocketService [app]
  p/Activity
  (start [this]
    (let [chans (websocket-chans ws-uri)]
      (reset! ws-chans chans)
      (go
        (<! (:open chans))
        (>! (:out chans) {msg/type :game-state})
        (>! (:out chans) {msg/type :whos-online}))
      (go-loop
        (let [msg (<! (:in chans))]
          ;(log/debug :msg msg)
          (p/put-message (:input app) msg)))))
  (stop [this]
    ))

(defn services-fn [message input-queue]
  (log/debug :services-fn message :chans (boolean @ws-chans))
  (when-let [chans @ws-chans]
    (go (>! (:out chans) message))))
