(ns powergrid.events)

;;;; Output Message Types
;; game-begin
;; game-end
;; update-resources
;; update-power-plants
;; update-money
;; update-players
;; status

(defmulti handle-message (fn [game msg] [(:topic msg) (:type msg)]))

;; GAME

(defmethod handle-message [:game :register]
  [game msg])

(defmethod handle-message [:game :resign]
  [game msg])

(defmethod handle-message [:game :ready]
  [game msg])

(defmethod handle-message [:game :set-color]
  [game msg])

(defmethod handle-message [:game :set-region]
  [game msg])

(defmethod handle-message [:game :move-resource]
  [game msg])

;; PHASE 2

(defmethod handle-message [:phase2 :buy]
  [game msg])

(defmethod handle-message [:phase2 :bid]
  [game msg])

(defmethod handle-message [:phase2 :pass]
  [game msg])

;; PHASE 3

(defmethod handle-message [:phase3 :buy]
  [game msg])

(defmethod handle-message [:phase3 :pass]
  [game msg])

;; PHASE 4

(defmethod handle-message [:phase4 :buy]
  [game msg])

(defmethod handle-message [:phase4 :trash]
  [game msg])

(defmethod handle-message [:phase4 :pass]
  [game msg])

(defmethod handle-message [:phase4 :end]
  [game msg])

;; PHASE 5
;;
(defmethod handle-message [:phase5 :sell]
  [game msg])
