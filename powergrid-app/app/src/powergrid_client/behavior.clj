(ns ^:shared powergrid-client.behavior
    (:require [clojure.string :as string]
              [cljs.reader :refer [read-string]]
              [powergrid.common.game :as g]
              [powergrid.common.player :as p]
              [powergrid.common.cities]
              [powergrid.common.auction]
              [powergrid.common.resource]
              [powergrid.common.power-plants]
              [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]
              [io.pedestal.app.dataflow :as dataflow]
              [io.pedestal.app.util.log :as log]))

(def msg-pass :powergrid.message/pass)

;; While creating new behavior, write tests to confirm that it is
;; correct. For examples of various kinds of tests, see
;; test/powergrid_client/test/behavior.clj.

(defn inc-transform [old _]
  (inc (int old)))

(defn dec-transform [old _]
  (dec (int old)))

(defn cons-value-transform [old message]
  (cons (:value message) (or old [])))

(defn swap-value-transform [_ message]
  (:value message))

(defn swap-int-value-transform [_ message]
  (int (:value message)))

(defn conj-value-transform [old message]
  (conj (or old #{}) (:value message)))

(defn conj-keyword-value-transform [old message]
  (conj (or old #{}) (keyword (:value message))))

(defn disj-keyword-value-transform [old message]
  (disj old (keyword (:value message))))

(defn assoc-transform [data-key message-key]
  (fn [old message]
    (assoc old data-key (message-key message))))

(defn inc-resource [old message]
  (let [resource (keyword (:resource message))]
   (update-in old [:resources resource] (fnil inc 0))))

(defn dec-resource [old message]
  (let [resource (keyword (:resource message))]
    (update-in old [:resources resource] #(max 0 ((fnil dec 0))))))

(defn login-transform
  [old message]
  (assoc old
         :handle (:handle message)
         :color (keyword (:color message))))

(defn select-power-plant [old message]
  (let [v (int (:value message))]
    {:plant-id v
     :bid v}))

;; Derive

(defn derive-turn-topic [_ game]
  (when-let [phase (get-in game [:state :phase])]
   (keyword (str "phase" phase))))

(defn has-action
  [_ {:keys [game player-id] :as inputs}]
  (when player-id
    (= player-id
       (g/action-player-id game))))

;; Effects

;; TODO move committed? out of turn
(defn commit-turn [{:keys [turn player-id state]}]
  (if (:committed? turn)
    [{msg/type :update-game
      msg/topic [:game (:id state)]
      :turn (-> turn
                (dissoc :committed?)
                (assoc :player-id player-id))}]
    []))

(defn login-effect
  [{:keys [game-id handle color] :as login}]
  (log/debug :in :login-effect :login login)
  (if (and game-id handle color)
    [{msg/type :game-login
      msg/topic [:games game-id]
      :game-id game-id :handle handle :color color}]
    []))

;; Emitter

(defn init-login [_]
  [{:login
    {:transforms
     {:login [{msg/type :login msg/topic [:login] (msg/param :handle) {} (msg/param :color) {}}
              {msg/type :swap msg/topic [:login :game-id] :value "1"}
              ;{msg/type :set-focus msg/topic msg/app-model :name :game}
              ]}}}])

(defn init-main [_]
  [{:main
    {:game
     {:turn
      {:transforms
       {:commit [{msg/topic [:game :turn]} {msg/topic [:game :turn] msg/type :reset}]
        :pass-bid [{msg/topic [:game :turn] msg/type :pass}
                   {msg/topic [:game :turn :type] msg/type :swap :value :bid}
                   {msg/topic [:game :turn] msg/type :reset}]
        :pass-buy [{msg/topic [:game :turn] msg/type :pass}
                   {msg/topic [:game :turn :type] msg/type :swap :value :buy}
                   {msg/topic [:game :turn] msg/type :reset}]
        :reset [{msg/topic [:game :turn]}]
        :select-power-plant [{msg/topic [:game :turn] (msg/param :value) {}}
                             {msg/topic [:game :turn :topic] msg/type :swap :value :phase2}
                             {msg/topic [:game :turn :type] msg/type :swap :value :bid}]
        :inc-resource [{msg/topic [:game :turn] (msg/param :resource) {}}
                       {msg/topic [:game :turn :topic] msg/type :swap :value :phase3}
                       {msg/topic [:game :turn :type] msg/type :swap :value :buy}]
        :dec-resource [{msg/topic [:game :turn] (msg/param :resource) {}}
                       {msg/topic [:game :turn :topic] msg/type :swap :value :phase3}
                       {msg/topic [:game :turn :type] msg/type :swap :value :buy}]
        :select-city [{msg/topic [:game :turn :new-cities] (msg/param :value) {}}
                      {msg/topic [:game :turn :topic] msg/type :swap :value :phase4}
                      {msg/topic [:game :turn :type] msg/type :swap :value :buy}]
        :deselect-city [{msg/topic [:game :turn :new-cities] (msg/param :value) {}}]
        :power-cities [{msg/topic [:game :turn :powered-plants] (msg/param :plant-id) {} (msg/param :resource) {} (msg/param :amount) {}}
                       {msg/topic [:game :turn :topic] msg/type :swap :value :phase5}
                       {msg/topic [:game :turn :type] msg/type :swap :value :sell}]
        :bid [{msg/topic [:game :turn :bid] msg/type :swap-int (msg/param :value) {}}]
        :inc-bid [{msg/topic [:game :turn :bid] msg/type :inc}]
        :dec-bid [{msg/topic [:game :turn :bid] msg/type :dec}]
        :set [{msg/topic [:game :turn] (msg/param :value) {}}]
        }}}}}])

;;

(def example-app
  {:version 2
   :debug true
   :transform [[:swap [:**] swap-value-transform]
               [:conj [:**] conj-value-transform]
               [:cons [:**] cons-value-transform]
               [:inc  [:**] inc-transform]
               [:dec  [:**] dec-transform]
               [:swap-int [:**] swap-int-value-transform]
               [:login [:login] login-transform]
               [:select-power-plant [:game :turn] select-power-plant]
               [:inc-resource [:game :turn] inc-resource]
               [:dec-resource [:game :turn] dec-resource]
               [:select-city [:game :turn :new-cities] conj-keyword-value-transform]
               [:deselect-city [:game :turn :new-cities] disj-keyword-value-transform]
               [:commit [:game :turn] #(assoc %1 :committed? true)]
               [:pass [:game :turn] #(assoc %1 msg-pass true :committed? true)]
               [:reset [:game :turn] (constantly {})]

               [:set [:game :turn] #(read-string (:value %2))]
               [:debug [:pedestal :**] swap-value-transform]]
   :derive #{[{[:game :state] :game
               [:game :player-id] :player-id} [:game :has-action] has-action :map]}
   :effect #{[#{[:login]} login-effect :single-val]
             [#{[:game]} commit-turn :single-val]}
   :emit [{:init init-login}
          [#{[:login :*]} (app/default-emitter [])]
          {:init init-main}
          [#{[:game :*]} (app/default-emitter [:main])]
          [#{[:pedestal :debug :*]} (app/default-emitter [])]
          ]
   ;:focus {:login [[:login]]
   ;:wait [[:wait]]
   ;:game [[:main] [:pedestal]]
   ;:default :login}
   })
