(ns powergrid.ui.resources
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [powergrid.domain.resource :as resource]))


(def ^:private resource-els
  {:coal #(dom/div #js {:className "resource coal"})
   :oil #(dom/div #js {:className "resource oil"})
   :garbage #(dom/div #js {:className "resource garbage"})
   :uranium #(dom/div #js {:className "resource uranium"})})

(def resource-prices [1 2 3 4 5 6 7 8 12 14 15 16])

(defn- resource-class [r avail i]
  (str "resource "
       (name r)
       (when (>= i avail) " unavailable")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Components

(defn resource-market [resources owner]
  (reify
    om/IRender
    (render [_]
      (let [avail-by-price (into {} (map (fn [[k r]] [k (frequencies (resource/market-pricing r))])
                                         (seq resources)))]
        (apply dom/ul nil
               (map (fn [price]
                      (dom/li #js {:data-resource-price price}
                              (dom/span #js {:className "resource-price"} price)
                              (apply dom/ul nil
                                     (reduce (fn [els r]
                                               (let [capacity (get-in resource/market-capacity [r price] 0)
                                                     avail    (get-in avail-by-price [r price] 0)]
                                                 (concat els
                                                         (for [i (range capacity)]
                                                           (dom/li #js {:className (resource-class r avail i)})))))
                                             []
                                             [:coal :oil :uranium :garbage]))))
                    resource-prices))))))
