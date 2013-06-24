(ns powergrid.server
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.handler :refer [site]]
            [clojure.tools.nrepl.server :as repl]
            [powergrid.util.log :as log]
            [powergrid.service])
  (:gen-class))

(def app (powergrid.service/init-handler (atom {})))

(defn -main
  [& args]
  (log/info "Starting powergrid repl server...")
  (repl/start-server :port 7474)
  (log/info "Starting powergrid service...")
  (run-server (wrap-reload (site #'app)) {:port 8484}))
