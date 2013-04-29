(ns powergrid.core-test
  (:require [midje.sweet :refer :all]
            [powergrid.core :refer :all]
            [powergrid.player :as p]
            [powergrid.power-plants :as pp]
            [powergrid.game :as g]
            [powergrid.message :as msg]
            [powergrid.messages.factory :as msgs]))

(defn msg
  [topic title player-id m]
  (if (= m :pass)
    {msg/topic topic
     msg/title title
     msg/pass true
     :player-id player-id}
    (assoc m
           msg/topic topic
           msg/title title
           :player-id player-id)))

(use 'clojure.pprint)

(let [game (g/new-game [(p/new-player 1 nil :blue)
                        (p/new-player 2 nil :black)
                        (p/new-player 3 nil :red)])]
  (fact "phase2 - simple auction"
    (let [game (tick game)
          [a b c] (g/turns game)
          msgs (map msgs/create-message
                    [(msg :phase2 :bid a {:plant-id 3 :bid 3})
                     (msg :phase2 :bid b :pass)
                     (msg :phase2 :bid c :pass)
                     (msg :phase2 :bid b {:plant-id 4 :bid 4})
                     (msg :phase2 :bid c {:plant-id 4 :bid 5})
                     (msg :phase2 :bid b :pass)
                     (msg :phase2 :bid b {:plant-id 5 :bid 5})])
          states (reductions update-game game msgs)]
      ;(pprint (map #(select-keys % [:auction :turns :phase]) (cons game states)))
      (:auction (first states)) => falsey
      (:auction (last states))  => falsey
      (:auction (get states 3)) => falsey
      (:auction (get states 6)) => falsey
      (every? #{[a b c]} (map :turns (take 3 states))) => truthy
      (every? #{[b c]} (map :turns (take 3 (drop 3 states)))) => truthy
      (:turns (last (butlast states))) => [b]
      (g/current-phase (last states)) => c
      (map #(p/money (g/player (last states) %)) [a b c]) => [47 45 45]
      (map #(p/owns-power-plant? (g/player (last states) %1) %2)
           [a b c] (map pp/plant [3 5 4])) => [true true true]
      (g/turns (last states)) => [b c a])))
