(ns powergrid.ui.power-plants
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn resource-name [r]
  (if (set? r)
    (clojure.string/join "-" (sort (map name r)))
    (name r)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Components


(defn power-plant-card [data owner]
  (reify
    om/IRender
    (render [this]
      (let [{:keys [number resource capacity yield]} data]
        (dom/li #js {:className (str "power-plant " (resource-name resource) " pp-" number)}
                (dom/span #js {:className "number"} number)
                (dom/span #js {:className "capacity"} capacity)
                (dom/span #js {:className "yield"} yield))))))

(defn power-plant-market [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div
       nil
       (apply dom/ul
              #js {:className "present-market"}
              (om/build-all power-plant-card (:market data)
                            {:key :number}))
       (apply dom/ul
              #js {:className "future-market"}
              (om/build-all power-plant-card (:future data)
                            {:key :number}))))))
