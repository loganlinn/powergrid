(defproject powergrid "0.1.0-SNAPSHOT"
  :description ""
  :url "https://github.com/loganlinn/powergrid"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/algo.monads "0.1.4"]
                 [com.keminglabs/cljx "0.3.1"]
                 ;; TODO com.stuartsierra/component
                 [om "0.5.3"]
                 [com.taoensso/timbre "3.1.6"]
                 [ring "1.2.2"]
                 [http-kit "2.1.1"]
                 [potemkin "0.3.0"]
                 [ring-anti-forgery "0.2.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.5"]
                 [prismatic/dommy "0.1.1"] ;; TODO REMOVE
                 [midje "1.5.1"]]
  :plugins  [[lein-cljsbuild "1.0.2"]]

  :source-paths ["target/generated/src/clj" "src/clj"]
  :resource-paths ["target/generated/src/cljs"]
  :test-paths ["target/generated/test/clj" "test/clj"]

  :profiles {:dev {:source-paths  ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]]
                   :plugins [[lein-midje "3.0.0"]]}}

  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "target/generated/src/cljs"]
                        :compiler {:output-to "out/dev/powergrid.dev.js"
                                   :output-dir "out/dev"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "release"
                        :source-paths ["src/cljs" "target/generated/src/cljs"]
                        :compiler {:output-to "out/release/powergrid.release.js"
                                   :output-dir "out/release"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :preamble ["react/react.min.js"]
                                   :externs ["react/externs/react.js"]}}
                       #_{:id "test"
                          :source-paths ["src/cljs"
                                         "test/cljs"
                                         "target/generated/src/cljs"
                                         "target/generated/test/cljs"]
                          :compiler {:output-to "main.js"
                                     :output-dir "out"
                                     :optimizations :none
                                     :source-map true}}]})
