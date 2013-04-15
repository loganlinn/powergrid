(ns powergrid.messages.factory
  (:require (powergrid.messages [global :as global]
                                [phase2 :as phase2]
                                [phase3 :as phase3]
                                [phase4 :as phase4])))

(def messages
  {:global global/messages
   :phase2 phase2/messages
   :phase3 phase3/messages
   :phase4 phase4/messages})

