(ns powergrid.style
  (:require [garden.core :as css]
            [garden.units :refer [em px]]
            [garden.color :as color]))

(def bg-color (color/rgb 255 255 255))

(def oil-color (color/rgb [3 3 3]))

(def coal-color (color/rgb [152 118 84]))

(def garbage-color (color/rgb [255 255 0]))

(def uranium-color (color/rgb [255 0 0]))

(def resource-colors
  {:oil oil-color
   :coal coal-color
   :garbage garbage-color
   :uranium uranium-color})

(defn unavailable-resource [r]
  {:background-color (color/lighten (get resource-colors r) 30)
   :border-color "#666"})

(def styles
  [[:* {:box-sizing :border-box}]

   [:.unselectable
    {:-webkit-user-select :none
     :-moz-user-select :-moz-none
     :-khtml-user-select :none
     :-ms-user-select :none
     :user-select :none}]

   [:.cities
    [:.regions
     [:path {:fill "#ccc" :stroke bg-color}]]
    [:.connections
     [:path {:stroke "#333" :stroke-width 2}]]]

   [:.power-plants
    {:position :absolute
     :top (px 10)
     :right (px 10)}]
   [:.resources
    {:position :absolute
     :bottom (px 10)}]
   [:.cities
    {:position :absolute
     :top "20%"
     :bottom "20%"
     :right 0
     :left 0}]

   [:.present-market :.future-market
    {:margin [(px 0) (px 0) (px 10) (px 0)]
     :padding 0}
    [:ul {:clear :both}
     [:li {:float :left}]]]
   [:.power-plant
    {:width (em 4)
     :height (em 4)
     :margin-right (px 10)
     :position :relative
     :list-style :none
     :display :inline-block}
    [:.number {:font-size (em 2)
               :position :absolute
               :top (px 5)
               :right (px 5)}]
    [:.capacity {:position :absolute
                 :bottom (px 5)
                 :left (px 5)}]
    [:.yield {:position :absolute
              :bottom (px 5)
              :right (px 5)}]]

   [:.resource-track
    {:padding 0
     :margin 0
     :width "100%"}
    [:li {:list-style :none
          :float :left}]]
   [:.resource-block
    {:clear :both
     :width "4em"
     :height "4em"
     :margin 0
     :padding 0
     :position :relative}
    [:.resource {:height "31.333333%"
                 :border "1px solid black"
                 :margin "1%"
                 :float :left
                 :list-style :none
                 :display :inline-block}]
    [:.resource.unavailable {:border-color "#666"}]

    [:.resource.coal {:width "31.333333%"}]
    [:.resource.oil {:width "23%"}]
    [:.resource.garbage {:width "31.333333%"}]
    [:.resource.uranium {:width "23%"}]

    [:.resource.unavailable.coal {:background-color (color/lighten coal-color 30)}]
    [:.resource.unavailable.oil {:background-color (color/lighten oil-color 30)}]
    [:.resource.unavailable.garbage {:background-color (color/lighten garbage-color 30)}]
    [:.resource.unavailable.uranium {:background-color (color/lighten uranium-color 30)}]]

   [:.oil {:background-color oil-color :color "#ccc"}]
   [:.coal {:background-color coal-color}]
   [:.garbage {:background-color garbage-color}]
   [:.uranium {:background-color uranium-color}]])

(defn compile-styles []
  (css/css {:pretty-print? false} styles))

#+cljs
;; hacky styles (TODO make a real asset)
(let [h (or (.-head js/document) (aget (.getElementsByTagName js/document "head") 0))
      s (.createElement js/document "style")
      css (compile-styles)]
  (set! (.-type s) "text/css")
  (if (.-styleSheet s)
    (set! (.-cssText (.-styleSheet s)) css)
    (.appendChild s (.createTextNode js/document css)))
  (.appendChild h s))
