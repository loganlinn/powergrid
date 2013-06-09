(ns powergrid.dom-events
  (:require [powergrid.util.log :refer [debug info error spy]])
  )

(defn create-event
  [event-name event-data & {:keys [bubbles cancelable]
                            :or {bubbles true cancelable true}}]
  (js/CustomEvent.
    (name event-name)
    (clj->js {"bubbles" true
              "cancelable" true
              "detail" (js-obj)})))

(defn trigger
  ([node event-name]
   (trigger node event-name nil))
  ([node event-name event-data]
   (.dispatchEvent (spy node) (spy (create-event event-name event-data)))))
