(ns powergrid.service
  (:require [powergrid.core :as c]
            [powergrid.game :as g]
            [powergrid.message :as msg]
            [powergrid.messages.factory :as msgs]
            [powergrid.common.player :as p]
            [powergrid.common.power-plants :as pp]
            [powergrid.service.channel :as chan]
            [powergrid.util :refer [uuid]]
            [powergrid.util.log :refer [debug]]
            [org.httpkit.server :refer :all]
            [compojure.core :refer [routes context GET POST ANY]]
            [compojure.handler :refer [site]]
            [hiccup.core :refer [html]]
            [hiccup.page :as page]
            [ring.util.response :refer [response redirect redirect-after-post]]
            [ring.middleware.resource :refer [wrap-resource]]))

(def msg-type :io.pedestal.app.messages/type)
(def msg-topic :io.pedestal.app.messages/topic)

(defn- fix-auction-bidders
  "Replace bidders queue with seq for js portability"
  [game]
  (if-let [a (:auction game)]
    (update-in game [:auction :bidders] seq)
    game))

(defn client-game
  "Returns representation of game state that's sent to clients"
  [game]
  (-> (select-keys game [:id :step :phase :round :players :resources :turns :turn-order :auction])
      (assoc-in [:power-plants :market] (map pp/id (get-in game [:power-plants :market])))
      (assoc-in [:power-plants :future] (map pp/id (get-in game [:power-plants :future])))
      (assoc-in [:cities :owners] (get-in game [:cities :owners]))
      fix-auction-bidders
      g/map->Game))

;;

(defn error-msg [error]
  {msg-type :cons
   msg-topic [:game :error]
   :value error})

(defn game-msg [games game-id]
  {msg-type :swap
   msg-topic [:game :state]
   :value (client-game (@games game-id))})

(defn- send-game-state!
  "Sends game-state over individual channel"
  [games channel game-id]
  (chan/send-msg! channel (game-msg games game-id)))

(defn- broadcast-game-state!
  "Sends current game state to all associated channels"
  [games channels game-id]
  (chan/broadcast-msg! channels game-id (game-msg games game-id)))

(defn whos-online-msg [channels game-id]
  {msg-type :swap
   msg-topic [:game :online]
   :value (keys (chan/game-channels channels game-id))})

;;

(defmulti handle-message (fn [games channels channel game-id player-id message] (msg-type message)))

(defmethod handle-message :default
  [games channels channel game-id player-id message]
  (debug "Unsupported message" message)
  (chan/send-msg! channel (error-msg "Unknown message")))

(defn- debug-game-msg [msg] (debug :game-message msg) msg)

(defn- create-game-msg
  [games game-id player-id message]
  (if-let [game (get @games game-id)]
    (-> (:turn message)
        (assoc :topic (or (:topic message) (msg/expected-topic game)))
        (assoc :player-id player-id)
        (debug-game-msg)
        (msgs/create-message))
    (debug "GAME NOT FOUND")))

(defmethod handle-message :update-game
  [games channels channel game-id player-id message]
  (if-let [game-msg (create-game-msg games game-id player-id message)]
    (do
      (swap! games update-in [game-id]
             c/update-game
             game-msg
             :error #(chan/send-msg! channel (error-msg (str "Turn failed: " %3))))
      (broadcast-game-state! games channels game-id))
    (chan/send-msg! channel (error-msg "Unrecognized message"))))

(defmethod handle-message :game-state
  [games channels channel game-id player-id message]
  (send-game-state! games channel game-id))

(defmethod handle-message :whos-online
  [games channels channel game-id player-id message]
  (chan/send-msg! channel (whos-online-msg channels game-id)))

;;

(defn ws-handler [games channels game-id player-id {:keys [session] :as req}]
  (with-channel req channel
    (on-close channel
              (fn [status]
                (chan/cleanup channels game-id player-id)
                (chan/broadcast-msg! channels game-id (whos-online-msg channels game-id))))

    (on-receive channel
                (fn [data]
                  (let [message (read-string data)]
                    (debug {:game-id game-id :player-id player-id :message message})
                    (handle-message games channels channel game-id player-id message))))

    (chan/send-msg! channel {msg-type :swap
                             msg-topic [:game :player-id]
                             :value player-id})
    (chan/setup channels channel game-id player-id)
    (chan/broadcast-msg! channels game-id
                         (whos-online-msg channels game-id))
    ))

(defn render-game [game-id request]
  (page/html5
    [:head
     [:title "Funkenschlag"]
     (page/include-css "/css/page.css")]
    [:body {:data-game-id game-id}
     [:h1 "Funkenschlag"]
     [:div#game]
     (page/include-js "/js/raphael-min.js"
                      "/js/cljs.js")]))

(defn render-join [request game]
  (page/html5
    [:head]
    [:body
     [:h1 "Join Game"]
     [:form {:method "POST" :action (:uri request)}
      [:input {:type "text" :name "handle" :placeholder "Handle"}]
      [:select {:name "color"} (for [c p/colors ;; TODO use g/available-colors
                                     :let [cn (name c)]]
                                 [:option {:value cn} cn])]
      [:input {:type "submit" :value "Join"}]]]))

(defn game-url
  ([game-id] (str "/game/" game-id))
  ([game-id action] (str (game-url game-id) "/" (name action))))

(defn init-routes
  [games channels]
  (routes
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
                    (ws-handler games channels game-id (p/id session) req)
                    (-> (response "")
                        (assoc :status 403))))

             (GET "/join" {:keys [session] :as req}
                  (if-let [game (@games game-id)]
                    (response (render-join req game))))
             (POST "/join" {:keys [session params] :as req}
                   (if-let [handle (:handle params)] ;; TODO check if handle in use
                     (if-let [color (:color params)]
                       (-> (redirect (game-url game-id))
                           (assoc :session {:id (uuid)
                                            :game-id game-id
                                            :handle handle
                                            :color color})))
                     (redirect (:uri req)))))

    (ANY "/logout" [] (-> (redirect "/")
                          (assoc :session nil)))))

(defn- default-game [id]
  (-> (g/new-game id
                  :usa
                  [(p/new-player "Logan" :black)
                   (p/new-player "Maeby" :blue)])
      c/tick))

(defn init-handler
  [games channels]
  (swap! games assoc "1" (default-game "1")) ;; todo remove
  (-> (init-routes games channels)
      (wrap-resource "public")
      site))
