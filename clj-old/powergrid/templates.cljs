(ns powergrid.templates
  (:use-macros [dommy.macros :only [sel sel1 node deftemplate]])
  (:require [powergrid.common.game :as g]
            [powergrid.common.player :as p]
            [powergrid.common.cities :as c]
            [powergrid.common.auction :as a]
            [powergrid.common.resource :as r]
            [powergrid.common.power-plants :as pp]
            [dommy.template]
            [dommy.core :as dom]))

(defn- resource-name [r]
  (if (set? r)
    (clojure.string/join "-" (sort (map name r)))
    (name r)))

(def resource-el
  {:coal [:span.resource.coal]
   :oil  [:span.resource.oil]
   :garbage [:span.resource.garbage]
   :uranium [:span.resource.uranium]})

;; PLAYER

(extend-type powergrid.common.player.Player
  dommy.template/PElement
  (-elem [{:keys [id handle color money] :as player}]
    (letfn [(pp-rsrcs-tpl [pp-rsrcs]
              (node [:div.power-plant-resources
                     (mapcat
                       #(repeat (val %) (resource-el (key %)))
                       pp-rsrcs)]))
            (pps-tpl [player pps]
              (map #(vector :li (pp/plant %)
                            (pp-rsrcs-tpl (p/power-plant-resources player %)))
                   pps))]
      (node [:div.player
             {:class (str "player-" (name color))}
             [:span.handle handle]
             [:div.player-icon]
             [:div.money (str "$" money)]
             [:ul.player-power-plants (pps-tpl player (p/power-plants player))]]))))

;; RESOURCES

(deftemplate resources-tpl []
  (let [coal-el (resource-el :coal)
        oil-el  (resource-el :oil)
        garbage-el (resource-el :garbage)
        uranium-el (resource-el :uranium)]
   [:div#resources
   [:h3 "Resources"]
   (for [cost (range 1 9)]
     [:div.resource-block
      {:data-resource-cost cost}
      [:span.resource-block-cost cost]
      [:div (repeat 3 coal-el)]
      [:div (repeat 3 oil-el)]
      [:div (repeat 3 garbage-el)]
      [:div uranium-el]])
   (for [cost (range 10 18 2)]
     [:div.resource-block {:data-resource-cost cost}
      [:span cost] [:div uranium-el]])]))

;; POWER PLANTS

(extend-type powergrid.common.power-plants.PowerPlant
  dommy.template/PElement
  (-elem [{:keys [number resource capacity yield]}]
    (node [:div.power-plant
           {:class (format "%s power-plant-%d" (resource-name resource) number)}
           [:span.number number]
           [:span.capacity capacity]
           [:span.yield yield]])))

(deftemplate power-plants-tpl [power-plants]
  (let [plant-node #(node [:li (pp/plant %)])]
    [:div#power-plants
     [:h3 "Power Plants"]
     [:ul.market
      (map plant-node (:market power-plants))]
     [:ul.future
      (map plant-node (:future power-plants))]]))

;; AUCTION

(deftemplate auction-tpl [game {:keys [item price bidders] :as auction}]
  [:div.auction
   [:h3 "Auction"]
   [:div.auction-price (str "Current Bid: $" price)]
   [:ul.auction-bidders
    (map #(node [:li (p/handle (g/player game %))]) bidders)]
   [:div.auction-item item]
   [:div {:style {:clear "both"}}]])

;; CITIES

(deftemplate player-cities-tpl [game player]
  [:ul.player-cities
   (map #(vector :li (name %)) (c/owned-cities (g/cities game) (p/id player)))])

;; COUNTRY

;; GAME / LAYOUT

(defn- turn-order-map
  "Returns maping from player-id to index in player order"
  [game]
  (into {} (map-indexed #(vector %2 %1) (:turn-order game))))

(deftemplate game-tpl [{:keys [step phase round power-plants] :as game}]
  [:div#game
   [:div
    [:div (str "Step: " step)]
    [:div (format "Phase: %d (%s)" phase (g/phase-title phase))]
    [:div (str "Round: " round)]]
   [:div#players
    [:h3 "Players"]
    (mapcat (juxt identity (partial player-cities-tpl game))
            (sort-by (comp (turn-order-map game) p/id) (g/players game)))]
   (resources-tpl)
   (when-let [auction (g/auction game)] (auction-tpl game auction))
   (power-plants-tpl power-plants)
   [:div#game-map]])

