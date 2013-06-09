(ns powergrid.macros)

(defmacro defcomponent
  [name bindings & sigs]
  `(do
     (defprotocol ~name ~bindings ~@sigs)
     ))

(comment
  (macroexpand `(defcomponent PlayerBar [node]
                  component/PComponent
                  (event-subscriptions [_]
                                       {:anywhere {:set-turn-order set-turn-order
                                                   :action-to-player action-to-player}})
                  (render [this]
                          ))))
