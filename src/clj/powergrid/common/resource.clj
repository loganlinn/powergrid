(ns powergrid.common.resource
  (:require [powergrid.common.protocols :as pc]))

(def types #{:coal :oil :garbage :uranium})

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

(defn resource-price
  "Returns the price of purchasing amt of resource in current market.
  Asserts that amt is not larger than currently available in market."
  [{:keys [market pricing] :as resource} amt]
  {:pre [(>= market amt)]}
  (let [unavail (- (count pricing) market)]
    (apply + (take amt (drop unavail pricing)))))

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
