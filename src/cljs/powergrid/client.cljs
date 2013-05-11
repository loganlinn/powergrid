(ns powergrid.client
  (:use-macros [dommy.macros :only [sel sel1 node deftemplate]])
  (:require [dommy.core :as dom]
            [dommy.template]
            [powergrid.common.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.cities :as c]
            [powergrid.common.auction :as a]
            [powergrid.common.resource :as r]
            [powergrid.common.power-plants :as pp]
            [clojure.browser.repl :as repl]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [cljs.reader :refer  [read-string register-tag-parser!]]))

;(repl/connect "http://localhost:9000/repl")

;; Register types for read-string
(register-tag-parser! "powergrid.common.game.Game" g/map->Game)
(register-tag-parser! "powergrid.common.player.Player" p/map->Player)
(register-tag-parser! "powergrid.common.cities.Cities" c/map->Cities)
(register-tag-parser! "powergrid.common.auction.Auction" a/map->Auction)
(register-tag-parser! "powergrid.common.resource.Resource" r/map->Resource)
(register-tag-parser! "powergrid.common.power_plants.PowerPlant" pp/map->PowerPlant)

(defn log [& args] (doseq [x args] (.log js/console (pr-str x))))
(defn log-r [& args] (doseq [x args] (.log js/console x)))
(defn log-g [f game] (.log js/console (name f) (f game)))

(set! *print-fn* log)

(extend-type powergrid.common.player.Player
  dommy.template/PElement
  (-elem [{:keys [id handle color money] :as player}]
    (node [:div.player
           {:class (str "player-" (name color))
            :id (str "player-" id)}
           [:span.handle handle]
           [:div.player-icon]
           [:div.money (str "$" money)]
           [:ul.power-plants
            (map #(node [:li %]) (p/power-plants player))]])))

(deftemplate resources-tpl []
  [:div#resources
   [:h3 "Resources"]
   (for [cost (range 1 9)]
     [:div.resource-block
      {:data-resource-cost cost}
      [:span.resource-block-cost cost]
      [:div
       [:span.resource.coal]
       [:span.resource.coal]
       [:span.resource.coal]]
      [:div
       [:span.resource.oil]
       [:span.resource.oil]
       [:span.resource.oil]]
      [:div
       [:span.resource.garbage]
       [:span.resource.garbage]
       [:span.resource.garbage]]
      [:div
       [:span.resource.uranium]]])
   (for [cost (range 10 18 2)]
     [:div.resource-block {:data-resource-cost cost}
      [:span cost]
      [:div [:span.resource.uranium]]])])

(defn resource-name [r]
  (if (set? r)
    (clojure.string/join "-" (sort (map name r)))
    (name r)))

(extend-type powergrid.common.power-plants.PowerPlant
  dommy.template/PElement
  (-elem [{:keys [number resource capacity yield]}]
    (node [:div.power-plant
           {:class (format "%s power-plant-%d" (resource-name resource) number)}
           [:span.number number]
           [:span.capacity capacity]
           [:span.yield yield]])))

(deftemplate power-plants-tpl [power-plants]
  [:div#power-plants
   [:h3 "Power Plants"]
   [:div.market
    (map pp/plant (:market power-plants))]
   [:div.future
    (map pp/plant (:future power-plants))]])

(deftemplate auction-tpl [game {:keys [item price bidders] :as auction}]
  [:div.auction
   [:h3 "Auction"]
   [:div.auction-price (str "Current Bid: $" price)]
   [:ul.auction-bidders
    (map #(node [:li (p/handle (g/player game %))]) bidders)]
   [:div.auction-item item]
   [:div {:style {:clear "both"}}]])

(deftemplate game-tpl [{:keys [power-plants] :as game}]
  [:div#game
   [:div
    [:div (str "Step: " (g/current-step game))]
    [:div (str "Phase: " (g/current-phase game))]
    [:div (str "Round: " (g/current-round game))] ]
   [:div#players
    [:h3 "Players"]
    (g/players game)]
   (resources-tpl)
   (when-let [auction (g/auction game)] (auction-tpl game auction))
   (power-plants-tpl power-plants)])

(defn update-resource
  [{:keys [market pricing]} nodes]
  (let [rfill (- (count pricing) market)]
    (doall
      (map-indexed
        (fn [ind node]
          (dom/toggle-class! node "unavailable" (< ind rfill)))
        nodes))))

(defn update-resources
  [resources]
  (doseq [r [:coal :oil :garbage :uranium]]
    (update-resource
      (get resources r)
      (sel (str "#resources ." (name r))))))

(defn render-game [game]
  (dom/replace! (sel1 :#game) (game-tpl game))
  (update-resources (:resources game))
  (if-let [p (sel1 (str "#player-" (g/action-player-id game)))]
    (dom/add-class! p "has-action")))

(def current-game (atom {:id 1}))

(defn update-game []
  (remote-callback :game-state
                   [(@current-game :id)]
                   (fn [{:keys [game error] :as resp}]
                     (if game
                       (do
                         (.debug js/console (pr-str game))
                         (reset! current-game game)
                         (render-game game))
                       (.error js/console (or error resp "Failed game update"))))))

(defn- send-message [msg & [f]]
  (log "Sending message" msg)
  (remote-callback :send-message
                   [(@current-game :id) msg]
                   (or f update-game)))

(dom/listen! (sel1 :#update-game) :click update-game)

(defn render-debug-panel []
  (let [phase-msg-types {2 :bid
                         3 :buy
                         4 :buy
                         5 :sell}
        msg-tmpls {"Bid Power-Plant" "{:topic :phase2 :type :bid :player-id %player-id% :plant-id 3 :bid 3}"
                   "Buy Resources" "{:topic :phase3 :type :buy :player-id %player-id% :resources {:oil 0 :coal 0 :garbage 0 :uranium 0}}"
                   "Buy Cities" "{:topic :phase2 :type :buy :player-id %player-id% :new-cities []}"
                   "Power Cities" "{:topic :phase2 :type :sell :player-id %player-id% :powered-plants {}}"
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
    (dom/listen! (sel1 "#debug .update-game") :click update-game)
    (dom/listen! (sel1 "#debug .log-game") :click #(log @current-game))
    (dom/listen! (sel1 "#debug .reset-game") :click #(remote-callback :reset-game [] (update-game)))
    (dom/listen! [(sel1 :#debug) :.msg-tmpl] :click
                 (fn [e]
                   (dom/set-text! (sel1 "#debug .send-message textarea")
                                  (-> (dom/value (.-target e))
                                      (clojure.string/replace #"%player-id%" (str (g/action-player-id @current-game)))
                                      (clojure.string/replace #"%current-phase%" (str (g/current-phase @current-game)))
                                      (clojure.string/replace #"%phase-msg-type%" (str (phase-msg-types (g/current-phase @current-game))))
                                      ))))
    (dom/listen! (sel1 "#debug form.send-message") :submit
                 (fn [e]
                   (.preventDefault e)
                   (let [msg (read-string (dom/value (sel1 "#debug .send-message .message")))]
                     (if (and (map? msg) (every? msg [:topic :type]))
                       (do
                         (send-message msg)
                         (dom/set-text! (sel1 "#debug .send-message textarea") ""))
                       (.debug js/console "Invalid message")))))))

(render-debug-panel)
(update-game)
