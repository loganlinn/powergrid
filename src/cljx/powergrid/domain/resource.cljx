(ns powergrid.domain.resource
  (:require [powergrid.domain.protocols :as pc]))

(def types #{:coal :oil :garbage :uranium})

(def market-capacity
  (let [standard {1 3, 2 3, 3 3, 4 3, 5 3, 6 3, 7 3, 8 3}]
    {:coal standard
     :oil  standard
     :garbage standard
     :uranium {1 1, 2 1, 3 1, 4 1, 5 1, 6 1, 7 1, 8 1, 12 1, 14 1, 15 1, 16 1}}))

(def resupply-table
  {2 {:coal    {1 3, 2 4, 3 3}
      :oil     {1 2, 2 2, 3 4}
      :garbage {1 1, 2 2, 3 3}
      :uranium {1 1, 2 1, 3 1}}
   3 {:coal    {1 4, 2 5, 3 3}
      :oil     {1 2, 2 3, 3 4}
      :garbage {1 1, 2 2, 3 3}
      :uranium {1 1, 2 1, 3 1}}
   4 {:coal    {1 5, 2 6, 3 4}
      :oil     {1 3, 2 4, 3 5}
      :garbage {1 2, 2 3, 3 4}
      :uranium {1 1, 2 2, 3 2}}
   5 {:coal    {1 5, 2 7, 3 5}
      :oil     {1 4, 2 5, 3 6}
      :garbage {1 3, 2 3, 3 5}
      :uranium {1 2, 2 3, 3 2}}
   6 {:coal    {1 7, 2 9, 3 6}
      :oil     {1 5, 2 6, 3 7}
      :garbage {1 3, 2 5, 3 6}
      :uranium {1 2, 2 3, 3 3}}})

(defrecord Resource [label market supply pricing]
  pc/Labeled
  (label [_] label)
  pc/ResourceTrader
  (accept-resource [resource dest amt]
    (update-in resource [dest] (fnil + 0) amt))
  (send-resource [resource src amt]
    (update-in resource [src] (fnil - 0) amt)))

(defn market-pricing
  [{:keys [market pricing]}]
  (drop (- (count pricing) market) pricing))

(defn resource-price
  "Returns the price of purchasing amt of resource in current market.
  Asserts that amt is not larger than currently available in market."
  [resource amt]
  {:pre [(>= (:market resource) amt)]}
  (->> (market-pricing resource)
       (take amt)
       (apply +)))

(defn resupply-rate
  "Returns a map of resource to amount to re-supply the resource market with,
  optionally taking into account the current resource supply"
  ([num-players step]
     (reduce (fn [m [resource rates]]
               (assoc m resource (get rates step)))
             {} (get resupply-table num-players)))
  ([num-players step supply]
     (reduce (fn [m [resource rates]]
               (assoc m resource (min (get rates step) (get supply resource))))
             {} (get resupply-table num-players))))

(defn- resource-pricing [resource]
  (->> (get market-capacity resource)
       (mapcat (fn [[price n]] (repeat n price)))))

(defn initial-resources []
  {:coal (map->Resource {:label "coal" :market 24 :supply 0 :pricing (resource-pricing :coal)})
   :oil  (map->Resource {:label "oil" :market 18 :supply 6 :pricing (resource-pricing :oil)})
   :garbage (map->Resource {:label "garbage" :market 6 :supply 18 :pricing (resource-pricing :garbage)})
   :uranium (map->Resource {:label "uranium" :market 2 :supply 10 :pricing (resource-pricing :uranium)})})
