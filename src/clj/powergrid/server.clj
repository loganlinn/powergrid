(ns powergrid.server
  (:require [ring.adapter.jetty :refer :all]
            [powergrid.service])
  (:gen-class))

(defn -main
  [& args]
  (println "Starting powergrid service...")
  (run-jetty #'powergrid.service/app {:port 8484}))
