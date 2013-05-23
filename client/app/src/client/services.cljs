(ns client.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]))

;; The services namespace responsible for communicating with back-end
;; services. It receives messages from the application's behavior,
;; makes requests to services and sends responses back to the
;; behavior.
;;
;; This namespace will usually contain a function which can be
;; configured to receive effect events from the behavior in the file
;;
;; app/src/client/start.cljs
;;
;; After creating a new application, set the effect handler function
;; to receive effect
;;
;; (app/consume-effect app services-fn)
;;
;; A very simple example of a services function which echos all events
;; back to the behavior is shown below

(def sockets (atom {}))

(defrecord WebSocketActivity [app uri]
  p/Activity
  (start [this]
    (let [ws (js/WebSocket. uri)]
      (aset "onmessage"
            (fn [message]
              (let [data (read-string (.-data message))]
               (p/put-message (:input app)
                             ))))
      (swap! sockets assoc uri ws)))
  (stop [this]
    (when-let [ws (@sockets uri)]
      (.close ws)))
  p/Transmitter
  (transmit [this message]
    (if-let [ws (@sockets uri)]
      (.send ws (pr-str message))
      (.error js/console "Failed transmit: socket not found"))))

(defn websocket-services-fn
  [ws message queue]
  (.send ws ()))
(comment

  ;; The services implementation will need some way to send messages
  ;; back to the application. The queue passed to the services function
  ;; will convey messages to the application.
  (defn echo-services-fn [message queue]
    (put-message queue message))
  
  )

;; During development, it is helpful to implement services which
;; simulate communication with the real services. This implementaiton
;; can be placed in the file
;;
;; app/src/client/simulated/services.cljs
;;
