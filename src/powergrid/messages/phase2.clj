(ns powergrid.messages.phase2
  (:require [powergrid.message :refer [Message GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(def messages
  {[:phase2 :buy] nil
   [:phase2 :bid] nil
   [:phase2 :pass] nil})
