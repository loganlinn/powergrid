(ns powergrid.message)

(defprotocol Validated
  (validate [msg game]))

(defrecord ValidateError [msg silent])

(defprotocol GameUpdate
  (update-game [update game]))
