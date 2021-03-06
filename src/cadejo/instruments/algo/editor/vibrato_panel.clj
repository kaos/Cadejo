(ns cadejo.instruments.algo.editor.vibrato-panel 
  (:use [cadejo.instruments.algo.algo-constants])
  (:require [cadejo.ui.util.sgwr-factory :as sfactory])
  (:require [cadejo.ui.util.lnf :as lnf])
  (:require [cadejo.util.math :as math])
  (:require [sgwr.components.line :as line])
  (:require [sgwr.components.text :as text])
  (:require [sgwr.indicators.displaybar :as dbar])
  (:require [sgwr.tools.slider :as slider]))

(defn vibrato-panel [drw p0 ied]
  (let [param-freq :vfreq
        param-sens :vsens
        param-depth :vdepth
        param-delay :vdelay
        id :vibrato
        root (.root drw)
        tools (.tool-root drw)
        [x0 y0] p0
        x-dbar (+ x0 20)
        x-edit (+ x-dbar 180)
        x-sens (+ x-edit 80)
        x-depth (+ x-sens slider-spacing)
        x-delay (+ x-depth slider-spacing)
        y-dbar (- y0 120)
        y-edit (+ y-dbar 0)
        y-sliders (- y0 32)
        y-labels (- y0 10)
        dbar (sfactory/displaybar drw [x-dbar y-dbar] 6)
        edit-action (fn [b _]
                       (dbar/displaybar-dialog dbar
                                               "Vibrato Frequency"
                                               :validator (fn [q]
                                                            (let [b (math/str->float q)]
                                                              (and b (pos? b))))
                                               :callback (fn [_]
                                                           (let [s (.current-display dbar)
                                                                 b (math/str->float s)]
                                                             (.set-param! ied param-freq b)))))
        b-edit (sfactory/mini-edit-button drw [x-edit y-edit] id edit-action)
        slider-action (fn [s _]
                        (let [p (.get-property s :id)
                              v (slider/get-slider-value s)]
                          (.set-param! ied p v)))
        s-sens (sfactory/vslider drw ied param-sens [x-sens y-sliders] 0.0 max-vibrato-sensitivity slider-action)
        s-depth (sfactory/vslider drw ied param-depth [x-depth y-sliders] 0.0 1.0 slider-action)
        s-delay (sfactory/vslider drw ied param-delay [x-delay y-sliders] 0.0 max-vibrato-delay slider-action)
        sync-fn (fn []
                  (let [dmap (.current-data (.bank (.parent-performance ied)))
                        freq (float (param-freq dmap))
                        sens (float (param-sens dmap))
                        depth (float (param-depth dmap))
                        delay (float (param-delay dmap))]
                    (.display! dbar (format "%6.4f" freq) false)
                    (slider/set-slider-value! s-sens sens false)
                    (slider/set-slider-value! s-depth depth false)
                    (slider/set-slider-value! s-delay delay false)))]
    (sfactory/label drw [(- x-sens 12) y-labels] "Sens")
    (sfactory/label drw [(- x-depth 14) y-labels] "Depth")
    (sfactory/label drw [(- x-delay 14) y-labels] "Delay")
    (sfactory/text drw [(+ x-dbar 38)(- y-dbar 20)] "Frequency")
    (sfactory/title drw [(+ x0 13)(- y0 224)] "Vibrato")
    ;; rules
    (let [x1 (- x-sens 10)
          x2 (+ x-delay 10)
          xtx (- x1 35)
          vn1 (- y-sliders 0)
          vp1 (- vn1 slider-length)
          v0 (math/mean vn1 vp1)
          vline (fn [y c] 
                  (line/line root [x1 y][x2 y] :id id
                             :style :dotted
                             :color c))
          minor (fn [y n]
                  (vline y (lnf/minor-tick))
                  (text/text root [xtx (+ y 5)] (format "%4.2f" n)
                             :style :mono
                             :size 6
                             :color (lnf/minor-tick)))]
      (minor vn1 0.0)
      (minor vp1 1.0)
      (minor v0 0.5)
      (minor (math/mean v0 vp1) 0.75)
      (minor (math/mean v0 vn1) 0.25))
    (sfactory/minor-border drw [x0 y0][(+ x0 410)(- y0 260)])
    {:sync-fn sync-fn }))
