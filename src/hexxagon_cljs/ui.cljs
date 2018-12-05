(ns hexxagon-cljs.ui
  (:require [rum.core :as rum]
            [clojure.string :as string]
            [hexxagon-cljs.hex :as hex]))

(defn classnames [classname->value]
  (->> classname->value
       (filterv (fn [[key value]] value))
       (mapv (fn [[key value]] key))
       (string/join " ")))


(rum/defc ui-board [children]
  [:div.board children])


(rum/defc ui-row
  < {:key-fn (fn [key children] key)}
  [key children]
  [:div.row children])


(rum/defc ui-cell
  < rum/static
  {:key-fn (fn [cell] (str (get cell :x) "-" (get cell :y)))}
  [cell on-click]
  [:div.cell
   {:class (classnames {"cell--red" (= :player/red (get cell :owner))
                        "cell--blue" (= :player/blue (get cell :owner))
                        "cell--selected" (get cell :selected?)
                        "cell--highlighted" (get cell :highlighted?)
                        "cell--hidden" (nil? (get cell :owner))
                        "cell--empty" (get cell :empty?)})
    :on-click #(on-click cell)}
  ;  (str "y:" (get cell :y) " x:" (get cell :x))
   ])


(rum/defc ui-field
  < rum/static
  [{on-cell-click :on-cell-click
    board :board}]
  [:div.field
   (map-indexed
    (fn [row-index row]
      (ui-row
       row-index
       (mapv
        (fn [cell] (ui-cell cell on-cell-click))
        row)))
    board)])


(rum/defc ui-status
  < rum/static
  [state]
  (let [{board :board
         turn :turn
         winner :winner} state]
    [:code
     (when (nil? winner)
       [:div "turn " (str turn)])
     [:div "red " (hex/get-cells-count :player/red board)]
     [:div "blue " (hex/get-cells-count :player/blue board)]
     [:div "winner " (str (or winner "none"))]]))


(rum/defc ui-game
  < rum/reactive
  rum/static
  [handlers state]
  (let [ui-state (rum/react state)
        on-cell-click (get handlers :on-cell-click)]
    [:div
     [:div (ui-field {:board (:board ui-state)
                      :on-cell-click on-cell-click})]
     [:div (ui-status ui-state)]]))


(defn mount! [handlers state-atom node]
  (rum/mount (ui-game handlers state-atom) node))