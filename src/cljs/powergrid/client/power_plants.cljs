(ns powergrid-client.power-plants)

(defrecord PowerPlant [number resource capacity yield])

(defn id [power-plant] (:number power-plant))
(defn capacity [power-plant] (:capacity power-plant))
(defn max-capacity [power-plant] (* 2 (capacity power-plant)))
(defn yield [power-plant] (:yield power-plant))
(defn plant-number [power-plant] (:number power-plant))

(def power-plants
  {3 (PowerPlant.  3 :oil 2 1)
   4 (PowerPlant.  4 :coal 2 1)
   5 (PowerPlant.  5 #{:coal :oil} 2 1)
   6 (PowerPlant.  6 :garbage 1 1)
   7 (PowerPlant.  7 :oil 3 2)
   8 (PowerPlant.  8 :coal 3 2)
   9 (PowerPlant.  9 :oil 1 1)
   10 (PowerPlant. 10 :coal 2 2)
   11 (PowerPlant. 11 :uranium 1 2)
   12 (PowerPlant. 12 #{:coal :oil} 2 2)
   13 (PowerPlant. 13 :ecological 0 1)
   14 (PowerPlant. 14 :garbage 2 2)
   15 (PowerPlant. 15 :coal 2 3)
   16 (PowerPlant. 16 :oil 2 3)
   17 (PowerPlant. 17 :uranium 1 2)
   18 (PowerPlant. 18 :ecological 0 2)
   19 (PowerPlant. 19 :garbage 2 3)
   20 (PowerPlant. 20 :coal 3 5)
   21 (PowerPlant. 21 #{:coal :oil} 2 4)
   22 (PowerPlant. 22 :ecological 0 2)
   23 (PowerPlant. 23 :uranium 1 3)
   24 (PowerPlant. 24 :garbage 2 4)
   25 (PowerPlant. 25 :coal 2 5)
   26 (PowerPlant. 26 :oil 2 5)
   27 (PowerPlant. 27 :ecological 0 3)
   28 (PowerPlant. 28 :uranium 1 4)
   29 (PowerPlant. 29 #{:coal :oil} 1 4)
   30 (PowerPlant. 30 :garbage 3 6)
   31 (PowerPlant. 31 :coal 3 6)
   32 (PowerPlant. 32 :oil 3 6)
   33 (PowerPlant. 33 :ecological 0 4)
   34 (PowerPlant. 34 :uranium 1 5)
   35 (PowerPlant. 35 :oil 1 5)
   36 (PowerPlant. 36 :coal 3 7)
   37 (PowerPlant. 37 :ecological 0 4)
   38 (PowerPlant. 38 :garbage 3 7)
   39 (PowerPlant. 39 :uranium 1 6)
   40 (PowerPlant. 40 :oil 2 6)
   42 (PowerPlant. 42 :coal 2 6)
   44 (PowerPlant. 44 :ecological 0 5)
   46 (PowerPlant. 46 #{:coal :oil} 3 7)
   50 (PowerPlant. 50 :fusion 0 6) })

(defn initial-market [] (map power-plants [3 4 5 6]))
(defn initial-future [] (map power-plants [7 8 9 10]))
(defn initial-deck   [] (keep #(when (> (key %) 10) (val %)) power-plants))

(defn plant [plant-num]
  (power-plants plant-num))

(defn min-price [plant] (:number plant))

(defn is-hybrid?
  "Returns true if power-plant is hybrid, otherwise false"
  [power-plant]
  (set? (:resource power-plant)))

(defn consumes-resources?
  "Returns true if plant requires resources to operate, otherwise false."
  [{resource :resource}]
  (not (or (= :ecological resource)
           (= :fusion resource))))

(defn accepts-resource?
  "Returns true if the power-plant accepts the resource, otherwise false"
  [{power-plant-resource :resource :as power-plant} resource]
  (if (is-hybrid? power-plant)
    (contains? power-plant-resource resource)
    (condp = power-plant-resource
      :ecological false
      :fusion     false
      resource    true
      false)))
