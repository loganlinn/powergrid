(ns powergrid.ui.auction
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn auction-view [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil))))
