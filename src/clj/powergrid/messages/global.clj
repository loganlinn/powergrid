(ns powergrid.messages.global
  (:require [powergrid.message :as msg]
            [powergrid.game :as g]
            [powergrid.domain.protocols :as pc]
            [powergrid.domain.player :as p]
            [powergrid.domain.power-plants :as pp]
            [powergrid.domain.resource :as r]
            [powergrid.util.error :refer [fail]]
            [powergrid.util :refer [kw]]))

(def ^:private player-color (comp p/color g/player))

(defrecord SetColorMessage [player-id color]
  msg/Message
  (turn? [_] false)
  (passable? [_ _] false)
  (validate [_ game]
    (let [color (kw color)]
      (cond
        (not (p/valid-color? color)) (fail "Invalid color")
        (= color (player-color player-id)) (fail "Already using that color")
        (g/color-taken? game color) (fail "Color taken")
        :else game)))
  (update-game [_ game logger]
    (g/update-player game player-id p/set-color color)))

(defrecord MoveResourcesMessage [player-id resource amt src-plant dst-plant]
  pc/Labeled
  (label [_ game]
    (format "%s moved %d %s from %s to %s."
            (pc/label (g/player player-id))
            amt
            (name resource)
            (pc/label (pp/plant src-plant))
            (pc/label (pp/plant dst-plant))))
  msg/Message
  (turn? [_] false)
  (passable? [_ _] false)
  (validate [_ game]
    )
  (update-game [_ game logger]
    ))

(def messages
  {:register nil
   :resign nil
   :ready nil
   :set-color map->SetColorMessage
   :set-region nil
   :move-resource nil})
