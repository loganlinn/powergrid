(ns powergrid-client.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro powergrid-client-templates
  []
  ;; The last argument to 'dtfn' is a set of fields that should be
  ;; treated as static fields (may only be set once). Dynamic templates
  ;; use ids to set values so you cannot dynamically set an id.
  {:powergrid-game (dtfn (tnodes "powergrid-client.html"
                                 "powergrid-game"
                                 [[:.power-plant-market]]))
   :power-plant (tfn (tnodes "powergrid-client.html" "power-plant"))})

;; Note: this file will not be reloaded automatically when it is changed.
