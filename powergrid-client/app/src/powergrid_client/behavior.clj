(ns ^:shared powergrid-client.behavior
    (:require [clojure.string :as string]
              [cljs.reader :refer [read-string]]
              [powergrid.domain.game :as g]
              [powergrid.domain.player :as p]
              [powergrid.domain.cities]
              [powergrid.domain.auction]
              [powergrid.domain.resource]
              [powergrid.domain.power-plants]
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

(defn has-action
  [_ {:keys [game player-id] :as inputs}]
  (when player-id
    (= player-id
       (g/action-player-id game))))

;; Effects

;; TODO move committed? out of turn
(defn commit-turn [{:keys [turn player-id game-id]}]
  (log/debug :in :commit-turn :turn turn :player-id player-id :game-id game-id)
  (when-let [turn (first turn)]
   (when (and game-id player-id (every? turn [:committed? :topic :type]))
    [{msg/type :update-game
      msg/topic [:game game-id]
      :turn (-> turn
                (dissoc :committed?)
                (assoc :player-id player-id))}])))

(defn login-effect
  [{:keys [game-id handle color] :as login}]
  (log/debug :in :login-effect :login login)
  (when (and game-id handle color)
    [{msg/type :game-login
      msg/topic [:games game-id]
      :game-id game-id :handle handle :color color}]))

;; Continue

(defn phase-transition [phase]
  (log/debug :in :phase-transition :phase phase)
  (when phase
    [^:input {msg/topic msg/app-model msg/type :set-focus :name (keyword (str "phase" phase))}]))

;; Emitter

(defn init-login [_]
  [{:login
    {:transforms
     {:login [{msg/type :login msg/topic [:login] (msg/param :handle) {} (msg/param :color) {}}
              {msg/type :swap msg/topic [:login :game-id] :value "1"}
              {msg/type :set-focus msg/topic msg/app-model :name :game}]}}}])

(defn init-main [_]
  [{:main
    {:game
     {:state
      {:transforms
        {:refresh [{msg/topic [:game :state]}]}}}}}])

(defn init-phase2 [_]
  (log/debug :in :init-phase2 :args _)
  [{:main
    {:phase
     {2 {:turn
         {:transforms
          {:commit [{msg/topic [:phase 2 :turn]} {msg/topic [:phase 2 :turn] msg/type :reset}]
           :select-power-plant [{msg/topic [:phase 2 :turn] (msg/param :value) {}}
                                {msg/topic [:phase 2 :turn :topic] msg/type :swap :value :phase2}
                                {msg/topic [:phase 2 :turn :type] msg/type :swap :value :bid}]
           :bid [{msg/topic [:phase 2 :turn :bid] msg/type :swap-int (msg/param :value) {}}]
           :inc-bid [{msg/topic [:phase 2 :turn :bid] msg/type :inc}]
           :dec-bid [{msg/topic [:phase 2 :turn :bid] msg/type :dec}]
           :pass-bid [{msg/topic [:phase 2 :turn] msg/type :pass}
                      {msg/topic [:phase 2 :turn :type] msg/type :swap :value :bid}
                      {msg/topic [:phase 2 :turn :topic] msg/type :swap :value :phase2}
                      {msg/topic [:phase 2 :turn] msg/type :reset}]
           }}}}}}
   ;[:value [:main :phase 2 :turn :topic] :phase 2]
   ])

(defn init-phase3 [_]
  [{:main
    {:phase
     {3 {:turn
         {:transforms
          {:commit [{msg/topic [:phase 3 :turn]} {msg/topic [:phase 3 :turn] msg/type :reset}]
           :inc-resource [{msg/topic [:phase 3 :turn] (msg/param :resource) {}}
                          {msg/topic [:phase 3 :turn :type] msg/type :swap :value :buy}]
           :dec-resource [{msg/topic [:phase 3 :turn] (msg/param :resource) {}}
                          {msg/topic [:phase 3 :turn :type] msg/type :swap :value :buy}]
           :pass-buy [{msg/topic [:phase 3 :turn] msg/type :pass}
                      {msg/topic [:phase 3 :turn :type] msg/type :swap :value :buy}
                      {msg/topic [:phase 3 :turn :topic] msg/type :swap :value :phase3}
                      {msg/topic [:phase 3 :turn] msg/type :reset}]
           }}}}}}
   ;[:value [:main :phase 3 :turn :topic] :phase 3]
   ])

(defn init-phase4 [_]
  [{:main
    {:phase
    {4 {:turn
        {:transforms
         {:commit [{msg/topic [:phase 4 :turn]} {msg/topic [:phase 4 :turn] msg/type :reset}]
          :select-city [{msg/topic [:phase 4 :turn :new-cities] (msg/param :value) {}}
                        {msg/topic [:phase 4 :turn :type] msg/type :swap :value :buy}]
          :deselect-city [{msg/topic [:phase 4 :turn :new-cities] (msg/param :value) {}}]
          :pass-buy [{msg/topic [:phase 4 :turn] msg/type :pass}
                     {msg/topic [:phase 4 :turn :type] msg/type :swap :value :buy}
                     {msg/topic [:phase 4 :turn :topic] msg/type :swap :value :phase4}
                     {msg/topic [:phase 4 :turn] msg/type :reset}]
          }}}}}}
   ;[:value [:main :phase 4 :turn :topic] :phase 4]
   ])

(defn init-phase5 [_]
  [{:main
    {:phase
     {5 {:turn
         {:transforms
          {:commit [{msg/topic [:phase 5 :turn]} {msg/topic [:phase 5 :turn] msg/type :reset}]

           :power-cities [{msg/topic [:phase 5 :turn :powered-plants] (msg/param :plant-id) {} (msg/param :resource) {} (msg/param :amount) {}}
                          {msg/topic [:phase 5 :turn :topic] msg/type :swap :value :phase5}
                          {msg/topic [:phase 5 :turn :type] msg/type :swap :value :sell}]
           :pass-sell [{msg/topic [:phase 5 :turn] msg/type :pass}
                       {msg/topic [:phase 5 :turn :type] msg/type :swap :value :sell}
                       {msg/topic [:phase 5 :turn :topic] msg/type :swap :value :phase5}
                       {msg/topic [:phase 5 :turn] msg/type :reset}]}}}}}}
   ;[:value [:main :phase 5 :turn :topic] :phase 5]
   ])

;;

(def example-app
  {:version 2
   :debug true
   :transform [[:swap [:**] swap-value-transform]
               [:conj [:**] conj-value-transform]
               [:cons [:**] cons-value-transform]
               [:inc  [:**] inc-transform]
               [:dec  [:**] dec-transform]
               [:swap-int [:**] (comp int int-value-transform)]
               [:login [:login] login-transform]
               [:select-power-plant [:phase 2 :turn] select-power-plant]
               [:inc-resource [:phase 3 :turn] inc-resource]
               [:dec-resource [:phase 3 :turn] dec-resource]
               [:select-city   [:phase 4 :turn :new-cities] conj-keyword-value-transform]
               [:deselect-city [:phase 4 :turn :new-cities] disj-keyword-value-transform]

               [:commit [:phase :* :turn] #(assoc %1 :committed? true)]
               [:pass   [:phase :* :turn] #(assoc %1 msg-pass true :committed? true)]
               [:reset  [:phase :* :turn] (constantly {})]

               [:set [:game :turn] #(read-string (:value %2))]
               [:debug [:pedestal :**] swap-value-transform]]

   :derive #{[{[:game :state] :game
               [:game :player-id] :player-id} [:game :has-action] has-action :map]}

   :effect #{[#{[:login]} login-effect :single-val]
             [{[:game :player-id] :player-id
               [:game :state :id] :game-id
               [:phase :* :turn] :turn} commit-turn :map]}

   :continue #{[#{[:game :state :phase]} phase-transition :single-val]}

   :emit [{:init init-login}
          [#{[:login :*]} (app/default-emitter [])]
          {:init init-main}
          [#{[:game :*]} (app/default-emitter [:main])]
          {:in #{[:phase 2 :*]} :init init-phase2 :fn (app/default-emitter [:main])}
          {:in #{[:phase 3 :*]} :init init-phase3 :fn (app/default-emitter [:main])}
          {:in #{[:phase 4 :*]} :init init-phase4 :fn (app/default-emitter [:main])}
          {:in #{[:phase 5 :*]} :init init-phase5 :fn (app/default-emitter [:main])}
          [#{[:pedestal :debug :*]} (app/default-emitter [])]]

   :focus {:login [[:login]]
           :wait [[:wait]]
           :game [[:main] [:pedestal]]
           :phase2 [[:main :game] [:main :phase 2] [:pedestal]]
           :phase3 [[:main :game] [:main :phase 3] [:pedestal]]
           :phase4 [[:main :game] [:main :phase 4] [:pedestal]]
           :phase5 [[:main :game] [:main :phase 5] [:pedestal]]
           :default :game}
   })

