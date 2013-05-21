(ns powergrid.country
  (:require [powergrid.country.usa]))

(defn render-country
  [container-id]
  (let [r (js/Raphael. container-id 1000 1000)
        attrs (clj->js
                {"fill" "#d3d3d3"
                 "stroke" "#fff"
                 "stroke-opacity" "1"
                 "stroke-linejoin" "round"
                 "stroke-miterlimit" "4"
                 "stroke-width" "0.75"
                 "stroke-dasharray" "none"})]
    (doseq [path powergrid.country.usa/paths]
      (.attr (.path r path) attrs))))
