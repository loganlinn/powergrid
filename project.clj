(defproject powergrid "0.1.0-SNAPSHOT"
  :description ""
  :url "https://github.com/loganlinn/powergrid"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/algo.monads "0.1.4"]
                 [com.taoensso/timbre "2.1.2"]
                 [ring "1.1.8"]
                 [http-kit "2.1.1"]
                 [potemkin "0.3.0"]
                 [ring-anti-forgery "0.2.1"]
                 [shoreleave/shoreleave-pubsub "0.3.0"] ;; TODO REMOVE
                 [shoreleave/shoreleave-browser "0.3.0"] ;; TODO REMOVE
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [prismatic/dommy "0.1.1"] ;; TODO REMOVE
                 [slingshot "0.10.3"]
                 [robert/hooke "1.3.0"]
                 [midje "1.5.1"]]
  :plugins  []
  :profiles {:dev {:source-paths  ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]]
                   :plugins [[lein-midje "3.0.0"]]}})
