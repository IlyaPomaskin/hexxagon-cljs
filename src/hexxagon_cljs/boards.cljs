(ns hexxagon-cljs.boards
    (:require [clojure.string :as string]))


(def char->player
  {"B" :player/blue
   "R" :player/red
   "0" :empty})


(defn create-cell [char x y]
  {:x x
   :y y
   :selected? false
   :highlighted? false
   :owner (char->player char)})


(defn string->board [board]
  (letfn [(mapv-indexed
            [func vect]
            (reduce-kv (fn [acc key value] (conj acc (func key value))) [] vect))]
    (mapv-indexed
     (fn [y line]
       (mapv-indexed
        (fn [x char] (create-cell char x y))
        (filterv #(not= "" %) (string/split line #""))))
     board)))


(def b0
  (string->board
   ["____B____"
    "__00000__"
    "R0000000R"
    "0000_0000"
    "000000000"
    "000_0_000"
    "B0000000B"
    "_0000000_"
    "___0R0___"]))


(def b1
  (string->board
   ["0_0_0_0_0"
    "000000000"
    "000_0_000"
    "0B00000R0"
    "000_0_000"
    "000000000"]))


(def b2
  (string->board
   ["B_R_B_R_B"
    "0_0_0_0_0"
    "_000_000_"
    "_0000000_"
    "_000_000_"
    "000000000"
    "R_B_R_B_R"]))


(def b3
  (string->board
   ["__0_0_0__"
    "__B___R__"
    "_0_000_0_"
    "0_00000_0"
    "___000___"
    "_0R0_0B0_"
    "__0_0_0__"]))


(def b4
  (string->board
   ["____B____"
    "__00000__"
    "R0000000R"
    "0000_0000"
    "000___000"
    "000___000"
    "B0000000B"
    "_0000000_"
    "___0R0___"]))


(def b5
  (string->board
   ["____R____"
    "0_00000_0"
    "B0000000B"
    "_0_0_0_0_"
    "_________"
    "_________"
    "R0000000R"
    "000000000"
    "___0B0___"]))


(def all [b0 b1 b2 b3 b4 b5])