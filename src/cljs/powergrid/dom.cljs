(ns powergrid.dom)

(defn create-event
  [event-name event-data]
  (js/CustomEvent.
    event-name
    (clj->js {"bubbles" true
              "cancelable" true
              "detail" event-data})))

(defn trigger
  ([node event-name event-data]
   (.dispatchEvent node (create-event event-name event-data)))
  ([node event-name]
   (trigger event-name nil)))
