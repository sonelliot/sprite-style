(ns sprite-style.vector
  (:require
   [sprite-style.math :as math]))

(defn v!
  ([] [0 0])
  ([x] [x 0])
  ([x y] [x y]))

(defn vx [[x _]] x)
(defn vy [[_ y]] y)

(defn v+ [[ax ay] [bx by]] [(+ ax bx) (+ ay by)])
(defn v- [[ax ay] [bx by]] [(- ax bx) (- ay by)])
(defn v* [[vx vy] n] [(* vx n) (* vy n)])

(defn length [[x y]]
  (math/sqrt (+ (* x x) (* y y))))

(defn dot [[ax ay] [bx by]]
  (+ (* ax bx) (* ay by)))

(defn norm [v]
  (v* v (/ 1 (length v))))

(defn rotate [[x y] theta]
  [(- (* x (math/cos theta)) (* (math/sin theta)))
   (+ (* x (math/sin theta)) (* (math/cos theta)))])

(defn angle-between [a b]
  (math/acos (dot (norm a) (norm b))))

(defn angle-ahead? [[ax ay] [bx by]]
  (> (- (.atan2 js/Math ay ax) (.atan2 js/Math by bx)) 0))

(defn angle-on-circle [a b]
  (let [theta (angle-between a b)]
    (if (angle-ahead? a b)
      (+ math/pi (- math/pi theta))
      theta)))
