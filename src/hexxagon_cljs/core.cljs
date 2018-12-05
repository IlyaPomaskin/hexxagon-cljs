; https://codepen.io/alexzaworski/pen/qDokc?editors=1100
; TODO передавать state во все функции первым параметром для использования threading macro

(ns hexxagon-cljs.core
  (:require [hexxagon-cljs.ui :as ui]
            [hexxagon-cljs.state :as state]))


(enable-console-print!)


(defonce game-state
  (atom (state/get-initial-state)))


(defonce game-state-logger
  (add-watch game-state :logger #(js/console.log "next-state" %4)))


(ui/mount!
 {:on-cell-click #(state/on-click %1 game-state)}
 game-state
 (js/document.querySelector "#app"))