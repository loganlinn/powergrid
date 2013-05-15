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
            [slingshot.slingshot :refer [try+]])
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
                                         [(p/new-player "Logan" :red)
                                          (p/new-player "Maeby" :blue)])
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

;;

(defn- send-msg! [channel msg] (chan/send! channel msg))
(defn- send-error! [channel err-msg] (send-msg! channel {:error err-msg}))

(defn game-msg [game-id] {:game (client-game (@games game-id))})

(defn- broadcast-msg!
  "Sends message to all channels associated with game"
  [game-id msg]
  (doseq [channel (chan/game-channels game-id)]
    (send-msg! channel msg)))

(defn- send-game-state!
  "Sends game-state over individual channel"
  [channel game-id]
  (send-msg! channel (game-msg game-id)))

(defn- broadcast-game-state!
  "Sends current game state to all associated channels"
  [game-id]
  (broadcast-msg! game-id (game-msg game-id)))

;;

(defmulti handle-message (fn [msg-type msg channel game-id player-id] msg-type))

(defmethod handle-message :default
  [msg-type _ channel _ player-id]
  (println "Unknown message" msg-type player-id)
  (send-error! channel "Unknown message"))

(defmethod handle-message :update-game
  [_ msg channel game-id player-id]
  (try+
    (if-let [game-msg (msgs/create-message msg)]
      (swap! games update-in [game-id] c/update-game game-msg)
      (if (not= msg {}) (send-error! channel "Invalid message"))) ;; TODO don't use empty map to get current state
    (broadcast-game-state! game-id)
    (catch ValidationError e
      (send-error! channel (:message e)))))

(defmethod handle-message :game-state
  [_ _ channel game-id player-id]
  (send-game-state! channel game-id))

(defmethod handle-message :whos-online
  [_ _ channel game-id player-id]
  )

;;

(defn player-msg
  "Returns msg after associating player-id from session"
  [msg session]
  (assoc msg :player-id (p/id session)))

(defn ws-handler [game-id player-id {:keys [session] :as req}]
  (with-channel req channel
    (on-close channel
              (fn [status]
                (chan/cleanup game-id player-id)
                (broadcast-msg! game-id {:leave player-id})))

    (on-receive channel
                (fn [data]
                  (let [data (read-string data)]
                    (if (map? data)
                      (doseq [[msg-type msg] data]
                        (handle-message msg-type
                                        (player-msg msg session)
                                        channel game-id player-id))))))

    (send-msg! channel {:player-id player-id})
    (broadcast-msg! game-id {:join player-id})
    (chan/setup channel game-id player-id)
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
      [:select {:name "color"} (for [c p/colors :let [cn (name c)]] [:option {:value cn} cn])]
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
                  (ws-handler game-id (p/id session) req)
                  (-> (response "")
                      (assoc :status 403))))

           (GET "/join" {:keys [session] :as req}
                (response (render-join req)))
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
                        (assoc :session nil))))

(def app (-> handler
             (wrap-resource "public")))
