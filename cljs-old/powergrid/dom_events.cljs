(ns powergrid.dom-events
  (:require [powergrid.util.log :refer [debug info error spy]])
  )

(defn create-event
  [event-name event-data & {:keys [bubbles cancelable]
                            :or {bubbles true cancelable true}}]
  (js/CustomEvent.
    (name event-name)
    (doto (js-obj)
      (aset "bubbles" bubbles)
      (aset "cancelable" cancelable)
      (aset "detail" event-data))))

(defn trigger
  ([node event-name]
   (trigger node event-name nil))
  ([node event-name event-data]
   (.dispatchEvent (spy node) (create-event event-name event-data))))
