(ns sprite-style.math)

(def pi js/Math.PI)

(defn degrees [r] (* r 57.2957795))

(defn round [n]
  (.round js/Math n))

(defn sqrt [n]
  (.sqrt js/Math n))

(defn acos [n]
  (.acos js/Math n))

(defn atan2 [m n]
  (.atan2 js/Math m n))

(defn cos [theta]
  (.cos js/Math theta))

(defn sin [theta]
  (.sin js/Math theta))

(defn snap-unit-circle [a n]
  (let [incr (/ (* 2 pi) n)]
    (-> (map #(* % incr) (range (inc n)))
        (nth (int (/ a incr))))))
