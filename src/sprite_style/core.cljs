(ns sprite-style.core)

(enable-console-print!)

(def renderer (.autoDetectRenderer js/PIXI 800 600))
(def stage (new js/PIXI.Stage 0x284b4d))

(.appendChild (.. js/document -body) (.. renderer -view))

(def bolo (atom nil))

(defn load-sprite-sheet [url proc]
  (doto (new js/PIXI.SpriteSheetLoader url)
    (.on "loaded" #(proc %)) .load))

(defn load-movie-clip [n]
  (->> (map #(.fromFrame js/PIXI.Texture
                         (str "bolo-walk " % ".ase")) (range n))
       (into []) clj->js (new js/PIXI.MovieClip)))

(defn animate []
  (js/requestAnimFrame animate)
  (.render renderer stage))

(load-sprite-sheet "assets/bolo-walk.json"
                   #(let [clip (reset! bolo (load-movie-clip 35))]
                      (.set (.. clip -position) 350 250)
                      (.set (.. clip -anchor) 0.5 0.5)
                      (.set (.. clip -scale) 1 1)
                      (set! (.. clip -rotation) 0)
                      (.gotoAndPlay clip 0)
                      (.addChild stage clip)))
(animate)
