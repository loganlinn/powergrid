(ns powergrid.power-plants)

(defrecord PowerPlant [number resource capacity yield])

(def power-plant-cards
  [(PowerPlant.  3 :oil 2 1)
   (PowerPlant.  4 :coal 2 1)
   (PowerPlant.  5 [:coal :oil] 2 1)
   (PowerPlant.  6 :garbage 1 1)
   (PowerPlant.  7 :oil 3 2)
   (PowerPlant.  8 :coal 3 2)
   (PowerPlant.  9 :oil 1 1)
   (PowerPlant. 10 :coal 2 2)
   (PowerPlant. 11 :uranium 1 2)
   (PowerPlant. 12 [:coal :oil] 2 2)
   (PowerPlant. 13 :ecological 0 1)
   (PowerPlant. 14 :garbage 2 2)
   (PowerPlant. 15 :coal 2 3)
   (PowerPlant. 16 :oil 2 3)
   (PowerPlant. 17 :uranium 1 2)
   (PowerPlant. 18 :ecological 0 2)
   (PowerPlant. 19 :garbage 2 3)
   (PowerPlant. 20 :coal 3 5)
   (PowerPlant. 21 [:coal :oil] 2 4)
   (PowerPlant. 22 :ecological 0 2)
   (PowerPlant. 23 :uranium 1 3)
   (PowerPlant. 24 :garbage 2 4)
   (PowerPlant. 25 :coal 2 5)
   (PowerPlant. 26 :oil 2 5)
   (PowerPlant. 27 :ecological 0 3)
   (PowerPlant. 28 :uranium 1 4)
   (PowerPlant. 29 [:coal :oil] 1 4)
   (PowerPlant. 30 :garbage 3 6)
   (PowerPlant. 31 :coal 3 6)
   (PowerPlant. 32 :oil 3 6)
   (PowerPlant. 33 :ecological 0 4)
   (PowerPlant. 34 :uranium 1 5)
   (PowerPlant. 35 :oil 1 5)
   (PowerPlant. 36 :coal 3 7)
   (PowerPlant. 37 :ecological 0 4)
   (PowerPlant. 38 :garbage 3 7)
   (PowerPlant. 39 :uranium 1 6)
   (PowerPlant. 40 :oil 2 6)
   (PowerPlant. 42 :coal 2 6)
   (PowerPlant. 44 :ecological 0 5)
   (PowerPlant. 46 [:coal :oil] 3 7)
   (PowerPlant. 50 :fusion 0 6)])
