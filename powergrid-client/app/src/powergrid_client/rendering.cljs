(ns powergrid-client.rendering
  (:require [dommy.core :as dom]
            [io.pedestal.app.util.log :as log]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.render.push.handlers.automatic :as d]
            [io.pedestal.app.render.push.handlers :as h])
  (:require-macros [powergrid-client.html-templates :as html-templates]
                   [dommy.macros :refer [sel sel1 node deftemplate]]))

(defn- by-id [id]
  (sel1 (str "#" id)))

(def templates (html-templates/powergrid-client-templates))

(defn game-tmpl
  [delta]
  [:div
   [:div#power-plant-market
   [:div.power-plant-market]
   [:div.power-plant-future]]])

(defn render-template [template-fn initial-value-fn]
  (fn [renderer [_ path :as delta] input-queue]
    (let [parent (render/get-parent-id renderer path)
          id (name (render/new-id! renderer path))]
      (dom/append! (by-id parent) (-> (template-fn delta)
                                      (dom/set-attr! :id id))))))

(defn render-value [renderer [_ path _ new-value] input-queue]
  (let [key (last path)]
    (templates/update-t renderer [:game] {key (str new-value)})))

(defn render-new-id [id]
  (fn [renderer [_ path] _]
    (render/new-id! renderer path id)))

(defn- resource-name [r]
  (cond
    (set? r) (clojure.string/join "-" (sort (map name r)))
    (keyword? r) (name r)
    :else r))

(deftemplate power-plant-tmpl [{:keys [number resource capacity yield] :as plant}]
  [:div.power-plant
   {:class (format "%s power-plant-%d" (resource-name resource) number)}
   [:span.number number]
   [:span.capacity capacity]
   [:span.yield yield]])

(defn render-power-plant-market
  [renderer [_ path _ new-value] input-queue]
  (let [parent (render/get-parent-id renderer path)
        market-type (name (last path))
        market-el (sel1 (by-id parent) (str ".power-plant-" market-type))
        html (:power-plant templates)]
    (dom/append! market-el (map power-plant-tmpl new-value))
    ))

(defn render-config []
  [[:node-create  [:game] (render-template game-tmpl (constantly {:game-id ""}))]
   [:node-destroy [:game] h/default-destroy]

   [:value [:game :*] render-value]
   [:value [:pedestal :debug :*] render-value]
   ;[:node-create  [:main :game :power-plant-market]
   ;(render-template :power-plant-market (fn [[_ path]]
   ;(log/debug :in [:main :game :power-plant-market] :path path)
   ;{}))]
   [:node-create [:game :power-plant-market] (render-new-id "power-plant-market")]
   [:value [:game :power-plant-market :*] render-power-plant-market]
   ;[:value [:main :game :power-plant-market :* :*] render-value]
   ;[:node-destroy [:main :game :power-plant-market] h/default-destroy]
   ;[:node-destroy [:main :game :power-plant-market :* :*] h/default-destroy]
   ;[:node-create  [:main :**] render-element]
   ;[:node-destroy [:main :**] h/default-destroy]
   ])
