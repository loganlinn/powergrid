(ns powergrid.service
  (:require [powergrid.core :as c]
            [powergrid.game :as g]
            [powergrid.messages.factory :as msgs]
            [powergrid.common.player :as p]
            [powergrid.common.power-plants :as pp]
            [org.httpkit.server :refer :all]
            [compojure.core :refer [defroutes GET]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.resource :refer [wrap-resource]]
            [slingshot.slingshot :refer [try+]]
            [shoreleave.middleware.rpc :refer [wrap-rpc defremote]])
  (:import [powergrid.message ValidationError]))

(def games (atom {}))

(defn reset-game []
  (swap! games assoc 1 (-> (g/new-game [(p/new-player 1 "Logan" :red)
                                        (p/new-player 2 "Maeby" :blue)])
                           (assoc :id 1)
                           c/tick)))
(reset-game)

(defn- fix-auction-bidders
  "Replace bidders queue with seq for js portability"
  [game]
  (if-let [a (:auction game)]
    (update-in game [:auction :bidders] seq)
    game))

(defn client-game
  "Returns representation of game state that's sent to clients"
  [game]
  (-> (select-keys game [:id :step :phase :round :players :resources :turns :auction])
      (assoc-in [:power-plants :market] (map pp/id (get-in game [:power-plants :market])))
      (assoc-in [:power-plants :future] (map pp/id (get-in game [:power-plants :future])))
      (assoc-in [:cities :owners] (get-in game [:cities :owners]))
      fix-auction-bidders
      g/map->Game))

;(defremote game-state [game-id]
  ;(if-let [game (@games game-id)]
    ;{:game (client-game game)}
    ;{:error "Unknown game"}))

;(defremote ^{:remote-name :send-message} recieve-message
  ;[game-id data]
  ;(prn game-id data)
  ;(if-let [msg (msgs/create-message data)]
   ;(if (contains? @games game-id)
    ;(try+
      ;(prn msg)
      ;(swap! games update-in [game-id] c/update-game msg)
      ;{:game (client-game (@games game-id))}
      ;(catch ValidationError e
        ;{:error (:message e)}))
     ;{:error "Invalid game"})
    ;{:error "Invalid message"}))

;(defremote ^{:remote-name :reset-game} remote-reset-game []
  ;(reset-game)
  ;{:game (client-game (@games 1))})

(defn ws-handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
                        (prn "channel closed" status)))

    (if (websocket? channel)
      (prn "WebSocket channel")
      (prn "HTTP channel"))

    (on-receive channel (fn [data]
                          (let [msg (read-string data)]
                            (prn "Recieved" msg))
                          (send! channel data)))
    ))

(defroutes handler
  (GET "/" [] (redirect "/powergrid.html"))
  (GET "/funkenschlag" [] ws-handler))

(def app (-> handler
             (wrap-resource "public")
             ;(wrap-rpc "/funkenschlag")
             ))
