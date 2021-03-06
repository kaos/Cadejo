(println "--> cadejo.midi.channel")
(ns cadejo.midi.channel
  "Provides management of a single MIDI channel
   scene-->channel"
  (:require [cadejo.config])
  (:require [cadejo.midi.node])
  (:require [cadejo.util.col :as ucol])
  (:require [cadejo.util.user-message :as umsg])
  (:require [cadejo.util.math])
  (:require [cadejo.util.string])
  (:require [cadejo.ui.midi.channel-editor])
  (:require [overtone.core :as ot]))

(defprotocol ChannelProtocol

  (get-scene
    [this]
    "Returns parent scene")

  (channel-number 
    [this]
    "Return s the MIDI channel number between 0 and 15 inclusive")

  (add-performance! 
    [this id pobj]
    "Add a performance to this channel.
     Each channel may have any number of performances.
     id   - A unique keyword identifying the performance 
     pobj - An instance of Performance.")
  
  (remove-performance! 
    [this id])
  
  (performance-ids 
    [this]
    "Return list of all performance ids for this channel.")
 
  (performance 
    [this id]
    "Return child performance with matching id
     If no such performance exist display warning and return nil")

  (reset 
    [this]
    "reset all control buses to their initial value.")

  ;; (dump 
  ;;   [this verbose depth]
  ;;   [this verbose]
  ;;   [this])
  )

(deftype Channel [parent-scene* children* properties* editor*]

  cadejo.midi.node/Node

  (node-type [this] :channel)

  (is-root? [this] (not @parent-scene*))

  (find-root [this]
    (if (.is-root? this)
      this
      (.find-root (.parent this))))
  
  (parent [this]
    @parent-scene*)

  (children [this]
    (map second (seq @children*)))

  (is-child? [this obj]
    (ucol/member? obj (vals @children*)))

  (add-child! [this obj]
    (if (and (not (.is-child? this obj))
             (= (.node-type obj) :performance))
      (do
        (.add-performance! this (.get-property obj :id) obj)
        (._set-parent! obj this)
        true)
      false))

  (remove-child! [this obj]
    (if (.is-child? this obj)
      (do
        (.remove-performance! this obj)
        (._orphan! obj)
        true)
      false))

  (_orphan! [this]
    (reset! parent-scene* nil))

  (_set-parent! [this parent]
    (reset! parent-scene* parent))
  
  (put-property! [this key value]
    (let [k (keyword key)]
      (swap! properties* (fn [n](assoc n k value)))
      k))

  (remove-property! [this key]
    (let [k (keyword key)]
      (swap! properties* (fn [n](dissoc n k)))
      k))
 
  (get-property [this key default]
    (let [value (or (get @properties* key)
                    (and @parent-scene*
                         (.get-property @parent-scene* key default))
                    default)]
      (if (= value :fail)
        (umsg/warning (format "Channel %s does not have property %s"
                              (.channel-number this) key))
        value)))
  
  (get-property [this key]
    (.get-property this key :fail))

  (local-property [this key]
    (get @properties* key))

  (properties [this local-only]
    (set (concat (keys @properties*)
                 (if (and @parent-scene* (not local-only))
                   (.properties @parent-scene*)
                   nil))))
  
  (properties [this]
    (.properties this false))

  (event-dispatcher [this]
    (fn [event]
      (doseq [p (.children this)]
        ((.event-dispatcher p) event))))
      
  
  (get-editor [this]
    @editor*)

  (set-editor! [this ed]
    (reset! editor* ed))

  ChannelProtocol

  (get-scene [this]
    (.parent this))

  (channel-number [this]
    (.get-property this :channel))

  (add-performance! [this id pobj]
    (swap! children* (fn [n](assoc n id pobj))))
  
  (remove-performance! [this id]
    (swap! children* (fn [n](dissoc n id))))
  
  (performance-ids [this]
    (sort (keys @children*)))

  (performance [this id]
    (or (get @children* id)
        (umsg/warning (format "Channel %s does not have performance %s"
                              (.get-property this :channel) id))))

  (reset [this]
    (doseq [p (.children this)]
      (.reset p)))

  ;; (dump [this verbose depth]
  ;;   (let [depth2 (inc depth)
  ;;         pad (cadejo.util.string/tab depth)
  ;;         pad2 (cadejo.util.string/tab depth2)]
  ;;     (printf "%sChannel %s\n" pad (.channel-number this))
  ;;     (if verbose
  ;;       (do
  ;;         (doseq [k (sort (.properties this :local-only))]
  ;;           (printf "%s[%-12s] --> %s\n" pad2 k (.get-property this k "?")))))
  ;;     (doseq [pid (.performance-ids this)]
  ;;       (let [pobj (get @children* pid)]
  ;;         (.dump pobj verbose depth2)))))
  
  ;; (dump [this verbose]
  ;;   (.dump this verbose 0))

  ;; (dump [this]
  ;;   (.dump this true 0))
  ) 

(defn- load-editor [chanobj]
  (if (cadejo.config/load-gui)
    (cadejo.ui.midi.channel-editor/channel-editor chanobj)))


(defn channel [parent ci]
  "Create new Channel. instance 
   parent - An instance of Scene.
   ci - MIDI channel number ci {0,1,2,...15}
   Channels are not typically created directly. Instead the parent Scene
   calls the function to create it's channels."
  (println (format "--> channel %02d" (cadejo.util.math/abs (- ci 16))))
  (let [children* (atom {})
        properties* (atom {:id (int ci)
                          :channel (int ci)})
        editor* (atom nil)
        cobj (Channel. (atom parent)
                       children*
                       properties*
                       editor*)]
    (reset! editor* (load-editor cobj))
    cobj))
