(ns powergrid.domain.country.usa)

(def cities
  #{:atlanta
    :billings
    :birmingham
    :boise
    :boston
    :buffalo
    :cincinnati
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
    :savannah
    :seattle
    :st-louis
    :tampa
    :washington})

(def connections
  {[:atlanta :knoxville] 5
   [:atlanta :birmingham] 3
   [:atlanta :raleigh] 7
   [:atlanta :savannah] 7
   [:atlanta :st-louis] 12
   [:billings :seattle] 9
   [:billings :fargo] 17
   [:billings :minneapolis] 18
   [:billings :boise] 12
   [:billings :cheyenne] 9
   [:birmingham :memphis] 7
   [:birmingham :jacksonville] 9
   [:birmingham :new-orleans] 11
   [:boise :portland] 13
   [:boise :seattle] 12
   [:boise :cheyenne] 24
   [:boise :salt-lake-city] 8
   [:boise :san-francisco] 23
   [:boston :new-york] 3
   [:buffalo :new-york] 8
   [:buffalo :pittsburgh] 7
   [:buffalo :detroit] 7
   [:cincinnati :chicago] 7
   [:cincinnati :detroit] 4
   [:cincinnati :pittsburgh] 7
   [:cincinnati :raleigh] 15
   [:cincinnati :knoxville] 6
   [:cincinnati :st-louis] 12
   [:cheyenne :minneapolis] 18
   [:cheyenne :omaha] 14
   [:cheyenne :denver] 0
   [:chicago :minneapolis] 8
   [:chicago :duluth] 12
   [:chicago :detroit] 7
   [:chicago :st-louis] 10
   [:chicago :kansas-city] 8
   [:chicago :omaha] 13
   [:detroit :pittsburgh] 6
   [:dallas :santa-fe] 16
   [:dallas :oklahoma-city] 3
   [:dallas :memphis] 12
   [:dallas :new-orleans] 12
   [:dallas :houston] 5
   [:denver :salt-lake-city] 21
   [:denver :santa-fe] 13
   [:duluth :fargo] 6
   [:duluth :minneapolis] 5
   [:duluth :detroit] 15
   [:fargo :minneapolis] 6
   [:houston :santa-fe] 21
   [:houston :new-orleans] 8
   [:jacksonville :new-orleans] 16
   [:jacksonville :birmingham] 9
   [:jacksonville :savannah] 0
   [:jacksonville :tampa] 4
   [:kansas-city :denver] 16
   [:kansas-city :omaha] 5
   [:kansas-city :st-louis] 6
   [:kansas-city :memphis] 12
   [:kansas-city :oklahoma-city] 8
   [:kansas-city :santa-fe] 16
   [:las-vegas :san-diego] 9
   [:las-vegas :los-angeles] 9
   [:las-vegas :san-francisco] 14
   [:las-vegas :salt-lake-city] 18
   [:las-vegas :santa-fe] 27
   [:las-vegas :phoenix] 15
   [:los-angeles :san-francisco] 9
   [:los-angeles :san-diego] 3
   [:miami :tampa] 4
   [:minneapolis :omaha] 8
   [:memphis :new-orleans] 7
   [:memphis :oklahoma-city] 14
   [:memphis :st-louis] 7
   [:new-york :philadelphia] 0
   [:norfolk :washington] 5
   [:norfolk :raleigh] 3
   [:oklahoma-city :santa-fe] 15
   [:philadelphia :washington] 3
   [:phoenix :san-diego] 14
   [:phoenix :santa-fe] 18
   [:pittsburgh :washington] 6
   [:pittsburgh :raleigh] 7
   [:portland :seattle] 3
   [:portland :san-francisco] 24
   [:raleigh :savannah] 7
   [:salt-lake-city :san-francisco] 27
   [:salt-lake-city :santa-fe] 28})
