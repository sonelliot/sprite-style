(ns sprite-style.core
  (:require
   [sprite-style.entities :refer [->Bolo]]
   [sprite-style.protocols :refer [awake step render]]))

(enable-console-print!)

(def assets (map #(str "assets/bolo-walk-" % ".json") (range 0 360 10)))
(def origin [400 300])

(def bolo (atom {}))
(def mouse (atom []))

(def renderer (.autoDetectRenderer js/PIXI 800 600))
(def stage (new js/PIXI.Stage 0x284b4d))

(.appendChild (.. js/document -body) (.. renderer -view))

(defn mouse-move! [host evt]
  (let [rect (.getBoundingClientRect host)
        posn [(- (.-clientX evt) (.-left rect))
              (- (.-clientY evt) (.-top rect))]]
    (reset! mouse posn)))

(defn animate []
  (js/requestAnimFrame animate)
  (swap! bolo #(-> % (step @mouse) render))
  (.render renderer stage))

(defn load-complete! [element]
  (doseq [[n f] [["mousemove" (partial mouse-move! element)]]]
    (.addEventListener element n f false))
  (reset! bolo (awake (->Bolo "bolo-walk" 36 36 origin [1 0] {}) stage))
  (animate))

(let [loader (new js/PIXI.AssetLoader (clj->js assets))]
  (set! (.. loader -onComplete)
        (partial load-complete! (.. renderer -view)))
  (.load loader))
