(ns powergrid.resource)

(defprotocol ResourceTrader
  (accept-resource [trader dest amt]
                   "Returns trader after storing the resource in dest.
                   Methods assert that amt is valid")
  (send-resource [trader src amt]
                 "Returns trader after removing resources from src.
                 Methods assert that amt is valid"))
