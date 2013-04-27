(ns powergrid.player-test
  (:require [midje.sweet :refer :all]
            [powergrid.player :refer :all]
            [powergrid.power-plants :as pp]
            [powergrid.resource :refer [accept-resource send-resource]]))

(defn mock-player
  [& args]
  (let [p (new-player 1 "mock-ctx" :red)]
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
    (power-plants ...p...) => [(pp/plant 3) (pp/plant 25) (pp/plant 13)]))

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
      (add-power-plant ...pp...)
      (owns-power-plant? ...pp...)) => truthy)

(fact assign-resource
  (-> (mock-player)
      (add-power-plant ...pp...)
      (assign-resource ...pp... ...r... 4)
      (assign-resource ...pp... ...r... 1)
      (power-plant-resources ...pp...))
  => {...r... 5}
  (provided
    (pp/accepts-resource? ...pp... ...r...) => true))

(fact accept-resource
  (let [p (-> (mock-player)
              (add-power-plant (pp/plant 3)) ; oil
              (add-power-plant (pp/plant 4)) ; coal
              (add-power-plant (pp/plant 5)))] ; coal/oil hybrid
    (fact "empty initially"
      (:oil (power-plant-resources p (pp/plant 3)) 0) => 0
      (:oil (power-plant-resources p (pp/plant 4)) 0) => 0
      (:oil (power-plant-resources p (pp/plant 5)) 0) => 0)
    (fact "fills to capacity"
      (let [p  (accept-resource p :oil 4)]
        (:oil (power-plant-resources p (pp/plant 3)) 0) => 4
        (:oil (power-plant-resources p (pp/plant 4)) 0) => 0
        (:oil (power-plant-resources p (pp/plant 5)) 0) => 0))
    (fact "adds to hybrid last"
      (let [p  (accept-resource p :oil 5)]
        (:oil (power-plant-resources p (pp/plant 3)) 0) => 4
        (:oil (power-plant-resources p (pp/plant 4)) 0) => 0
        (:oil (power-plant-resources p (pp/plant 5)) 0) => 1))))

(fact send-resource
  (let [pp (pp/plant 3)
        p (-> (mock-player)
              (add-power-plant pp) ; oil
              (accept-resource :oil 4)
              (send-resource [pp :oil] 2))]
    (power-plant-resources p pp) => {:oil 2}))

(fact can-power-plant?
  (fact "ecological"
    (let [plant (pp/plant 13)]
     (can-power-plant? ...player... plant) => truthy
     (provided
       (owns-power-plant? ...player... plant) => true)))
  (future-fact "can power")
  (future-fact "can power, hybrid")
  (future-fact "can't power"))

(future-fact has-capacity?)
