(ns powergrid.messages.phase4
  (:require [powergrid.message :refer [Message GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(def messages
  {:buy nil
   :trash nil
   :pass nil
   :end nil})
