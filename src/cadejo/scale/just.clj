(ns cadejo.scale.just
  (:require [cadejo.util.math :as math])
  (:require [cadejo.scale.intonation :as intonation]))


(deftype JustScale [properties ftab*]
  intonation/Intonation

  (get-name [this]
    (:name properties))

  (notes-per-octave [this]
    (:notes-per-octave properties))

  (octave-size [this]
    (:octave-size properties))

  (get-frequency [this keynum]
    (nth @ftab* keynum))

  (reference-frequency [this]
    (:reference-frequency properties))

  (reference-key [this]
    (:reference-key properties))

  (template [this]
    (:template properties))

  (dump [this verbose depth]
    (let [pad1 (cadejo.util.string/tab depth)
          pad2 (cadejo.util.string/tab (inc depth))]
      (printf "%sJustScale %s\n" pad1 (.get-name this))
      (if verbose
        (do 
          (printf "%snotes per octave : %s\n" pad2 (.notes-per-octave this))
          (printf "%soctave-size      : %s\n" pad2 (.octave-size this))
          (printf "%sreference-key    : %s\n" pad2 (.reference-key this))
          (printf "%sreference-freq   : %s\n" pad2 (.reference-frequency this))))))
  
  (dump [this verbose]
    (.dump this verbose 0))

  (dump [this]
    (.dump this true)))

(def scales {:just-c1 {:template '[1/1 16/15 9/8 6/5 5/4 4/3 36/25 3/2 8/5 5/3 9/5 15/8]
                       :name "just-c1"
                       :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :44-39-12 {:template '[1/1 14/13 44/39 13/11 14/11 4/3 56/39 3/2 11/7 22/13 39/22 21/11]
                        :name "44-39-12"
                        :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :blue-ji {:template '[1 15/14 9/8 6/5 5/4 4/3 7/5 3/2 8/5 5/3 9/5 15/8]
                       :name "blue-ji"
                       :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :pre-archytas {:template'[1 16/15 9/8 6/5 5/4 4/3 64/45 3/2 8/5 5/3 16/9 15/8]
                            :name "pre-archytas"
                            :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :bicycle {:template '[1 13/12 9/8 7/6 5/4 4/3 11/8 3/2 13/8 5/3 7/4 11/6]
                       :name "bicycle"
                       :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :breedball3 {:template '[1 49/48 21/20 15/14 48/40 5/4 7/5 10/7 3/2 49/32 12/7 7/4]
                          :name "breedball3"
                          :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :al-farabi {:template '[1/1 256/243 9/8 32/27 81/64 4/3 1024/729 3/2 128/81 27/16 7/4 16/9]
                         :name "al-farabi"
                         :source "http://www.chrysalis-foundation.org/Al-Farabi-s_Uds.htm"
                         :remarks "The source scale is diatonic on C. The 'black keys' were derived from the original scale with a root on the e-flat key"} 
             :canton {:template '[1/1 14/13 9/8 13/11 14/11 4/3 39/28 3/2 11/7 22/13 16/9 13/7]
                      :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :carlos-harm {:template '[1/1 17/16 9/8 19/16 5/4 21/16 11/8 3/2 13/8 27/16 7/4 15/8]
                           :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"
                           :remarks "Carlos Harmonic & Ben Johnston's scale of 'Blues' from Suite f.micr.piano (1977) & David Beardsley's scale of 'Science Friction'"}
             :centaur {:template '[1/1 21/20 9/8 7/6 5/4 4/3 7/5 3/2 14/9 5/3 7/4 15/8]
                           :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"
                           :remarks "A 7-limit scale"}
             :collapsar {:template '[1/1 15/14 49/44 7/6 5/4 15/11 7/5 3/2 35/22 5/3 7/4 21/11]
                           :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"
                           :remarks "An 11-limit scale"}
             :major-clus {:template '[1/1 135/128 10/9 9/8 5/4 4/3 45/32 3/2 5/3 27/16 16/9 15/8]
                          :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :minor-clus {:template '[1/1 16/15 9/8 6/5 4/3 27/20 46/45 3/2 8/5 27/16 16/9 9/5]
                          :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :thirteendene {:template '[1/1 13/12 9/8 6/5 9/7 27/20 13/9 3/2 8/5 27/16 9/5 27/14]
                            :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             :unimajor {:template '[1/1 22/21 9/8 32/27 14/11 4/3 63/44 3/2 11/7 27/16 16/9 21/11]
                        :source "http://xenharmonic.wikispaces.com/Gallery+of+12-tone+Just+Intonation+Scales"}
             })

(defn just-scale 
  ([scale-id a440]
     (let [notes-per-octave 12
           octave-size 2.0
           c0 (intonation/a69->c0 a440 notes-per-octave octave-size)
           sobj (get scales scale-id)]
       (if (not sobj)
         (do
           (print (format "WARNING: Unknown just scale %s" scale-id))
           (println "  Using default scale :just-c1")
           (just-scale :just-c1))
         (let [template (:template sobj)
               properties {:name (:name sobj)
                           :notes-per-octave notes-per-octave
                           :octave-size octave-size
                           :template template
                           :reference-key 69
                           :reference-frequency (float a440)}
               ftab* (atom [])
               keynum* (atom 0)]
           (while (< @keynum* 128)
             (let [key-class (rem @keynum* 12)
                   ratio (nth template key-class)
                   bfreq (float (* c0 ratio))
                   octave (int (/ @keynum* 12))
                   oxpose (math/expt 2 octave)
                   freq (* oxpose bfreq)]
               (swap! ftab* (fn [n](conj n freq)))
               (swap! keynum* inc)))
           (JustScale. properties ftab*)))))
  ([scale-id]
     (just-scale scale-id 440.0))
  ([]
     (just-scale :just-c1)))


(defn ?just-scales []
  "Display list of just available just scale templates"
  (println "Ava-liable just scales:")
  (doseq [s (keys scales)]
    (printf "\t%s\n" s))
  (println))
