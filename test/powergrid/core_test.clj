(ns powergrid.core-test
  (:require [midje.sweet :refer :all]
            [powergrid.core :refer :all]
            [powergrid.common.player :as p]
            [powergrid.common.power-plants :as pp]
            [powergrid.game :as g]
            [powergrid.cities :as c]
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

(defn money-diff
  [game-before player-id diff]
  (fn [game-after]
    (= (- (p/money (g/player game-before player-id))
          (p/money (g/player game-after player-id)))
       diff)))

(fact "simple round"
  (let [game (tick (g/new-game [(p/new-player 1 "Player1" :blue)
                                (p/new-player 2 "Player2" :black)
                                (p/new-player 3 "Player3" :red)]))
        [a b c] (g/turns game)]
    (fact "phase2"
      (let [msgs (map msgs/create-message
                      [(msg :phase2 :bid a {:plant-id 3 :bid 3})
                       (msg :phase2 :bid b :pass)
                       (msg :phase2 :bid c :pass)
                       (msg :phase2 :bid b {:plant-id 4 :bid 4})
                       (msg :phase2 :bid c {:plant-id 4 :bid 5})
                       (msg :phase2 :bid b :pass)
                       (msg :phase2 :bid b {:plant-id 5 :bid 5})])
            states (vec (reductions update-game game msgs))]
        ;(pprint (map #(select-keys % [:auction :turns :turn-order :phase]) states))
        (fact "auction cleaned-up"
          (:auction (first states)) => falsey
          (:auction (last states))  => falsey
          (:auction (get states 3)) => falsey
          (:auction (get states 6)) => falsey)
        (fact "turn progression"
          (every? #{[a b c]} (map :turns (take 3 states))) => truthy
          (every? #{[b c]} (map :turns (take 3 (drop 3 states)))) => truthy
          (:turns (last (butlast states))) => [b])
        (fact "money deducted for power-plants"
                  (get states 3) => (money-diff (get states 0) a 3)
                  (get states 6) => (money-diff (get states 5) c 5)
                  (get states 7) => (money-diff (get states 6) b 5))
        (fact "received power-plants"
          (map #(p/owns-power-plant? (g/player (last states) %1) %2)
               [a b c] [3 5 4]) => [true true true])
        (fact "resulting phase, turns"
          (g/current-phase (last states)) => 3
          (g/turns (last states)) => (just [a c b]))


        (fact "phase 3"
          (let [game (last states)
                msgs (map msgs/create-message
                          [(msg :phase3 :buy a {:resources {:oil 2}})
                           (msg :phase3 :buy c {:resources {:coal 2}})
                           (msg :phase3 :buy b {:resources {:coal 1}})])
                states (vec (reductions update-game game msgs))
                pplants {a 3 b 5 c 4}
                resource-states (mapv #(g/map-players % (fn [p] (p/power-plant-resources p (pplants (p/id p))))) states)
                money-states (mapv #(g/map-players % p/money) states) ]
            ;(pprint (map #(-> (select-keys % [:turns :phase])
            ;(assoc :players (g/map-players % (fn [p] (select-keys p [:money :power-plants]))))
            ;(assoc :resources (g/map-resources % (fn [r] (select-keys r [:market :supply])))))
            ;states))

            (fact "received resources"
              (get resource-states 0) => {a {} b {} c {}}
              (get resource-states 1) => {a {:oil 2} b {} c {}}
              (get resource-states 2) => {a {:oil 2} b {} c {:coal 2}}
              (get resource-states 3) => {a {:oil 2} b {:coal 1} c {:coal 2}})
            (fact "money changes every turn"
              (distinct money-states) => money-states)
            (fact "resulting phase, turns"
              (g/current-phase (last states)) => 4
              (g/turns (last states)) => (just [a c b]))

            (fact "phase 4"
              (let [game (last states)
                    msgs (map msgs/create-message
                              [(msg :phase4 :buy a {:new-cities [:philadelphia :new-york]})
                               (msg :phase4 :buy c {:new-cities [:atlanta]})
                               (msg :phase4 :buy b :pass)])
                    states (vec (reductions update-game game msgs))]
                ;(pprint (map #(-> (select-keys % [:turns :turn-order :phase])
                                  ;(assoc :cities (g/map-players % (fn [p]  (c/owned-cities (g/cities %) (p/id p)))  ))
                                  ;(assoc :players (g/map-players % (fn [p] (select-keys p [:money :power-plants])))))
                             ;states))
                (fact "received cities"
                  (c/owner? (g/cities (get states 1)) a :philadelphia) => truthy
                  (c/owner? (g/cities (get states 1)) a :new-york) => truthy
                  (c/owner? (g/cities (get states 2)) c :atlanta) => truthy
                  (c/owned-cities (g/cities (get states 2)) b) => empty?)
                (fact "money deducted for cities"
                  (get states 1) => (money-diff (get states 0) a 20)
                  (get states 2) => (money-diff (get states 1) c 10)
                  (get states 3) => (money-diff (get states 2) b 0))
                (fact "resulting phase, turns"
                  (g/current-phase (last states)) => 5
                  (g/turns (last states)) => (just [b c a]))

                (fact "phase 5"
                  (let [game (last states)
                        msgs (map msgs/create-message
                                  [(msg :phase5 :sell b :pass)
                                   (msg :phase5 :sell c {:powered-plants {4 {:coal 2}}})
                                   (msg :phase5 :sell a {:powered-plants {3 {:oil 2}}})])
                        states (vec (reductions update-game game msgs))]
                    ;(pprint (map #(-> (select-keys % [:turns :phase])
                                      ;(assoc :cities (g/map-players % (fn [p]  (c/owned-cities (g/cities %) (p/id p)))  ))
                                      ;(assoc :players (g/map-players % (fn [p] (select-keys p [:money :power-plants])))))
                                 ;states))
                    ;; money
                    (fact "recieved money for powering"
                      (get states 1) => (money-diff (get states 0) b (- 10)))
                      (get states 2) => (money-diff (get states 1) c (- 22))
                      (get states 3) => (money-diff (get states 2) a (- 22))))
                ))))
        ))))
