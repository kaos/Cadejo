(println "    --> xolotl.ui.rhythm-editor")
(ns xolotl.ui.rhythm-editor
  (:require [xolotl.util :as util])
  (:require [xolotl.ui.factory :as factory])
  (:require [seesaw.core :as ss])
  (:import javax.swing.event.ChangeListener))


;; Maps keywords to clock periods. 24 = quarter note
;;
(def ^:private rhythm-map {:W 96, :WT 64, :W. 144,
                           :H 48, :HT 32, :H.  72,
                           :Q 24, :QT 16, :Q.  36,
                           :E 12, :ET  8, :E.  18,
                           :S  6, :ST  4, :S.   9
                           :T  3, :TT  2})

;; Reverse map int clock periods to symbolic keywords.
;;
(def reverse-map (let [acc* (atom {})]
                   (doseq [p (seq rhythm-map)]
                     (swap! acc* (fn [q](assoc q (second p)(first p)))))
                   @acc*))

(def ^:private msg00 "Rhythm parse error: %s")
(def ^:private msg01 "Empty rhythm list")

;; Convert list of ints to string,
;; The reverse of parse-rhythm.
;;
;; RETURNS list (s err)
;;   Where s is the string version of lst, 
;;   and err is any error message. If there are no
;;   errors err is an empty string.
;;
(defn- rhythm-list->str [lst]
  (let [error (StringBuilder.)
        sb (StringBuilder.)
        line-length* (atom 8)]
    (doseq [r lst]
      (let [sym (get reverse-map r)]
        (if sym
          (.append sb (format "%3s " (name sym)))
          (if (util/int? r)
            (.append sb (format "%3d " r))
            (.append error (format "%3s " r))))
        (swap! line-length* dec)
        (if (zero? @line-length*)
          (do 
            (.append sb "\n")
            (reset! line-length* 8)))))
    (list (.toString sb)(.toString error))))

;; Convert string to list of int clock periods.
;; Throws IllegalArgumentException on parsing error.
;;
(defn- parse-rhythm [text]
  (let [acc* (atom [])
        tokens (util/tokenize text)]
    (if (pos? (count tokens))
      (do 
        (doseq [token tokens]
          (let [kw (keyword token)]
            (swap! acc*
                   (fn [q](conj q (or (get rhythm-map kw)
                                      (util/str->int token)
                                      (throw (IllegalArgumentException.
                                              (format msg00 token)))))))))
        @acc*)
      (throw (IllegalArgumentException. msg01)))))

;; Test text for validity as rhythmic pattern
;; If text is valid return false.
;; If text is invalid return offending token as string.
;;
(defn- validator [text]
  (try
    (if (pos? (count text))
      (do 
        (parse-rhythm text)
        false)
      (throw (IllegalArgumentException. msg01)))
    (catch IllegalArgumentException ex
      (.getMessage ex))))

;; Construct rhythm-editor sub-panel
;; ARGS:
;;   parent-editor - an instance of NodeEditor for Xolotl
;;   seq-id - keyword :A or :B
;;
;; RETURNS: map with keys :pan-main -> JPanel
;;                        :sync-fn  -> GUI update function
;;
(defn rhythm-editor [parent-editor seq-id]
  (let [xobj (.node parent-editor)
        xseq (.get-xseq xobj (if (= seq-id :A) 0 1))
        bank (.program-bank xobj)
        enter-action (fn [text]
                       (let [data (parse-rhythm text)
                             prog (.current-program bank)]
                         (.rhythm-pattern! prog seq-id data)
                         (.rhythm-pattern! xseq data)))
                         
        ted (factory/text-editor "Rhythm Pattern"
                                 validator enter-action
                                 factory/rhythm-clipboard*
                                 parent-editor)
        spin-repeat (factory/spinner 0 128 1)
        pan-repeat (factory/border-panel :center spin-repeat
                                         :east (factory/label "Repeat"))
        spin-jump (factory/spinner -1 127 1)
        pan-jump (factory/border-panel :center spin-jump
                                       :east (factory/label "Jump"))
        pan-south (factory/grid-panel 1 2 pan-repeat pan-jump)
        pan-main (:pan-main ted)
        sync-fn (fn [prog]
                  (let [rpat (.rhythm-pattern prog seq-id)
                        rs (rhythm-list->str rpat)
                        pat (first rs)
                        err (second rs)
                        rep (.repeat prog)
                        jmp (.jump prog)]
                    (.setValue spin-repeat (int rep))
                    (.setValue spin-jump (int jmp))
                    (if (zero? (count err))
                      (.setText (:text-area ted)(first rs))
                      (let [msg (format msg00 err)]
                        (.warning! parent-editor msg)
                        (throw (IllegalArgumentException. msg))))))]
    (if (= seq-id :A) (ss/config! pan-main :south pan-south))
    (.addChangeListener spin-repeat (proxy [ChangeListener][]
                                      (stateChanged [_]
                                        (let [prog (.current-program bank)
                                              n (int (.getValue spin-repeat))]
                                          (.repeat! prog n)
                                          (.repeat! xseq n)))))
    
    (.addChangeListener spin-jump (proxy [ChangeListener][]
                                     (stateChanged [_]
                                       (let [prog (.current-program bank)
                                             n (int (.getValue spin-jump))]
                                         (.jump! prog n)
                                         (.jump! xseq n)))))
    {:pan-main pan-main
     :sync-fn sync-fn}))          
        

