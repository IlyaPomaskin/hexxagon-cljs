(ns hexxagon-cljs.state
    (:require [hexxagon-cljs.hex :as hex]
              [hexxagon-cljs.boards :as boards]))


(defn get-initial-state []
  {:board boards/b0
   :turn :player/red
   :winner nil})


(defn available-cell? [cell]
  (let [owner (get cell :owner)
        empty-cell? (= owner :empty)
        visible-cell? (not (nil? owner))]
    empty-cell?))


(defn find-selected-cell [board]
  (->> board
       flatten
       (filter (fn [cell] (:selected? cell)))
       first))


(defn set-board-cell-attr [attr value cells board]
  (reduce
   (fn [board cell] (assoc-in board [(get cell :y) (get cell :x) attr] value))
   board
   cells))


(defn set-highlighted [cell highlighted? board]
  (let [cells-for-jump (hex/get-cells-for-jump cell board)
        available-cells (filterv #(and (not= cell %1) (available-cell? %1)) cells-for-jump)]
    (set-board-cell-attr :highlighted? highlighted? available-cells board)))


(defn set-cell-owner [owner cells board]
  (set-board-cell-attr :owner owner cells board))


(defn select-cell [cell state]
  (let [can-select? (and (= (get cell :owner) (get state :turn))
                         (not (nil? (get cell :owner))))]
    (if can-select?
      (-> state
          (assoc-in [:board (get cell :y) (get cell :x) :selected?] true)
          (update-in [:board] #(set-highlighted cell true %)))
      state)))


(defn deselect-cell [cell state]
  (if (true? (get cell :selected?))
    (-> state
        (assoc-in [:board (get cell :y) (get cell :x) :selected?] false)
        (update-in [:board] #(set-highlighted cell false %)))
    state))


(defn can-jump? [src-cell dst-cell state]
  (let [distance (hex/distance src-cell dst-cell)]
    (and (= (get src-cell :owner) (get state :turn))
         (= :empty (get dst-cell :owner))
         (not (nil? (get dst-cell :owner)))
         (true? (get dst-cell :highlighted?))
         (or (= distance 1)
             (= distance 2)))))


(defn get-winner [state]
  (let [opponent (if (= (get state :turn) :player/red) :player/blue :player/red)
        board (get state :board)
        opponent-can-move? (hex/player-has-movements? opponent board)
        winner (when (not opponent-can-move?) (hex/get-winner board))]
    (assoc-in state [:winner] winner)))


(defn jump [src-cell dst-cell state]
  (if (can-jump? src-cell dst-cell state)
    (let [distance (hex/distance src-cell dst-cell)
          current-player (get state :turn)
          next-src-cell {:owner (if (= distance 1) current-player :empty)
                         :selected? false}
          next-dst-cell {:owner current-player}
          opponent (if (= :player/blue current-player) :player/red :player/blue)
          opponent-cells (->> (get state :board)
                              (hex/get-neighbours dst-cell)
                              (filterv #(= (get % :owner) opponent)))]
      (-> state
          (update-in [:board] #(set-highlighted src-cell false %1))
          (update-in [:board] #(set-cell-owner current-player opponent-cells %1))
          (update-in [:board (get src-cell :y) (get src-cell :x)] #(merge %1 next-src-cell))
          (update-in [:board (get dst-cell :y) (get dst-cell :x)] #(merge %1 next-dst-cell))
          get-winner
          (assoc :turn opponent)))
    state))


(defn handle-cell-click [cell state]
  (let [selected-cell (find-selected-cell (get state :board))
        same-owner-cell? (= (get cell :owner) (get selected-cell :owner))
        same-cell? (= cell selected-cell)]
    (cond
      same-cell? (deselect-cell selected-cell state)
      same-owner-cell? (->> state
                            (deselect-cell selected-cell)
                            (select-cell cell))
      (nil? selected-cell) (select-cell cell state)
      (some? selected-cell) (jump selected-cell cell state)
      :else state)))


(defn on-click [cell state]
  (swap!
   state
   (fn [current-state]
     (if (nil? (get current-state :winner))
       (handle-cell-click cell current-state)
       current-state))))

