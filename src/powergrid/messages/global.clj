(ns powergrid.messages.global
  (:require [powergrid.message :refer [Message GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]
            [powergrid.util :refer [kw]]))

(defrecord SetColorMessage [player-id color]
  Message
  (validate [this game]
    (let [color (kw color)]
      (and (p/valid-color? color)
           (not (= color (p/color (g/player player-id))))
           (not (g/color-taken? game color)))))
  GameUpdate
  (update-game [this game]
    (g/update-player game player-id p/set-color color)))


(def messages
  {:register nil
   :resign nil
   :ready nil
   :set-color map->SetColorMessage
   :set-region nil
   :move-resource nil})
