(ns powergrid.message)

(defrecord ValidateError [msg silent?])

(defprotocol Validated
  (validate [msg game]))

(defprotocol GameUpdate
  (update-game [update game]))
