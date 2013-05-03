(ns powergrid.core-test
  (:require [midje.sweet :refer :all]
            [powergrid.core :refer :all]
            [powergrid.player :as p]
            [powergrid.power-plants :as pp]
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

(fact "simple game"
  (let [game (tick (g/new-game [(p/new-player 1 :blue)
                                (p/new-player 2 :black)
                                (p/new-player 3 :red)]))
        [a b c] (g/turns game)
        auction1 (map msgs/create-message
                      [(msg :phase2 :bid a {:plant-id 3 :bid 3})
                       (msg :phase2 :bid b :pass)
                       (msg :phase2 :bid c :pass)
                       (msg :phase2 :bid b {:plant-id 4 :bid 4})
                       (msg :phase2 :bid c {:plant-id 4 :bid 5})
                       (msg :phase2 :bid b :pass)
                       (msg :phase2 :bid b {:plant-id 5 :bid 5})])]
    (fact "phase2"
      (let [states (reductions update-game game auction1)]
        ;(pprint (map #(select-keys % [:auction :turns :phase]) states))
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
        (g/turns (last states)) => (just [a c b])


        (fact "phase 3"
          (let [game (last states)
                msgs (map msgs/create-message
                          [(msg :phase3 :buy a {:resources {:oil 2}})
                           (msg :phase3 :buy c {:resources {:coal 2}})
                           (msg :phase3 :buy b {:resources {:coal 1}})])
                states (vec (reductions update-game game msgs))
                pplants {a (pp/plant 3) b (pp/plant 5) c (pp/plant 4)}
                resource-states (mapv #(g/map-players % (fn [p] (p/power-plant-resources p (pplants (p/id p))))) states)
                money-states (mapv #(g/map-players % p/money) states) ]
            ;(pprint (map #(-> (select-keys % [:turns :phase])
            ;(assoc :players (g/map-players % (fn [p] (select-keys p [:money :power-plants]))))
            ;(assoc :resources (g/map-resources % (fn [r] (select-keys r [:market :supply])))))
            ;states))

            (get resource-states 0) => {a {} b {} c {}}
            (get resource-states 1) => {a {:oil 2} b {} c {}}
            (get resource-states 2) => {a {:oil 2} b {} c {:coal 2}}
            (get resource-states 3) => {a {:oil 2} b {:coal 1} c {:coal 2}}
            (distinct money-states) => money-states
            (:turns (last states)) => (just [1 3 2])

            (fact "phase 4"
              (let [game (last states)
                    msgs (map msgs/create-message
                              [(msg :phase4 :buy 1 {:new-cities [:boston :new-york]})
                               (msg :phase4 :buy 3 :pass)
                               (msg :phase4 :buy 2 :pass)])
                    states (reductions update-game game msgs)]
                (pprint (map #(-> (select-keys % [:turns :phase])
                                  (assoc :cities (g/map-players % (fn [p]  (c/owned-cities (g/cities %) (p/id p)))  ))
                                  (assoc :players (g/map-players % (fn [p] (select-keys p [:money :power-plants])))))
                             states))

                ;(fact "phase 5"
                  ;(let [game (last states)
                        ;msgs (map msgs/create-message
                                  ;[(msg :phase5 :sell 1 :pass)
                                   ;(msg :phase5 :sell 3 :pass)
                                   ;(msg :phase5 :sell 2 :pass)])
                        ;states (reductions update-game game msgs)]
                    ;(pprint (map #(-> (select-keys % [:turns :phase])
                                      ;(assoc :cities (g/map-players % (fn [p]  (c/owned-cities (g/cities %) (p/id p)))  ))
                                      ;(assoc :players (g/map-players % (fn [p] (select-keys p [:money :power-plants])))))
                                 ;states))

                    ;))
                )))) ))))
