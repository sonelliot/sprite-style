(ns sprite-style.entities
  (:require
   [sprite-style.math :as math]
   [sprite-style.protocols :refer [Entity Renderable]]
   [sprite-style.pixi :as pixi]
   [sprite-style.vector :as vec :refer [v-]]))

(defn load-anim [name angle frames]
  [angle (pixi/load-clip (str name "-" angle) ".png" frames)])

(defn load-bolo-anim [name angle frames [x y] stage]
  (let [[a c :as anim] (load-anim name angle frames)]
    ;; (.set (.. c -scale) 5 5)
    (.set (.. c -position) x y)
    (.set (.. c -anchor) 0.5 0.5)
    (set! (.. c -animationSpeed) 0.5)
    (set! (.. c -visible) false)
    (.play c)
    (.addChild stage c)
    anim))

(defrecord Bolo [name frames angles position facing clips]
  Entity
  (awake [{:keys [name frames angles position] :as bolo} stage]
    (let [degree (math/round (/ 360 angles))]
      (assoc bolo :clips
             (->> (map #(load-bolo-anim name (* degree %) frames position stage)
                       (range angles))
                  (into {})))))
  (step [{:keys [position] :as bolo} mouse]
    ;; (update-in bolo [:facing]
    ;;            #(vec/norm (vec/rotate % )))
    (assoc bolo :facing (vec/norm (v- position mouse))))
  Renderable
  (render [{:keys [facing clips angles] :as bolo}]
    (let [angle (-> (vec/angle-on-circle facing [1 0])
                    (math/snap-unit-circle angles)
                    (math/degrees))]
      (doseq [[a c] clips]
        (if (= a (math/round angle))
          (set! (.. c -visible) true)
          (set! (.. c -visible) false))))
    bolo))
