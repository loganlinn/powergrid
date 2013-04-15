(ns powergrid.service
  (:require [powergrid.core :as core]
            [io.pedestal.service.http.servlet :as ps]
            [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.log :as log]
            ;; the impl dependencies will go away
            [io.pedestal.service.impl.interceptor :as interceptor]
            [io.pedestal.service.interceptor :refer [definterceptor handler]]
            [io.pedestal.service.http.impl.servlet-interceptor :as servlet-interceptor]
            [io.pedestal.service.http.ring-middlewares :as middlewares]
            [io.pedestal.service.http.body-params :as body-params]
            ;; these next two will collapse to one
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-response]
            [io.pedestal.service.http.sse :refer :all]
            [ring.util.mime-type :as ring-mime]
            [ring.middleware.session.cookie :as cookie]))

(def ^{:doc "Map of subscriber IDs to SSE contexts"}
  subscribers (atom {}))

(defn context-key
  "Return key for given `context`."
  [sse-context]
  (get-in sse-context [:request :cookies "player-id" :value]))

(defn add-subscriber
  "Add `context` to subscribers map."
  [sse-context]
  (swap! subscribers assoc (context-key sse-context) sse-context))

(defn remove-subscriber
  "Remove `context` from subscribers map and end the event stream."
  [context]
  (log/info :msg "removing subscriber")
  (swap! subscribers dissoc (context-key context))
  (end-event-stream context))

(def ^{:doc "Interceptor used to add subscribers."}
  wait-for-events (sse-setup add-subscriber))

(declare url-for)

(defn- session-id [] (.toString (java.util.UUID/randomUUID)))

(defn subscribe
  [request]
  (let [session-id (or (get-in request [:cookies "player-id" :value])
                       (session-id))
        cookie {:player-id {:value session-id :path "/"}}]
    (-> (ring-response/redirect (url-for ::wait-for-events))
        (update-in [:cookies] merge cookie))))

(defn publish
  [{msg-data :edn-params :as request}]
  (log/info :message "publish"
            :request request
            :msg-data msg-data)
  (when msg-data
    ;; TODO handle msg
    )
  (ring-response/response ""))

(definterceptor session-interceptor
  (middlewares/session {:store (cookie/cookie-store)}))

;; define service routes
(defroutes routes
  [[["/" ^:interceptors [body-params/body-params session-interceptor]
     ["/msgs" {:get subscribe :post publish}
      ["/events" {:get wait-for-events}]]]]])

(def url-for (route/url-for-routes routes))

;; Consumed by chat-server.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ::bootstrap/routes routes
              ;; Root for resource interceptor that is available by default.
              ;;              ::bootstrap/resource-path nil
              ;; Choose from [:jetty :tomcat].
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
