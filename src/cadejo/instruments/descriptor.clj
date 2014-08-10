(ns cadejo.instruments.descriptor
  (:use [cadejo.util.trace])
  (:require [cadejo.util.user-message :as umsg])
  (:require [cadejo.ui.util.icon]))

(defprotocol InstrumentDescriptor

  (instrument-name
    [this]
    "Return name for this instrument")

  (about 
    [this]
    "Returns terse instrument description")

  (modes
    [this]
    "Returns a list of possible key-modes")

  (logo 
    [this size]
    [this]
    "Returns instruments logo icon. Size argument may be 
     one of :small :medium or :large")

  (controllers
    [this]
    "Returns list of MIDI continuous controllers this instrument
     responds to. The contents are symbolic keywords of the form :cc?
     Where ? is either an integer or letter")

  (controller
    [this key]
    "Returns a map describing specific controller
     key should be one of the keywords returned by controllers.
     The result has the form {:key key       ; same as key argument
                              :usage text    ; brief description 
                              :default int}  ; MIDI cc number")

  (add-controller!
    [this key usage default]
    "Adds MIDI continuous controller
     key     - Keyword which should match a controller argument
               for the instrument's constructors. In general the
               form is :cc? where ? is either an nit or letter
     usage   - brief text describing how controller is used
     default - int, the default MIDI controller number")

  (add-constructor!
    [this mode cfn]
    "Adds an instrument constructor function
     mode - mode should be a keyword indicating the key-mode
            typically this is either :mono or :poly but additional
            modes may be added later. 
     cfn - The instrument constructor function for mode.")

  (create 
    [this mode args]
    "Create an instance of the instrument and link it into a cadejo
     process tree. 
     mode - keyword (typically either :mono or :poly)
     args - list of arguments passed to the constructor.
            [s c id options....]
            Where s is a Cadejo Scene object
                  c is a MIDI channel (0-15)
                  id is a unique identification keyword for this specific 
                  instrument instance
                  All other arguments are optional
                  :main-out - sets the SuperCollider bus the instrument 
                              outputs to. (default 0)
                  
                  :voice-count - Number of poly-phonic voices to create
                                 ignored by mono mode, default 8
 
                  All other options relate to MIDI cc assignments 
                  Use controllers method to discover whats available."))
                       


(defn instrument-descriptor [iname about-text]
  (let [controllers* (atom (sorted-map))
        constructors* (atom (sorted-map))
        dobj (reify InstrumentDescriptor

               (instrument-name [this] 
                 iname)

               (about [this]
                 about-text)

               (modes [this]
                 (keys @constructors*))

               (logo [this]
                 (.logo this :small))

               (logo [this size]
                 (cadejo.ui.util.icon/logo iname size))

               (controllers [this]
                 (keys @controllers*))

               (controller [this kw]
                 (get @controllers* (keyword kw)))

               (add-controller! [this key usage default]
                 (let [kw (keyword key)]
                   (swap! controllers* 
                          (fn [n](assoc n kw
                                        {:key kw
                                         :usage (str usage)
                                         :default (int default)})))))

               (add-constructor! [this mode cfn]
                 (swap! constructors* (fn [n](assoc n (keyword mode) cfn))))
          
               (create [this mode args]
                 (let [cfn (get @constructors* (keyword mode))]
                   (if cfn 
                     (let [p (apply cfn args)]
                       p)
                     (umsg/warning (format "%s does not support %s mode"
                                           iname mode))))) )]
    dobj))

