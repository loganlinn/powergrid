(ns powergrid.message)

(defprotocol Message
  (validate [msg game]))

(defprotocol GameUpdate
  (update-game [update game]))
