(ns powergrid.client
  (:use-macros [dommy.macros :only [sel sel1 node deftemplate]])
  (:require [powergrid.common.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.cities :as c]
            [powergrid.common.auction :as a]
            [powergrid.common.resource :as r]
            [powergrid.common.power-plants :as pp]
            [powergrid.app]
            [powergrid.templates :as templates]
            [powergrid.util.log :refer [debug info error spy]]
            [powergrid.country :as country]
            [powergrid.country.usa]
            [powergrid.component]
            [powergrid.components.player-bar]
            [powergrid.dom-events]
            [dommy.template]
            [dommy.core :as dom]
            [clojure.browser.repl :as repl]
            [shoreleave.pubsubs.simple :as pbus]
            [shoreleave.pubsubs.protocols :as ps]
            [shoreleave.pubsubs.publishable]
            [cljs.reader :refer [read-string register-tag-parser!]]))

;(repl/connect "http://localhost:9000/repl")

;; Register types for read-string
(register-tag-parser! "powergrid.common.game.Game" g/map->Game)
(register-tag-parser! "powergrid.common.player.Player" p/map->Player)
(register-tag-parser! "powergrid.common.cities.Cities" c/map->Cities)
(register-tag-parser! "powergrid.common.auction.Auction" a/map->Auction)
(register-tag-parser! "powergrid.common.resource.Resource" r/map->Resource)
(register-tag-parser! "powergrid.common.power_plants.PowerPlant" pp/map->PowerPlant)

(set! *print-fn* info)

(def socket-bus (atom nil))
(def game-bus (pbus/bus))
(def app (atom (powergrid.app/create-app)))

(defn game-state [] (:game @app))
(defn player-state [] (g/player (game-state) (:player-id @app)))

(defn update-resource-availability
  "Applies 'unavailable' class to resources not available in market"
  [{:keys [market pricing]} nodes]
  (let [rfill (- (count pricing) market)]
    (doall
      (map-indexed
        (fn [ind node]
          (dom/toggle-class! node "unavailable" (< ind rfill)))
        nodes))))

(defn update-resources
  "Updates all resource nodes in market based on their availability"
  [resources]
  (doseq [r [:coal :oil :garbage :uranium]]
    (update-resource-availability
      (get resources r)
      (sel (str "#resources ." (name r))))))

(defn render-game [game]
  (dom/replace! (sel1 :#game) (templates/game-tpl game))
  ;; TODO Make the following react to render-game
  (update-resources (:resources game))
  (if-let [n (sel1 (str ".player-" (name (g/action-player-id game))))]
    (dom/add-class! n "has-action"))
  (if-let [n (sel1 (str ".player-" (name (p/id (player-state)))))]
   (dom/add-class! n "current-player"))
  (country/render-country "game-map"))

(defn send-message
  "Sends message to back-end"
  ([msg-type msg]
   (ps/publish @socket-bus :send {msg-type msg}))
  ([msg-type]
   (send-message msg-type nil)))

(defn render-debug-panel []
  (let [phase-msg-types {2 :bid
                         3 :buy
                         4 :buy
                         5 :sell}
        msg-tmpls {"Bid Power-Plant" "{:topic :phase2 :type :bid :player-id %player-id% :plant-id %plant-id% :bid %plant-id%}"
                   "Buy Resources" "{:topic :phase3 :type :buy :player-id %player-id% :resources {:oil 0 :coal 0 :garbage 0 :uranium 0}}"
                   "Buy Cities" "{:topic :phase4 :type :buy :player-id %player-id% :new-cities []}"
                   "Power Cities" "{:topic :phase5 :type :sell :player-id %player-id% :powered-plants {}}"
                   "Pass" "{:topic :phase%current-phase% :type %phase-msg-type% :player-id %player-id% :powergrid.message/pass true}"}
        panel (node [:div#debug
                     [:button.update-game "Update Game"]
                     [:button.log-game "Print Game"]
                     [:button.reset-game "Reset Game"]
                     [:div "Messages" (map #(node [:button.msg-tmpl {:value (val %)} (key %)]) msg-tmpls)]
                     [:form.send-message
                      [:textarea.message {:style {:width "100%"}}]
                      [:input {:type "submit" :value "Send Message"}]]])]
    (if-let [prev (sel1 :#debug)] (dom/remove! prev))
    (dom/prepend! (sel1 :body) panel)
    (dom/listen! (sel1 "#debug .update-game") :click #(send-message :game-state))
    (dom/listen! (sel1 "#debug .log-game") :click #(debug @app))
    (dom/listen! [(sel1 :body) :#debug :.msg-tmpl] :click
                 (fn [e]
                   (dom/set-text! (sel1 "#debug .send-message textarea")
                                  (-> (dom/value (.-target e))
                                      (clojure.string/replace #"%player-id%" (str (g/action-player-id (game-state))))
                                      (clojure.string/replace #"%current-phase%" (str (g/current-phase (game-state))))
                                      (clojure.string/replace #"%phase-msg-type%" (str (phase-msg-types (g/current-phase (game-state)))))
                                      (clojure.string/replace #"%plant-id%" (str (first (g/power-plants (game-state)))))
                                      ))))
    (dom/listen! (sel1 "#debug form.send-message") :submit
                 (fn [e]
                   (.preventDefault e)
                   (let [msg (read-string (dom/value (sel1 "#debug .send-message .message")))]
                     (if (and (map? msg) (every? msg [:topic :type]))
                       (do
                         (send-message :update-game msg)
                         (dom/set-text! (sel1 "#debug .send-message textarea") ""))
                       (.debug js/console "Invalid message")))))))

;;;;;;;

(defn websocket-bus
  "Creates websocket to URI, returns bus to subscribe to"
  [uri]
  (let [ws (js/WebSocket. uri)
        b (pbus/bus)]
    (.addEventListener js/window "unload" (fn [_] (.close ws)))
    (doto ws
      (aset "onopen" (fn [] (ps/publish b :open nil)))
      (aset "onclose" (fn [] (ps/publish b :close nil)))
      (aset "onerror" (fn [e] (ps/publish b :error e)))
      (aset "onmessage" (fn [m] (ps/publish b :message (read-string (.-data m))))))
    (ps/subscribe b :send (fn [data] (.send ws (pr-str data))))
    (aset b "_webSocket" ws)
    b))

(defn subscribe-events
  [bus & events]
  (doseq [[event f] (partition 2 events)]
    (ps/subscribe bus event f)))

(defn- init []
  (debug "Initializing client")
  (let [game-id (dom/attr (sel1 :body) :data-game-id)
        base-url (.-host (.-location js/window))
        wsb (websocket-bus (str "ws://" base-url "/game/" game-id "/ws"))]
    ;; Bind loggers
    (subscribe-events wsb
                      :open (fn [] (debug "Socket OPEN"))
                      :close (fn [] (debug "Socket CLOSE"))
                      :error (fn [e] (error "Socket ERROR" e))
                      :message (fn [m] (debug "Socket MESSAGE" (pr-str m)))
                      :send (fn [m] (debug "Socket SEND" (pr-str m))))

    ;; Bind services
    (ps/subscribe wsb :open #(ps/publish wsb :send {:game-state nil}))
    (ps/subscribe wsb :close (fn [] (reset! socket-bus nil)))
    (ps/subscribe wsb :message #(swap! app powergrid.app/handle-messages %))

    (reset! socket-bus wsb))

  ;; Render
  (ps/publishize app game-bus)
  (ps/subscribe game-bus app (fn [{{game :game} :new {game-prev :game} :old :as update}]
                               (if (and game (not= game game-prev))
                                 (render-game game)))))

(init)
(render-debug-panel)
(let [mount (node [:div#player-bar])
      player-bar (powergrid.component/mount-component!
                   powergrid.components.player-bar/->PlayerBar
                   mount
                   {:id 123})]
  (dom/append! (sel1 :body) mount)
  (.setTimeout js/window #(powergrid.component/trigger! js/document :action-to-player {"LOL" 2}) 500)
  (.setTimeout js/window #(powergrid.component/trigger! js/document :action-to-player {"LOL" 3}) 550)
  )
