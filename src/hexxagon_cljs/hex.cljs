(ns hexxagon-cljs.hex)

;; https://www.redblobgames.com/grids/hexagons/


(def offset-neighbours
  [[-1 -1] [0 -1] [1 -1]
   [-1  0] [0  1] [1  0]])


(def non-offset-neighbours
  [[-1 0] [0 -1] [1 0]
   [-1 1] [0  1] [1 1]])


(defn even-q->cube [cell]
  (let [col (get cell :x)
        row (get cell :y)
        x col
        z (- row (/ (+ col (bit-and col 1)) 2))
        y (- (- x) z)]
    {:x x
     :z z
     :y y}))


(defn distance [src-cell dst-cell]
  (let [{src-x :x
         src-y :y
         src-z :z} (even-q->cube src-cell)
        {dst-x :x
         dst-y :y
         dst-z :z} (even-q->cube dst-cell)]
    (/ (+ (Math/abs (- src-x dst-x))
          (Math/abs (- src-y dst-y))
          (Math/abs (- src-z dst-z)))
       2)))


(defn offset-cell? [cell]
  (odd? (get cell :x)))


(defn get-neighbours [cell board]
  (let [x (get cell :x)
        y (get cell :y)
        offsets (if (offset-cell? cell)
                  offset-neighbours
                  non-offset-neighbours)]
    (->> offsets
         (mapv (fn [[offset-x offset-y]] (get-in board [(+ y offset-y) (+ x offset-x)])))
         (filterv some?)
         (reduce conj []))))


(defn get-cells-for-jump [cell board]
  (let [closest-neighbours (get-neighbours cell board)
        neighbours-of-neighbours (mapv #(get-neighbours %1 board) closest-neighbours)
        all-cells (conj neighbours-of-neighbours closest-neighbours)]
    (->> all-cells
         flatten
         distinct)))


(defn player-has-movements? [owner board]
  (let [moves (->> board
                   flatten
                   (filter #(= (get %1 :owner) owner))
                   (filter #(pos? (count (get-cells-for-jump %1 board)))))
        first-move (first (take 1 moves))]
    (some? first-move)))


(defn get-cells-count [player board]
  (->> board
       flatten
       (filter (fn [cell] (= (get cell :owner) player)))
       count))


(defn get-winner [board]
  (let [red-cells-count (get-cells-count :player/red board)
        blue-cells-count (get-cells-count :player/blue board)]
    (if (> red-cells-count blue-cells-count)
      :player/red
      :player/blue)))
