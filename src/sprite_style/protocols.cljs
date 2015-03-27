(ns sprite-style.protocols)

(defprotocol Entity
  (awake [this stage])
  (step [this mouse]))

(defprotocol Renderable
  (render [this]))
