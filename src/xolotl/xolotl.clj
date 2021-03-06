(println "--> Loading Xolotl Sequencer")
(ns xolotl.xolotl
  (:require [xolotl.util])
  (:require [xolotl.cycle])
  (:require [xolotl.program])
  (:require [xolotl.program-bank])
  (:require [xolotl.clock])
  (:require [xolotl.controllers])
  (:require [xolotl.counter])
  (:require [xolotl.eventgen])
  (:require [xolotl.pitch])
  (:require [xolotl.shift-register])
  (:require [xolotl.timebase])
  (:require [xolotl.xobj])
  (:require [xolotl.xseq])
  (:require [xolotl.ui.bank-editor])
  (:require [xolotl.ui.channel-editor])
  (:require [xolotl.ui.clock-editor])
  (:require [xolotl.ui.controller-editor])
  (:require [xolotl.ui.factory])
  (:require [xolotl.ui.hold-editor])
  (:require [xolotl.ui.monitor])
  (:require [xolotl.ui.pitch-editor])
  (:require [xolotl.ui.rhythm-editor])
  (:require [xolotl.ui.sr])
  (:require [xolotl.ui.strum-editor])
  (:require [xolotl.ui.velocity-editor])
  (:require [xolotl.ui.xeditor]))


(defn create-sequencer [nodes*]
  (xolotl.xobj/xolotl nodes*))


;;;; TEST TEST TEST TEST TEST
;;;; TEST TEST TEST TEST TEST
;;;; TEST TEST TEST TEST TEST

;(def x (create-sequencer nil))
