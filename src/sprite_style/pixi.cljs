(ns sprite-style.pixi)

(defn load-clip [anim ext frames]
  (->> (map #(.fromFrame js/PIXI.Texture (str anim "-" % ext))
            (range frames))
       clj->js (new js/PIXI.MovieClip)))
