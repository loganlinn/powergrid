(ns powergrid.util.error
  (:require [clojure.algo.monads :refer [defmonad domonad]]))

(defrecord Failure [message])

(defn fail [message] (->Failure message))

(defprotocol StateChangeFailed
  "Determines if a state change has resulted in failure."
  (has-failed? [this]))

(extend-protocol StateChangeFailed
  Object
  (has-failed? [_] false)

  Failure
  (has-failed? [_] true)

  Exception
  (has-failed? [_] true))

(defmonad error-m
  [m-result identity
   m-bind (fn [m f]
            (if (has-failed? m)
              m
              (f m)))])

(defmacro attempt-all
  ([bindings return] `(domonad error-m ~bindings ~return))
  ([bindings return else]
     `(let [result# (attempt-all ~bindings ~return)]
        (if (has-failed? result#)
            ~else
            result#))))
