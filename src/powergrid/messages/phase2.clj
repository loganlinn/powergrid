(ns powergrid.messages.phase2
  (:require [powergrid.message :refer [Validated GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]))

(def messages
  {:buy nil
   :bid nil
   :pass nil})
