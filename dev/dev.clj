(ns dev
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer  (pprint)]
            [clojure.repl :refer :all] ;; explore
            [clojure.tools.namespace.repl :refer  (refresh refresh-all)]
            [powergrid.system :as system]))

(def system nil)

(defn init
  "Constructs the current development system"
  []
  (alter-var-root #'system (constantly (system/system))))

(defn start
  "Starts the current development system"
  []
  (alter-var-root #'system system/start))

(defn stop
  "Shutsdown and destroys current development system"
  []
  (alter-var-root #'system (fn [s] (when s (system/stop s)))))

(defn go
  "Initializes current development system and starts it running"
  []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))
