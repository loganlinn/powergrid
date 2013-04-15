(ns powergrid.scratch
  (:require [powergrid.core :refer :all]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]
            [clojure.pprint :refer :all]))

(let [game (g/new-game [(p/new-player 1 nil :blue) (p/new-player 2 nil :black)])
      plant1 {:number 36, :resource :coal, :capacity 3, :yield 7}
      plant2 {:number 17, :resource :uranium, :capacity 1, :yield 2}
      plant3 {:number 12, :resource #{:coal :oil}, :capacity 2, :yield 2}
      [p1 p2] (g/players game)
      game (-> game
               (update-player p1 p/add-power-plant plant1)
               (update-player p1 p/add-power-plant plant3)
               (update-player p1 p/assign-resource plant1 :coal 5)
               (update-player p1 p/assign-resource plant3 :coal 1)
               (update-player p2 p/add-power-plant plant2)
               (update-player p2 p/add-city :norfolk))
      [p1 p2] (g/players game)]
  (pprint game)
  (pprint (p/resource-capacities p1)))
