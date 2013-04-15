(ns powergrid.messages.global
  (:require [powergrid.message :refer [Validated ->ValidateError GameUpdate]]
            [powergrid.game :as g]
            [powergrid.player :as p]
            [powergrid.resource :as r]
            [powergrid.util :refer [kw]]
            [slingshot.slingshot :refer [throw+]]))

(def ^:private player-color (comp p/color g/player))

(defrecord SetColorMessage [player-id color]
  Validated
  (validate [this game]
    (let [color (kw color)]
      (cond
        (not (p/valid-color? color)) (throw+ (->ValidateError "Invalid color"))
        (= color (player-color player-id)) (throw+ (->ValidateError "Already using that color" true))
        (g/color-taken? game color) (throw+ (->ValidateError "Color taken")))))
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
