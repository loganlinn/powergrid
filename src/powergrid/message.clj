(ns powergrid.message)

(defrecord ValidateError [msg silent?])

(defprotocol Validated
  (validate [msg game]))

(defprotocol GameUpdate
  (update-game [update game]))

(defmulti passable? (fn [game msg-type] msg-type))

(defmethod passable? :default [_ _] false)
