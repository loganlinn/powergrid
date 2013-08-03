(ns powergrid.domain.player-test
  (:require [midje.sweet :refer :all]
            [powergrid.domain.protocols :as pc]
            [powergrid.domain.player :refer :all]
            [powergrid.domain.power-plants :as pp]))

(defn mock-player
  [& args]
  (let [p (new-player "Player1" :red)]
    (if (seq args)
      (apply assoc p args)
      p)))

(tabular
  (fact valid-color? (valid-color? ?color) => ?expected)
  ?color  ?expected
  :blue   truthy
  "blue"  falsey
  ""      falsey
  :orange falsey)


(fact set-color
  (:color (set-color (mock-player :color :red) :blue)) => :blue
  (set-color ...p... :orange) => (throws AssertionError))

(fact max-power-plant
  (max-power-plant ...p...) => 25
  (provided
    (power-plants ...p...) => [3 25 13]))

(fact update-money
  (money (update-money (mock-player :money 50) 10)) => 60
  (money (update-money (mock-player :money 50) (- 10))) => 40)


(fact can-afford?
  (can-afford? (mock-player :money 50) 49) => truthy
  (can-afford? (mock-player :money 50) 50) => truthy
  (can-afford? (mock-player :money 50) 51) => falsey)

(fact can-buy-resource?
  (fact
    (can-buy-resource? ...p... ...r...) => truthy
    (provided
      (power-plants ...p...) => [...pp1... ...pp2... ...pp3...]
      (pp/accepts-resource? ...pp1... ...r...) => false
      (pp/accepts-resource? ...pp2... ...r...) => false
      (pp/accepts-resource? ...pp3... ...r...) => true))
  (fact
    (can-buy-resource? ...p... ...r...) => falsey
    (provided
      (power-plants ...p...) => [...pp1... ...pp2... ...pp3...]
      (pp/accepts-resource? ...pp1... ...r...) => false
      (pp/accepts-resource? ...pp2... ...r...) => false
      (pp/accepts-resource? ...pp3... ...r...) => false)))

(fact add-power-plant
  (-> (mock-player)
      (add-power-plant 19)
      (owns-power-plant? 19)) => truthy)

(fact remove-power-plant
  (let [plr (-> (mock-player)
                (add-power-plant 19)
                (add-power-plant 20)
                (remove-power-plant 19))]
    (owns-power-plant? plr 19) => falsey
    (owns-power-plant? plr 20) => truthy))

(fact add-power-plant-resources
  (-> (mock-player)
      (add-power-plant ...ppid...)
      (add-power-plant-resources ...ppid... ...r... 4)
      (add-power-plant-resources ...ppid... ...r... 1)
      (power-plant-resources ...ppid...))
  => {...r... 5}
  (provided
    (pp/plant ...ppid...) => ...pp...
    (pp/accepts-resource? ...pp... ...r...) => true))

(fact pc/accept-resource
  (let [p (-> (mock-player)
              (add-power-plant 3) ; oil
              (add-power-plant 4) ; coal
              (add-power-plant 5))] ; coal/oil hybrid
    (fact "empty initially"
      (:oil (power-plant-resources p 3) 0) => 0
      (:oil (power-plant-resources p 4) 0) => 0
      (:oil (power-plant-resources p 5) 0) => 0)
    (fact "fills to capacity"
      (let [p  (pc/accept-resource p :oil 4)]
        (:oil (power-plant-resources p 3) 0) => 4
        (:oil (power-plant-resources p 4) 0) => 0
        (:oil (power-plant-resources p 5) 0) => 0))
    (fact "adds to hybrid last"
      (let [p  (pc/accept-resource p :oil 5)]
        (:oil (power-plant-resources p 3) 0) => 4
        (:oil (power-plant-resources p 4) 0) => 0
        (:oil (power-plant-resources p 5) 0) => 1))))

(fact pc/send-resource
  (let [plant-id 3
        p (-> (mock-player)
              (add-power-plant plant-id) ; oil
              (pc/accept-resource :oil 4)
              (pc/send-resource [plant-id :oil] 2))]
    (power-plant-resources p plant-id) => {:oil 2}))

(fact can-power-plant?
  (fact "ecological"
    (can-power-plant? ...player... 13) => truthy
    (provided
      (owns-power-plant? ...player... 13) => true))
  (future-fact "can power")
  (future-fact "can power, hybrid")
  (future-fact "can't power"))

(future-fact has-capacity?)
