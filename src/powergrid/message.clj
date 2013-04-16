(ns powergrid.message
  (:require [powergrid.game :as g]
            [slingshot.slingshot :refer [throw+]]))

(def topic :topic)
(def title :title)

(defrecord ValidateError [msg silent?])

(defprotocol Validated
  (validate [msg game] "Retruns string of validation message if msg is invalid, otherwise nil"))

(defn base-validate
  [{:keys [player-id] :as msg} game]

  ;; TODO ensure msg is authorized (check users match)

  (cond
    (not (g/player player-id)) "Invalid player"))

(defn throw-validation-errors
  [msg game]
  (when-let [error (or (base-validate msg game)
                       (validate msg game))]
    (throw+ (->ValidateError error))))

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
