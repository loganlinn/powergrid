(ns powergrid.messages.factory
  (:require (powergrid.messages [global :as global]
                                [phase2 :as phase2]
                                [phase3 :as phase3]
                                [phase4 :as phase4])
            [powergrid.util :refer [kw]]))

(def messages
  {:global global/messages
   :phase2 phase2/messages
   :phase3 phase3/messages
   :phase4 phase4/messages})

(defn create-message
  "Returns a message instance for a given message if one is registered for the
  message's topic and title. Otherwise returns original message"
  [{:keys [topic title] :as msg}]
  ((get-in messages [(kw topic) (kw title)] identity) msg))
