(ns powergrid.util.log
  (:require [taoensso.timbre :as timbre]))

(def ^:macro trace #'timbre/trace)
(def ^:macro debug #'timbre/debug)
(def ^:macro info #'timbre/info)
(def ^:macro warn #'timbre/warn)
(def ^:macro error #'timbre/error)
(def ^:macro fatal #'timbre/fatal)
(def ^:macro report #'timbre/report)
(def ^:macro spy #'timbre/spy)
