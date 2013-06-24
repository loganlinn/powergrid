(ns powergrid.common.protocols
  "Protocols used globally the application")

;; TODO Move other protocols here

(defprotocol Activity
  (start [this])
  (stop [this]))

(defprotocol Labeled
  "Provides a user-facing label or description"
  (label [this] [this game]))

(extend-protocol Labeled
  nil
  (label [_] "")
  clojure.lang.Keyword
  (label [this] (name this)))

(defprotocol ResourceTrader
  (accept-resource [trader dest amt]
                   "Returns trader after storing the resource in dest.
                   Methods assert that amt is valid")
  (send-resource [trader src amt]
                 "Returns trader after removing resources from src.
                 Methods assert that amt is valid"))

