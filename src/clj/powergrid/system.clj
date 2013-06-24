(ns powergrid.system
  (:require [powergrid.service]
            [org.httpkit.server :as http-kit]))

(defrecord PowergridSystem [handler server port games])

(defn system
  "Returns new instance of whole application"
  ([] (system {}))
  ([{:keys [port] :or {port 8484}}]
   (map->PowergridSystem
     {:handler nil
      :games nil
      :server nil
      :port port})))

(defn- stop-server
  "Stops server if running, returns system"
  [system]
  (if-let [stop-server (:server system)]
    (do (stop-server) (assoc system :server nil))
    system))

(defn- start-server
  "Starts a server. Sets a 'stop' function at :server"
  [{:keys [handler port] :as system}]
  (assoc system :server (http-kit/run-server handler {:port port})))

(defn start
  "Performs side effects to initialize the system, acquire resources,
  and start it running. Returns an updated instance of the system."
  [system]
  (let [games (atom {})]
    (-> system
        (assoc :games games)
        (assoc :handler (powergrid.service/init-handler games))
        (start-server))))

(defn stop
  "Performs side effects to shut down the system and release its
  resources. Returns an updated instance of the system."
  [system]
  (-> system
      (stop-server)
      (assoc :handler nil)
      (assoc :games nil)))
