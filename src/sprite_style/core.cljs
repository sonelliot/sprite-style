(ns sprite-style.core)

(enable-console-print!)

(def origin [400 300])
(def angles [0 45 90 135 180 225 270 315])

(def bolo (atom {}))
(def mouse (atom []))

(def renderer (.autoDetectRenderer js/PIXI 800 600))
(def stage (new js/PIXI.Stage 0x284b4d))

(.appendChild (.. js/document -body) (.. renderer -view))

(defn load-movie-clip [name frames]
  (->> (map #(.fromFrame js/PIXI.Texture
                         (str name " " % ".ase"))
            (range frames))
       (into []) clj->js (new js/PIXI.MovieClip)))

(defn mouse-move! [host evt]
  (let [rect (.getBoundingClientRect host)
        posn [(- (.-clientX evt) (.-left rect))
              (- (.-clientY evt) (.-top rect))]]
    (reset! mouse posn)))

; math

(defn len [[x y]]
  (.sqrt js/Math (+ (* x x) (* y y))))

(defn norm [[x y :as v]]
  (let [l (len v)] [(/ x l) (/ y l)]))

(defn dot [[ax ay] [bx by]]
  (+ (* ax bx) (* ay by)))

(defn ahead? [[ax ay] [bx by]]
  (> (- (.atan2 js/Math ay ax) (.atan2 js/Math by bx)) 0))

(defn angle [a b]
  (let [theta (.acos js/Math (dot (norm a) (norm b)))]
    (if (ahead? a b)
      (+ js/Math.PI (- js/Math.PI theta))
      theta)))

(defn snap-unit-circle [a n]
  (let [incr (/ (* 2 js/Math.PI) n)]
    (nth
     (map #(* % incr) (range 0 (inc n)))
     (int (/ a incr)))))

(defn vadd [[ax ay] [bx by]]
  [(+ ax bx) (+ ay by)])

(defn vsub [[ax ay] [bx by]]
  [(- ax bx) (- ay by)])

(defn degrees [r] (* r 57.2957795))

(defn start! [clip]
  (set! (.. clip -visible) true)
  (.gotoAndPlay clip 0))

(defn stop! [clip]
  (set! (.. clip -visible) false)
  (.stop clip))

(defn play-anim! [angle bolo]
  (doseq [[a c] bolo]
    (if (= a angle)
      (set! (.. c -visible) true)
      (set! (.. c -visible) false))))

(defn step! [bolo mouse]
  (let [a (-> (angle [1 0] (vsub mouse origin))
              (snap-unit-circle 8)
              (degrees))]
    (play-anim! (.round js/Math a) bolo)))

(defn load-bolo-anim! [angle [x y]]
  (let [clip (load-movie-clip (str "bolo-walk-" angle) 35)]
    (.set (.. clip -position) x y)
    (.set (.. clip -anchor) 0.5 0.5)
    (set! (.. clip -animationSpeed) 0.5)
    (set! (.. clip -visible) false)
    (.play clip)
    (swap! bolo #(assoc % angle clip))
    (.addChild stage clip)))

(defn animate []
  (js/requestAnimFrame animate)
  (step! @bolo @mouse)
  (.render renderer stage))

(defn load-complete! [element]
  (doseq [[n f] [["mousemove" (partial mouse-move! element)]]]
    (.addEventListener element n f false))
  (doseq [n angles]
    (load-bolo-anim! n origin))
  ;; (let [m (-> (elm/foldp #'step @bolo mouse) elm/port second)]
  ;;   (go (while true (reset! bolo (:value (<! m))))))
  (animate))

(let [loader (new js/PIXI.AssetLoader
                  (clj->js (map #(str "assets/bolo-walk-" % ".json") angles)))]
  (set! (.. loader -onComplete)
        (partial load-complete! (.. renderer -view)))
  (.load loader))

;;;
