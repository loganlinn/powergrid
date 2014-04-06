(ns powergrid.ui.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))


(defn nav [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/nav ))))
