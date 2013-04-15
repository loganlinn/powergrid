(ns powergrid.message)

(def topic :topic)
(def title :title)

(defrecord ValidateError [msg silent?])

(defprotocol Validated
  (validate [msg game]))

(defprotocol GameUpdate
  (update-game [update game]))

(defmulti passable? (fn [game msg-type] msg-type))

(defmethod passable? :default [_ _] false)

(comment
  ;; TODO
  (defevent EventName fields
    {:passable? passable
     :pre (fn [this game] true)}
    [game player]

    )
  )
