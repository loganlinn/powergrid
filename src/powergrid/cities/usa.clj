(ns powergrid.cities.usa)

(def cities
  [:atlanta
   :billings
   :birmingham
   :boise
   :boston
   :buffalo
   :cincinnatti
   :cheyenne
   :chicago
   :dallas
   :denver
   :detroit
   :duluth
   :fargo
   :houston
   :jacksonville
   :kansas-city
   :knoxville
   :las-vegas
   :los-angeles
   :miami
   :minneapolis
   :memphis
   :new-orleans
   :new-york
   :norfolk
   :oklahoma-city
   :omaha
   :philadelphia
   :phoenix
   :pittsburgh
   :portland
   :raleigh
   :salt-lake-city
   :san-diego
   :san-francisco
   :santa-fe
   :savanna
   :seattle
   :st-louis
   :tampa
   :washington])

(def connection-costs
  {[:boston :new-york] 3
   [:buffalo :new-york] 8
   [:buffalo :pittsburgh] 7
   [:buffalo :detroit] 7
   [:new-york :philadelphia] 0
   [:philadelphia :washington] 3
   [:pittsburgh :washington] 6
   [:norfolk :washington] 5})

;; TODO
;; TODO
;; TODO
