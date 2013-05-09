(ns powergrid.client
  (:use-macros [dommy.macros :only [sel sel1 node deftemplate]])
  (:require [dommy.core :as dommy]
            [powergrid.common.power-plants :as pp]
            ;[clojure.browser.repl :as repl]
            ))

;(repl/connect "http://localhost:9000/repl")

(defn log
  [& args]
  (.log js/console (pr-str args)))

(deftemplate player-tpl [{:keys [id username color money]}]
  [:div.player
   {:class (str "player-" (name color))
    :id (str "player-" id)}
   [:span.name username]
   [:div.money money]
   [:div.power-plants ""]])

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
       [:span.resource.oil]
       ]
      [:div
       [:span.resource.garbage]
       [:span.resource.garbage]
       [:span.resource.garbage]]
      [:div
       [:span.resource.uranium]]])
   (for [cost (range 10 18 2)]
     [:div {:data-resource-cost cost}
      [:span.resource-block-cost cost]
      [:div [:span.resource.uranium]]])])

(defn resource-name [r]
  (if (set? r)
    (clojure.string/join "-" (sort (map name r)))
    (name r)))

(deftemplate power-plant-tpl [{:keys [number resource capacity yield]}]
  [:div.power-plant
   {:class (format "%s power-plant-%d" (resource-name resource) number)}
   [:span.number number]
   [:span.capacity capacity]
   [:span.yield yield]])

(deftemplate power-plants-tpl [power-plants]
  [:div#power-plants
   [:h3 "Power Plants"]
   [:div.market
    (map power-plant-tpl (:market power-plants))]
   [:div.future
    (map power-plant-tpl (:future power-plants))]])

(deftemplate game-tpl [{:keys [players power-plants]}]
  [:div#game
   [:div#players [:h3 "Players"] (map player-tpl (vals players))]
   (resources-tpl)
   (power-plants-tpl power-plants)])

(defn update-resource
  [{:keys [market pricing]} nodes]
  (let [rfill (- (count pricing) market)]
    (doall
      (map-indexed
        (fn [ind node]
          (log ind node rfill)
          (dommy/toggle-class! node "unavailable" (< ind rfill)))
        nodes))))

(defn update-resources
  [resources]
  (doseq [r [:coal :oil :garbage :uranium]]
    (update-resource
      (get resources r)
      (sel (str "#resources ." (name r))))))

(let [std-pricing (for [p (range 1 9) _ (range 3)] p)
      uranium-pricing '(1 2 3 4 5 6 7 8 12 14 15 16)]
  (def mock-game {:players {1 {:id 1 :username "Logan" :color :red :money 50 :power-plants {}}
                            2 {:id 2 :username "Maeby" :color :black :money 50 :power-plants {}}}
                  :resources {:coal {:market 24 :supply 0 :pricing std-pricing}
                              :oil {:market 18 :supply 6 :pricing std-pricing}
                              :garbage {:market 6 :supply 18 :pricing std-pricing}
                              :uranium {:market 2 :supply 10 :pricing uranium-pricing}}
                  :power-plants {:market (pp/initial-market)
                                 :future (pp/initial-future)
                                 :deck (pp/initial-deck)}
                  :turn-order [1 2]
                  :turns '()}))

(dommy/replace! (sel1 :#game)
                (game-tpl mock-game))

(update-resources (:resources mock-game))


;(defn handle-click []
  ;(js/alert "Hello!"))
;(def clickable (.getElementById js/document "clickable"))
;(.addEventListener clickable "click" handle-click)
(dommy/listen! (sel1 :#clickable) :click #(js/alert "Helloheyy"))

