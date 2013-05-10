(ns powergrid.service
  (:require [powergrid.core :as c]
            [powergrid.game :as g]
            [powergrid.messages.factory :as msgs]
            [powergrid.common.player :as p]
            [powergrid.common.power-plants :as pp]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.resource :refer [wrap-resource]]
            [slingshot.slingshot :refer [try+]]
            [shoreleave.middleware.rpc :refer [wrap-rpc defremote]])
  (:import [powergrid.message ValidationError]))

(defn- uuid [] (str (java.util.UUID/randomUUID)))

(def games (atom {}))

(swap! games assoc 1 (-> (g/new-game [(p/new-player 1 "Logan" :red)
                                      (p/new-player 2 "Maeby" :blue)])
                         (assoc :id 1)
                         c/tick))

(defremote game-state [game-id]
  (if-let [game (@games game-id)]
    {:game
     (-> (select-keys game [:id :step :phase :round :players :resources :turns :auction])
         (assoc-in [:power-plants :market] (map pp/id (get-in game [:power-plants :market])))
         (assoc-in [:power-plants :future] (map pp/id (get-in game [:power-plants :future]))))}
    {:error "Unknown game"}))

(defremote ^{:remote-name :send-message} recieve-message
  [game-id data]
  (prn game-id data)
  (if-let [msg (msgs/create-message data)]
   (if (contains? @games game-id)
    (try+
      (prn msg)
      (swap! games update-in [game-id] c/update-game msg)
      {:game (@games game-id)}
      (catch ValidationError e
        {:error (:message e)}))
     {:error "Invalid game"})
    {:error "Invalid message"}))

(defroutes handler
  (GET "/" [] (redirect "/powergrid.html")))

(def app (-> handler
             (wrap-resource "public")
             (wrap-rpc)
             (site)))
