(ns powergrid.service
  (:require [powergrid.core :as c]
            [powergrid.game :as g]
            [powergrid.messages.factory :as msgs]
            [powergrid.common.player :as p]
            [powergrid.common.power-plants :as pp]
            [powergrid.service.channel :as chan]
            [org.httpkit.server :refer :all]
            [compojure.core :refer [defroutes context GET POST ANY]]
            [hiccup.core :refer [html]]
            [hiccup.page :as page]
            [ring.util.response :refer [response redirect redirect-after-post]]
            [ring.middleware.resource :refer [wrap-resource]]
            [slingshot.slingshot :refer [try+]]
            [shoreleave.middleware.rpc :refer [wrap-rpc defremote]])
  (:import [powergrid.message ValidationError]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn edn-response
  "Returns Ring response for EDN payload"
  ([data]
   (edn-response data 200))
  ([data status]
   {:status status
    :headers {"Content-Type" "application/edn"}
    :body (pr-str data)}))

;;

(def games (atom {}))

(defn reset-game []
  (swap! games assoc "1" (-> (g/new-game :usa
                                         [(p/new-player 1 "Logan" :red)
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

(defn- send-game
  [channel game-id]
  (chan/send! channel {:game (client-game (@games game-id))}))

(defmulti handle-message (fn [msg-type msg channel game-id player-id] msg-type))

(defmethod handle-message :default
  [msg-type _ channel _ player-id]
  (println "Unknown message" msg-type player-id)
  (chan/send! channel {:error "Unknown message"}))

(defmethod handle-message :update-game
  [_ msg channel game-id player-id]
  (try+
    (if-let [game-msg (msgs/create-message msg)]
      (swap! games update-in [game-id] c/update-game game-msg)
      (if (not= msg {}) (chan/send! channel {:error "Invalid message"}))) ;; TODO don't use empty map to get current state
    (send-game channel game-id)
    (catch ValidationError e
      (chan/send! channel {:error (:message e)}))))

(defmethod handle-message :game-state
  [_ _ channel game-id player-id]
  (send-game channel game-id))

(defn ws-handler [game-id player-id req]
  (with-channel req channel
    (on-close channel
              (fn [status]
                (prn "Closing channel" status)
                (chan/cleanup game-id player-id)))

    (on-receive channel
                (fn [data]
                  (let [data (read-string data)]
                    (if (map? data)
                      (doseq [[msg-type msg] data]
                        (handle-message msg-type msg channel game-id player-id))))))

    (chan/broadcast game-id {:joined player-id})
    (chan/setup channel game-id player-id)

    (if (websocket? channel)
      (prn "WebSocket channel" channel)
      (prn "HTTP channel"))
    ))

(defn render-game [game-id request]
  (page/html5
    [:head
     [:title "Funkenschlag"]
     (page/include-css "/css/page.css")]
    [:body {:data-game-id game-id}
     [:h1 "Funkenschlag"]
     [:div#game]
     (page/include-js "/js/cljs.js")]))

(defn render-join [request]
  (page/html5
    [:head]
    [:body
     [:h1 "Join Game"]
     [:form {:method "POST" :action (:uri request)}
      [:input {:type "text" :name "handle" :placeholder "Handle"}]
      [:input {:type "submit" :value "Join"}]]]))

(defn game-url
  ([game-id] (str "/game/" game-id))
  ([game-id action] (str (game-url game-id) "/" (name action))))

(defroutes handler
  (GET "/" []
       "Welcome to Funkenschlag")
  (GET "/games" []
       "TODO: Lobby")

  (context "/game/:game-id" [game-id]
           (GET "/" {:keys [session] :as req}
                (cond
                  (empty? session)
                  (redirect (game-url game-id :join)) ;; TODO check if game is joinable

                  (not= (:game-id session) game-id)
                  (redirect (game-url game-id))

                  :else (response (render-game game-id req))))

           (GET "/ws" {:keys [session] :as req}
                (if (and (= game-id (:game-id session)) (:id session))
                  (ws-handler game-id (:id session) req)
                  (-> (response "")
                      (assoc :status 403))))

           (GET "/join" {:keys [session] :as req}
                (response (render-join req)))
           (POST "/join" {:keys [session params] :as req}
                 (if-let [handle (:handle params)] ;; TODO check if handle in use
                   (-> (redirect (game-url game-id))
                       (assoc :session {:id (uuid)
                                        :game-id game-id
                                        :handle handle}))
                   (redirect (:uri req)))))

  (ANY "/logout" [] (-> (redirect "/")
                        (assoc :session nil))))

(def app (-> handler
             (wrap-resource "public")))
