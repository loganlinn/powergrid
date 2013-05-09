(ns powergrid.messages.global
  (:require [powergrid.message :refer [Message]]
            [powergrid.game :as g]
            [powergrid.common.player :as p]
            [powergrid.resource :as r]
            [powergrid.util :refer [kw]]))

(def ^:private player-color (comp p/color g/player))

(defrecord SetColorMessage [player-id color]
  Message
  (turn? [_] false)
  (passable? [_ _] false)
  (update-pass [_ game] game)

  (validate [this game]
    (let [color (kw color)]
      (cond
        (not (p/valid-color? color)) "Invalid color"
        (= color (player-color player-id)) "Already using that color"
        (g/color-taken? game color) "Color taken")))

  (update-game [this game]
    (g/update-player game player-id p/set-color color)))


(def messages
  {:register nil
   :resign nil
   :ready nil
   :set-color map->SetColorMessage
   :set-region nil
   :move-resource nil})
