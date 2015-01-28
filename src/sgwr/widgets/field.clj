(ns sgwr.widgets.field
  "Defines 2-dimensional widget for controlling values on 2-axis 
  simultaneously.

  Each field is a rectangular area which contains any number of 
  'balls'. Balls are used to display/alter specific values."
  
  (:require [sgwr.constants :as constants])
  (:require [sgwr.elements.group :as group])
  (:require [sgwr.elements.point :as point])
  (:require [sgwr.elements.rectangle :as rect])
  (:require [sgwr.util.math :as math])
  (:require [sgwr.util.utilities :as utilities]))

(let [counter* (atom 0)]
  (defn- get-field-id [id]
    (let [n @counter*]
      (swap! counter* inc))
    (or id (keyword (format "field-%d" @counter*)))))
    
(defn- dummy-action [& _] nil)

(defn set-ball-value! 
  "(set-ball-value! fobj id val)
   (set-ball-value! fobj id val render?)

   Set the value of a specific ball

   fobj - SgwrElement group which contains the field.
   id   - keyword, the unique ball id to be altered
   val  - vector [x y], the new value for ball id
   render? - boolean if true render drawing after update,
             default true

   Returns either the indicated ball or nil."

  ([fobj id val]
   (set-ball-value! fobj id val :render))
  ([fobj id val render?]
  (let [ball (get @(.get-property fobj :balls*) id)]
    (if ball 
      (let [mapx (.get-property fobj :fn-x-val->pos)
            mapy (.get-property fobj :fn-y-val->pos)
            xpos (mapx (first val))
            ypos (mapy (second val))
            xval ((.get-property fobj :fn-x-pos->val) xpos)
            yval ((.get-property fobj :fn-y-pos->val) ypos)
            p [(mapx (first val))(mapy (second val))]]
        (.set-points! ball [p])
        (.put-property! ball :value [xval yval])
        (if render? (.render (.get-property fobj :drawing)))
        ball)
      (do (utilities/warning (format "field %s does not have ball with id %s" 
                                     (.get-property fobj :id) id))
          nil)))))
        
 
(defn- select-ball [obj ev]
  (let [cs (.coordinate-system obj)
        pos (.inv-map cs [(.getX ev)(.getY ev)])
        d* (atom constants/infinity)
        ball* (atom nil)]
    (doseq [b (vals @(.get-property obj :balls*))]
      (let [d (.distance b pos)]
        (.use-attributes! b :default)
        (if (< d @d*)
          (do 
            (reset! d* d)
            (reset! ball* b)))))
    (if @ball*
      (let [b @ball*]
        (reset! (.get-property obj :current-ball*) b)
        (.use-attributes! b :selected)
        b))))

(defn- compose-move-action [mfn]
  (fn [obj ev]
    (select-ball obj ev)
    (mfn obj ev)
    (.render (.get-property obj :drawing))))

(defn- compose-drag-action [dfn]
  (fn [obj ev]
    (select-ball obj ev)
    (let [b @(.get-property obj :current-ball*)]
      (if b
        (let [cs (.coordinate-system obj)
              pos (.inv-map cs [(.getX ev)(.getY ev)])
              mapx (.get-property obj :fn-x-pos->val)
              mapy (.get-property obj :fn-y-pos->val)
              val [(mapx (first pos))(mapy (second pos))]]
          (.set-points! b [pos])
          (.put-property! b :value val)))
      (dfn obj ev)
      (.render (.get-property obj :drawing)))))


(defn field [parent p0 p1 range-x range-y & {:keys [id
                                                    drag-action move-action enter-action exit-action
                                                    press-action release-action click-action
                                                    pad-color 
                                                    rim-color rim-style rim-width rim-radius]
                                             :or {id nil
                                                  drag-action nil
                                                  move-action nil 
                                                  enter-action nil
                                                  exit-action nil
                                                  press-action nil
                                                  release-action nil
                                                  click-action nil
                                                  pad-color [0 0 0 0]
                                                  rim-color :gray
                                                  rim-style 0
                                                  rim-width 1.0
                                                  rim-radius 0}}]
  "(field parent p0 p1 range-x range-y 
          :id :drag-action :move-action :enter-action :exit-action 
          :press-action :release-action :click-action 
          :pad-color 
          :rim-color :rim-style :rim-width :rim-radius)

   Creates field widget. The field is initially empty and does not 
   contain control 'balls'. Use the ball function to add control elements
   
   parent  - SgwrElement, the parent group
   p0      - vector [x0 y0], coordinates of bounding vertex
   p1      - vector [x1 y1], coordinate of opposing vertex
   range-x - vector [v1 v2], horizontal field value range
   range-y - vector [u1 u2], vertical field value range
   :id     - keyword, if not specified a unique id will be generated.

   Actions 

   :drag-action, :move-action, :enter-action, :exit-action,
   :press-action, :release-action, :click-action
       
   Function of form (fn [obj ev] ...) where obj is this widget
   and ev is an instance of java.awt.event.MouseEvent 

   :pad-color - Color, keyword or vector, background color
   :rim-color  - Outer rim color
   :rim-style  - Rim dash pattern, default :solid
   :rim-width  - Rim line width, default 1.0
   :rim-radius - int, Rim/pad corner radius, default 12

   Returns SgwrElement group containing field elements."
           
  (let [grp (group/group parent 
                         :etype :field
                         :id (get-field-id id))
        pad (let [pad (rect/rectangle grp p0 p1
                                      :id :pad
                                      :color pad-color
                                      :fill true)]
              (.put-property! pad :corner-radius rim-radius)
              (.color! pad :rollover pad-color)
              pad)
        rim (let [rim (rect/rectangle grp p0 p1
                                      :id :rim
                                      :color rim-color
                                      :style rim-style
                                      :width rim-width
                                      :fill :no)]
              (.put-property! rim :corner-radius rim-radius)
              rim)]
    (.put-property! grp :pad pad)
    (.put-property! grp :rim rim)
    (.put-property! grp :balls* (atom {}))
    (.put-property! grp :current-ball* (atom nil))
    (.put-property! grp :action-mouse-dragged  (compose-drag-action (or drag-action dummy-action)))
    (.put-property! grp :action-mouse-moved    (compose-move-action (or move-action dummy-action)))
    (.put-property! grp :action-mouse-entered  (or enter-action dummy-action))
    (.put-property! grp :action-mouse-exited   (or exit-action dummy-action))
    (.put-property! grp :action-mouse-pressed  (or press-action dummy-action))
    (.put-property! grp :action-mouse-released (or release-action dummy-action))
    (.put-property! grp :action-mouse-clicked  (or click-action dummy-action))
    (.put-property! grp :range-x range-x)
    (.put-property! grp :range-y range-y)
    (.put-property! grp :fn-x-pos->val (math/clipped-linear-function (first p0)
                                                                     (first range-x)
                                                                     (first p1)
                                                                     (second range-x)))
    (.put-property! grp :fn-x-val->pos (math/clipped-linear-function (first range-x)
                                                                     (first p0)
                                                                     (second range-x)
                                                                     (first p1)))
    (.put-property! grp :fn-y-pos->val (math/clipped-linear-function (second p0)
                                                                     (second range-y)
                                                                     (second p1)
                                                                     (first range-y)))
    (.put-property! grp :fn-y-val->pos (math/clipped-linear-function (second range-y)
                                                                     (second p0)
                                                                     (first range-y)
                                                                     (second p1)))
    grp))
    
    
(defn ball [parent id init-value & {:keys [color style size 
                                           selected-color selected-style]
                                    :or {color :white
                                         style [:dot]
                                         size 3
                                         selected-color constants/default-rollover-color
                                         selected-style [:fill :dot]}}]
  "(ball parent id init-value
         :color :style :size
         :selected-color :selected-style

   Create control 'ball' object and add it to parent fie;d.

   parent     - SgwrElement, a group holding field widget
   id         - keyword, each ball within any given field must have a unique id.
   init-value - vector [x y], the initial value/position of the ball.
   
  :color - Color, keyword or vector, see sgwr.util.color/color
  :style - vector, point style, see sgwr.element.point, default [:dot]
  :size  - float, point size, default 3

  :selected-color - Color when ball is active, default to rollover color
  :selected-style - Shape when ball is active, defaults to [:fill :dot]

  Returns SgwrElement, the point element."

  (let [mapx (.get-property parent :fn-x-val->pos)
        mapy (.get-property parent :fn-y-val->pos)
        pnt (point/point parent [(mapx (first init-value))(mapy (second init-value))]
                         :id id
                         :color color
                         :style style
                         :size size)]
    (swap! (.get-property parent :balls*)
           (fn [q](assoc q id pnt)))
    (.put-property! pnt :value init-value)
    (.color! pnt :rollover color)
    (.color! pnt :selected selected-color)
    (.style! pnt :selected selected-style)
    pnt))    