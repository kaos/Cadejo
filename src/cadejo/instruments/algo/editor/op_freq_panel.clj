(ns cadejo.instruments.algo.editor.op-freq-panel 
  (:use [cadejo.instruments.algo.algo-constants])
  (:require [cadejo.instruments.algo.editor.factory :as factory])  
  (:require [cadejo.util.math :as math])
  (:require [cadejo.ui.util.lnf :as lnf])
  (:require [sgwr.indicators.displaybar :as dbar])
  (:require [sgwr.components.line :as line])
  (:require [sgwr.components.text :as text]))
  


(defn op-freq [n drw p0 ied]
  (let [op-id (keyword (format "op%d" n))
        param-freq (keyword (format "op%d-detune" n))
        param-bias (keyword (format "op%d-bias" n))
        root (.root drw)
        tools (.tool-root drw)
        [x0 y0] p0

        x-bias (+ x0 72) 
        y-bias (- y0 45)
        x-bias-button (+ x-bias 215)
        y-bias-button (+ y-bias 2.5)

        x-freq x-bias
        y-freq (- y-bias 40)
        x-freq-button (+ x-freq 215)
        y-freq-button (+ y-freq 2.5)

        width 350
        height 100
        dbar-bias (factory/displaybar root [x-bias y-bias] 7)
        action-edit-bias (fn [b _]
                           (dbar/displaybar-dialog dbar-bias
                                                   (format "Bias op %d" n)
                                                   :validator (fn [q]
                                                                (let [b (math/str->float q)]
                                                                  b))
                                                   :callback (fn [_]
                                                               (let [s (.current-display dbar-bias)
                                                                     b (math/str->float s)]
                                                                 (.set-param! ied param-bias b)))))
        b-bias (factory/mini-edit-button tools [x-bias-button y-bias-button]
                                 op-id action-edit-bias)

        dbar-freq (factory/displaybar root [x-freq y-freq] 7)
        action-edit-freq (fn [b _]
                           (dbar/displaybar-dialog dbar-freq
                                                   (format "frequency op %d" n)
                                                   :validator (fn [q]
                                                                (let [f (math/str->float q)]
                                                                  (>= f 0)))
                                                   :callback (fn [_]
                                                               (let [s (.current-display dbar-freq)
                                                                     f (math/str->float s)]
                                                                 (.set-param! ied param-freq f)))))
        b-freq (factory/mini-edit-button tools [x-freq-button y-freq-button]
                                 op-id action-edit-freq)

       
        disable-fn (fn []
                    (doseq [q (list dbar-bias b-bias dbar-freq b-freq)]
                      (.disable! q false)))
        enable-fn (fn []
                    (doseq [q (list dbar-bias b-bias dbar-freq b-freq)]
                      (.enable! q false)))
        sync-fn (fn []
                  (let [dmap (.current-data (.bank (.parent-performance ied)))
                        freq (float (param-freq dmap))
                        bias (float (param-bias dmap))]
                    (.display! dbar-bias (format "%+7.4f" bias) false)
                    (.display! dbar-freq (format "%7.4f" freq) false)))]

    (factory/inner-border root [x0 y0][(+ x0 width)(- y0 height)])
    (factory/section-label root [(+ x0 10)(+ y-freq 20)] op-id "Freq")
    (factory/section-label root [(+ x0 10)(+ y-bias 20)] op-id "Bias")
    (.display! dbar-freq " ")
    (.display! dbar-bias " ")
    {:sync-fn sync-fn
     :disable-fn disable-fn
     :enable-fn enable-fn}))



