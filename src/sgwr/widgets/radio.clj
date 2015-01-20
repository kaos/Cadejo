(ns sgwr.widgets.radio
  (:require [sgwr.elements.circle :as circle])
  (:require [sgwr.elements.group :as group])
  (:require [sgwr.elements.text :as text])
  (:require [sgwr.util.color :as uc]))

(let [counter* (atom 0)]
  (defn- get-button-id [id]
    (let [n @counter*]
      (swap! counter* inc)
      (or id (keyword (format "radio-button-%d" n))))))


(defn clear-radio-button-list! [rbl*]
  (doseq [b @rbl*]
    (.select! b false)
    (.use-attributes! b :default))
  nil)

(defn select-radio-button! [rb]
  (let [rbl* (.get-property rb :radio-button-list*)]
    (clear-radio-button-list! rbl*)
    (.select! rb true)
    (.use-attributes! rb :selected)
    rb))

(defn- compose-pressed-action 
  ([](compose-pressed-action (fn [& _])))
  ([pfn]
   (fn [obj ev]
     (let [rbl* (.get-property obj :radio-button-list* (atom []))
           drw (.get-property obj :drawing)]
       (clear-radio-button-list! rbl*)
       (.select! obj true)
       (.use-attributes! obj :selected)
       (if drw (.render drw))
       (pfn obj ev)))))

(defn- compose-exited-action 
  ([](compose-exited-action (fn [& _])))
  ([xfn]
   (fn [obj ev]
     (let [flag (.selected? obj)
           drw (.get-property obj :drawing)]
       (if flag
         (.use-attributes! obj :selected))
       (xfn obj ev)))))

(defn blank-radio-button [parent rbl* id & {:keys [drag-action move-action enter-action exit-action
                                                   press-action release-action click-action]
                                            :or {drag-action nil
                                                 move-action nil
                                                 enter-action nil
                                                 exit-action nil
                                                 press-action nil
                                                 release-action nil
                                                 click-action nil}}]
  (let [grp (group/group parent :etype :radio-button :id id)
        dummy-action (fn [obj ev] nil)]
    (.put-property! grp :radio-button-list* rbl*)
    (.put-property! grp :action-mouse-dragged  (or drag-action dummy-action))
    (.put-property! grp :action-mouse-moved    (or move-action dummy-action))
    (.put-property! grp :action-mouse-entered  (or enter-action dummy-action))
    (.put-property! grp :action-mouse-exited   (compose-exited-action (or exit-action dummy-action)))
    (.put-property! grp :action-mouse-pressed  (compose-pressed-action (or press-action dummy-action)))
    (.put-property! grp :action-mouse-released (or release-action dummy-action))
    (.put-property! grp :action-mouse-clicked  (or click-action dummy-action))
    (swap! rbl* (fn [q](conj q grp)))
    grp))

(defn radio-button [parent p0 txt rbl* & {:keys [id
                                                 drag-action move-action enter-action exit-action
                                                 press-action release-action click-action
                                                 text-color text-style text-size 
                                                 gap text-x-shift text-y-shift
                                                 c1-color c1-radius
                                                 c2-color c2-radius]
                                          :or {drag-action nil
                                               move-action nil
                                               enter-action nil
                                               exit-action nil
                                               press-action nil
                                               release-action nil
                                               click-action nil
                                               text-color :white
                                               text-style 0
                                               text-size 8
                                               gap 4
                                               text-x-shift 0
                                               text-y-shift 0
                                               c1-color :gray   ;; outer circle
                                               c1-radius 8
                                               c2-color [64 191 64]  ;; inner circle
                                               c2-radius 4}}]
  (let [grp (blank-radio-button parent rbl* (get-button-id id)
                                :drag-action drag-action 
                                :move-action move-action 
                                :enter-action enter-action 
                                :exit-action exit-action
                                :press-action press-action 
                                :release-action release-action 
                                :click-action click-action)
        est-tx-width (text/estimate-monospaced-width (* (count txt) text-size))
        est-tx-height (text/estimate-monospaced-height text-size)
        [x0 y0] p0
        xc (+ x0 c1-radius)
        x2 (+ xc c1-radius)
        x3 (+ x2 gap text-x-shift)
        yc (+ y0 c1-radius)
        y3 (+ yc (* 1/2 est-tx-height) text-y-shift)
        c1 (circle/circle-r grp [xc yc] c1-radius :color c1-color :id :c1)
        c2 (circle/circle-r grp [xc yc] c2-radius :color c2-color :id :c2)
        txobj (text/text grp [x3 y3] txt 
                         :color text-color
                         :id :text
                         :style text-style
                         :size text-size)]
        (.put-property! grp :c1 c1)
        (.put-property! grp :c2 c2)
        (.put-property! grp :text-element txobj)
        (.fill! c2 :default :no)
        (.fill! c2 :selected true)
        (.use-attributes! grp :default)
        grp))
