(ns powergrid.core-test
  (:use midje.sweet
        powergrid.core))

(fact player-order
  (player-order [...Anna... ...Angelika... ...Dale... ...Valerie...])
  => [...Anna... ...Dale... ...Angelika... ...Valerie...]
  (provided
    (network-size ...Anna...)     => 6
    (network-size ...Dale...)     => 5
    (network-size ...Angelika...) => 5
    (network-size ...Valerie....) => 4
    (max-power-plant ...Dale...)     => 17
    (max-power-plant ...Angelika...) => 15))

(fact replace-power-plant
  (let [a {:number 1}
        b {:number 2}
        c {:number 3}
        d {:number 4}
        e {:number 5}
        f {:number 6}
        x {:number 7}
        y {:number 8}
        z {:number 9}]
    (replace-power-plant {:actual [a b c]
                          :future [d e f]
                          :deck   [x y z]}
                         a)
    => {:actual [b c d]
        :future [e f x]
        :deck   [y z]}))

(fact accepts-resource?
 (accepts-resource? {:number 20, :resource :coal, :capacity 3, :yield 5} :coal) => true
 (accepts-resource? {:number 20, :resource :coal, :capacity 3, :yield 5} :oil) => false
 (accepts-resource? {:number 29, :resource [:coal :oil], :capacity 1, :yield 4} :coal) => true
 (accepts-resource? {:number 29, :resource [:coal :oil], :capacity 1, :yield 4} :oil) => true
 (accepts-resource? {:number 29, :resource [:coal :oil], :capacity 1, :yield 4} :garbage) => false)
