(ns powergrid.service
  (:require [powergrid.core :as c]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.power-plants :as pp]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.resource :refer [wrap-resource]]
            [shoreleave.middleware.rpc :refer [wrap-rpc defremote]]))

(defn- uuid [] (str (java.util.UUID/randomUUID)))

(def games (atom {}))

(swap! games assoc 1 (-> (g/new-game [(p/new-player 1 "Logan" :red)
                                      (p/new-player 2 "Maeby" :blue)])
                         (assoc :id 1)))

(defremote game-state [game-id]
  (if-let [game (@games game-id)]
    (-> (select-keys game [:players :resources :turns])
        (assoc-in [:power-plants :market] (map pp/id (get-in game [:power-plants :market])))
        (assoc-in [:power-plants :future] (map pp/id (get-in game [:power-plants :future]))))))

(defroutes handler
  (GET "/" [] (redirect "/powergrid.html")))

(def app (-> handler
             (wrap-resource "public")
             (wrap-rpc)
             (site)))