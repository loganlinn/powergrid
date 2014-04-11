(ns powergrid.ui.cities
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [powergrid.domain.country.usa]
            [powergrid.ui.usa :as usa]
            [powergrid.domain.cities :as cities]))

(defn- page-xy [e] [(.-pageX e) (.-pageY e)])

(defn map-view [data owner]
  (reify
    om/IRender
    (render [this]
      (let [{:keys [projection path arc city-radius city-coords]} data]
        (dom/svg #js {:xmlns "http://www.w3.org/2000/svg"
                      :version "1.1"
                      :width (:width data)
                      :height (:height data)}
                 (apply dom/g #js {:className "regions"}
                        (for [region (:regions data)]
                          (dom/path #js {:d (path region)})))
                 (apply dom/g #js {:className "cities"}
                        (for [[city xy] city-coords
                              :let [[x y] (projection (clj->js xy))
                                    city-name (name city)]]
                          (dom/g nil
                                 (dom/circle #js {:cx x
                                                  :cy y
                                                  :r city-radius})
                                 (dom/text #js {:x x
                                                :y (+ y (* city-radius 2) 5)
                                                :textAnchor "middle"} ;; TODO Debug textAnchor
                                           city-name))))
                 (apply dom/g #js {:className "connections"}
                        (for [[[c1 c2] cost] (:city-connections data)
                              :let [[x1 y1] (get city-coords c1)
                                    [x2 y2] (get city-coords c2)
                                    xy1 #js [x1 y1]
                                    xy2 #js [x2 y2]]]
                          (dom/path #js {:d (path (arc {:source xy1 :target xy2}))}))))))))

(defn cities-view [data owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [projection (-> (.-geo js/d3)
                           (.azimuthal)
                           (.mode "equidistant")
                           (.origin #js [-98 38])
                           (.scale 1400)
                           (.translate #js [540 360]))
            path (-> (.-geo js/d3)
                     (.path)
                     (.projection projection))
            arc (-> (.-geo js/d3)
                    (.greatArc)
                    ;; for some reason keyword-as-a-function doesn't work here
                    (.source #(:source %))
                    (.target #(:target %)))]
        {:projection projection
         :path path
         :arc arc
         :regions nil
         :mouse nil}))

    om/IDidMount
    (did-mount [_]
      (.json js/d3 "deps/us-states.json" #(om/set-state! owner :regions (.-features %))))

    om/IRenderState
    (render-state [_ {:keys [regions projection path arc] :as state}]
      (dom/div #js {:onMouseDown (fn [e]
                                   (om/set-state! owner :mouse
                                                  {:origin (.translate (om/get-state owner :projection))
                                                   :start (page-xy e)}))
                    :onMouseMove #(when-let [mouse (om/get-state owner :mouse)]
                                    (let [projection (om/get-state owner :projection)
                                          [origin-x origin-y] (:origin mouse)
                                          [start-x start-y] (:start mouse)
                                          [end-x end-y] (page-xy %)]
                                      (.translate projection #js [(+ origin-x (/ (- end-x start-x) 2))
                                                                  (+ origin-y (/ (- end-y start-y) 2))])
                                      (om/refresh! owner)))
                    :onMouseUp #(om/set-state! owner :mouse nil)}
               (when regions
                 (om/build map-view {:projection projection
                                     :path path
                                     :arc arc
                                     :regions regions
                                     :city-coords usa/city-coords
                                     :city-connections powergrid.domain.country.usa/connections
                                     :width "100%";; TODO
                                     :height "100%";; TODO
                                     :city-radius 5
                                     }))))))
