(ns powergrid-client.simulated.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.log :as log]
            [io.pedestal.app.net.xhr :as xhr]
            [cljs.reader :refer [register-tag-parser!]]
            [cljs.core.async :as async :refer [chan >! <!]]
            [powergrid-client.websocket :refer [websocket-chans]])
  (:require-macros [cljs.core.async.macros :as m :refer [go]]
                   [powergrid-client.util.macros :refer [go-loop]]))

(def ws-chans (atom nil))

(defn game-path [game-id]
  (str (.-hostname (.-location js/window)) ":8484/game/" game-id))

(defmulti services-fn (fn [message input-queue] (msg/type message)))

(defmethod services-fn :default
  [message input-queue]
  (when-let [chans @ws-chans]
    (go (>! (:out chans) message))))

(defmethod services-fn :game-login
  [{:keys [game-id handle color] :as message} input-queue]
  ;; FIXME can't do cross-domain XHR...
  (xhr/request (gensym)
               (str (game-path game-id) "/join")
               :request-method "POST"
               :headers {"Content-Type" "application/edn"}
               :body (pr-str {:handle handle :color color})
               :on-success
               (fn [resp]
                 (.log js/console resp)
                 ;(p/put-message input-queue)
                 (services-fn {msg/type :game-connect :game-id game-id} input-queue))
               :on-error
               (fn [res]
                 (log/error :in :services-fn/game-login :data res))))

(defmethod services-fn :game-connect
  [{:keys [game-id] :as message} input-queue]
  (let [chans (websocket-chans (str "ws://" (game-path game-id) "/ws"))]
    (reset! ws-chans chans)
    (go
      (<! (:open chans))
      (>! (:out chans) {msg/type :game-state})
      (>! (:out chans) {msg/type :whos-online}))
    (go-loop
      ;; todo alts w/ close channel
      (let [msg (<! (:in chans))]
        (p/put-message input-queue msg)))))

(defrecord WebsocketService [app]
  p/Activity
  (start [this]
    (services-fn {msg/type :game-connect :game-id "1"} (:input app)))
  (stop [this]
    (when-let [close-chan (:close @ws-chans)]
     (go
      (!> close-chan true)))))

