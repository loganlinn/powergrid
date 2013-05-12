(ns powergrid.server
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.handler :refer [site]]
            [powergrid.service])
  (:gen-class))


(defn -main
  [& args]
  (println "Starting powergrid service...")
  (run-server (wrap-reload (site #'powergrid.service/app)) {:port 8484}))
