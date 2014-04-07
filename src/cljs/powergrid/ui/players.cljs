(ns powergrid.ui.players
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [powergrid.domain.player :as player]))

(defn turn-order-lookup
  "Returns a mapping from player id to turn order. Can be used as a
   single-argument function to look-up player's turn order"
  [turn-order]
  (zipmap turn-order (iterate inc 0)))

(defn sort-players-by-turn-order
  "Returns players after sorting by turn-order"
  [players turn-order]
  (sort-by (comp (turn-order-lookup turn-order) :color) (vals players)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Components

(defn player-view [player owner]
  (reify
    om/IRender
    (render [_]
      (dom/li #js {:className (cond-> "player"
                                      (:has-action? player) (str " has-action"))
                   :data-player-id (player/id player)}
              (dom/span #js {:className "handle"} (:handle player))
              (dom/span #js {:className "money"} (str "$" (:money player 0)))))))

(defn players-view [data owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [action-player-id]} data
            ps (-> (:players data)
                   (sort-players-by-turn-order (:turn-order data)))]
        (apply dom/ul nil
               (om/build-all player-view ps
                             {:key :color
                              :fn #(assoc % :has-action? (= (player/id %) action-player-id))}))))))
