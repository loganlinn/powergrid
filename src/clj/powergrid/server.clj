(ns powergrid.server
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.handler :refer [site]]
            [clojure.tools.nrepl.server :as repl]
            [powergrid.service])
  (:gen-class))


(defn -main
  [& args]
  (println "Starting powergrid repl server...")
  (repl/start-server :port 7474)
  (println "Starting powergrid service...")
  (run-server (wrap-reload (site #'powergrid.service/app)) {:port 8484}))
