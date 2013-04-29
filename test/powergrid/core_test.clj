(ns powergrid.core-test
  (:require [midje.sweet :refer :all]
            [powergrid.core :refer :all]
            [powergrid.player :as p]
            [powergrid.power-plants :as pp]
            [powergrid.game :as g]
            [powergrid.message :as msg]
            [powergrid.messages.factory :as msgs]
            ))

(fact player-order
  (player-order ...game... [...Dale... ...Anna... ...Angelika... ...Valerie...])
  => [...Anna... ...Dale... ...Angelika... ...Valerie...]
  (provided
    ;(p/id ...Dale...)     => 3
    ;(p/id ...Anna...)     => 1
    ;(p/id ...Angelika...) => 2
    ;(p/id ...Valerie...)  => 4
    (g/network-size ...game... ...Dale...)     => 5
    (g/network-size ...game... ...Anna...)     => 6
    (g/network-size ...game... ...Angelika...) => 5
    (g/network-size ...game... ...Valerie....) => 4
    (p/max-power-plant ...Dale...)     => 17
    (p/max-power-plant ...Anna...)     => ...dont-care...
    (p/max-power-plant ...Angelika...) => 15
    (p/max-power-plant ...Valerie...)  => ...dont-care...))


(defn msg
  [topic title player-id m]
  (if (= m :pass)
    {:topic topic
     :title title
     :player-id player-id
     msg/pass true}
    (assoc m
           :topic topic
           :title title
           :player-id player-id)))

(let [game (g/new-game [(p/new-player 1 nil :blue)
                        (p/new-player 2 nil :black)
                        (p/new-player 3 nil :red)])]
  (fact "phase2 - simple auction"
    (let [game (next-phase game)
          msgs (map msgs/create-message
                    [(msg :phase2 :bid 1 {:plant-id 3 :bid 3})
                     (msg :phase2 :bid 2 :pass)
                     (msg :phase2 :bid 3 :pass)
                     (msg :phase2 :bid 2 {:plant-id 4 :bid 4})
                     (msg :phase2 :bid 3 {:plant-id 4 :bid 5})
                     (msg :phase2 :bid 2 :pass)
                     (msg :phase2 :bid 2 {:plant-id 5 :bid 5})])
          states (reductions update-game (next-phase game) msgs)]
      ;(pprint (map #(select-keys % [:auction :turns ]) states))
      (:auction (first states)) => falsey
      (:auction (last states))  => falsey
      (:auction (get states 3)) => falsey
      (:auction (get states 6)) => falsey
      (every? #{[1 2 3]} (take 3 (map :turns states))) => truthy
      (every? #{[2 3]} (take 3 (drop 3 (map :turns states)))) => truthy
      (:turns (last (butlast states))) => [2]
      (:turns (last states)) => empty?
      (map #(p/money (g/player (last states) %)) [1 2 3]) => [47 45 45]
      (map #(p/owns-power-plant? (g/player (last states) %1) %2)
           [1 2 3] (map pp/plant [3 5 4])) => [true true true])))
