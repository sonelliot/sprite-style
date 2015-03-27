(ns sprite-style.elm
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :as a :refer [>! <! alts! chan put!]]
   [cljs.core.async.impl.protocols :as ap]))

(deftype MultiRead [in out]
  ap/WritePort
  (put! [_ v f]
    (ap/put! in v f))
  a/Mult
  (tap* [_ ch close?]
    (a/tap* out ch close?))
  (untap* [_ ch]
    (a/untap* out ch))
  (untap-all* [_]
    (a/untap-all* out)))

(defn event-stream []
  (let [in (chan 100)]
    (MultiRead. in (a/mult in))))

(defn <tap100
  "Create a new channel (buffer 100) that taps the
  argument (multi-channel)."
  [mc]
  (let [c (chan 100)]
    (a/tap mc c)
    c))

(defn port
  "Tap a signal. Returns the default value of signal and a channel
  containing output."
  [[v mc]]
  [v (<tap100 mc)])

(defn change [value]
  {:type :change
   :value value})

(defn no-change [value]
  {:type :no-change
   :value value})

(defn change? [x]
  (= :change (:type x)))

(defn input
  "Create an input node.

   events: an events mult
   id: unique idenfier to listen for messages on
   default: the default value

   returns: a signal"
  [events id default]
  (let [<msgs (<tap100 events)
        >out (chan 100)]
    (go-loop [current (no-change default)]
      (let [[i msg] (<! <msgs)]
        (if (= id i)
          (do
            (>! >out (change msg))
            (recur (no-change msg)))
          (do
            (>! >out current)
            (recur current)))))
    [default (a/mult >out)]))

(defn pulse
  "Create an input node. It returns to default after the signal is
  emitted."
  [events id default]
  (let [<msgs (<tap100 events)
        >out (chan 100)]
    (go-loop [current (no-change default)]
      (let [[i msg] (<! <msgs)]
        (if (= id i)
          (do
            (>! >out (change msg))
            (recur current))
          (do
            (>! >out current)
            (recur current)))))
    [default (a/mult >out)]))

(defn value
  "Create a constant value node, which is an input node that always
  returns the default value."
  [events value]
  (let [<msgs (<tap100 events)
        >out (chan 100)
        current (no-change value)]
    (go-loop []
      (<! <msgs)
      (>! >out current)
      (recur))
    [value (a/mult >out)]))

(defn lift
  "Create a lift node, which calls and maps a function on the values of signals.

  f: the function
  & signals: the signals of arguments to pass to f (in
  order)

  returns: a signal"
  [f & signals]
  (assert (seq signals))
  (let [signals* (map port signals)
        values   (map first  signals*)
        channels (map second signals*)
        <msgs (a/map vector channels)
        >out (chan 100)
        default (apply f values)]
    (go-loop [current (no-change default)]
      (let [msgs (<! <msgs)]
        (if (some change? msgs)
          (let [v (apply f (map :value msgs))]
            (>! >out (change v))
            (recur (no-change v)))
          (do
            (>! >out current)
            (recur current)))))
    [default (a/mult >out)]))

(defn foldp
  "Create a fold node, which folds f over a default value and
  signals.

  f: fold function
  default: default value
  signals: input signals

  returns: a signal"
  [f default & signals]
  (assert (seq signals))
  (let [signals* (map port signals)
        channels (map second signals*)
        <msgs (a/map vector channels)
        >out (chan 100)]
    (go-loop [current (no-change default)]
      (let [msgs (<! <msgs)]
        (if (some change? msgs)
          (let [v (apply f (:value current) (map :value msgs))]
            (>! >out (change v))
            (recur (no-change v)))
          (do
            (>! >out current)
            (recur current)))))
    [default (a/mult >out)]))

(defn async
  "Creates an async node from a signal. These are used mainly to decouple two subgraphs in time.

  in: events input events: signal: signal

  returns: a new signal"
  [events signal]
  (let [[default <msgs] (port signal)
        id (gensym "async_node")]
    (go-loop []
      (let [mi (<! <msgs)]
        (when (change? mi)
          (>! events [id (:value mi)])))
      (recur))
    (input events id default)))
