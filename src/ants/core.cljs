(ns escher.core)

(def canvas (.getElementById js/document "frame"))
(def context (.getContext canvas "2d"))

(def frame {:origin [0 0]
            :e1 [(.-width canvas) 0]
            :e2 [0 (.-height canvas)]})

(defn new-canvas
  ([width height]
     (let [canvas
           (.createElement js/document "canvas")]
       (set! (.-width canvas) width)
       (set! (.-height canvas) height)
       canvas))
  ([] (new-canvas 0 0)))

(defn get-context [canvas]
  (.getContext canvas "2d"))

(defn load-image [image-name callback]
  (let [image (js/Image.)
        canvas (new-canvas 400 400)
        context (get-context canvas)
        canvas-size 400]
    (set! (.-src image) (str "image/" image-name))
    (set! (.-onload image)
          #(do (.drawImage context image 0 0 canvas-size canvas-size)
               (callback canvas)))))

(defn image [image-name]
  (let [image (js/Image.)
        canvas (.createElement js/document "canvas")
        context (.getContext canvas "2d")]
    (set! (.-src image) (str "image/" image-name))
    (set! (.-onload image)
          #(do (set! (.-width canvas) (.-width image))
               (set! (.-height canvas) (.-height image))
               (.drawImage context image 0 0)
               ))
    canvas))

;; (defn flip-vert [p]
;;   (transform-picture p [0 1] [1 1] [0 0]))

;; (defn flip-horiz [p]
;;   (transform-picture p [1 0] [0 0] [1 1]))


(def PI (.-PI js/Math))

(defn to-rad [degree]
  (* PI (/ degree 180)))

(defn rot [im]
  (let [width (.-width im)
        height (.-height im)
        canvas (new-canvas height width)
        context (get-context canvas)]
    (.translate context height 0)
    (.rotate context (to-rad 90))
    (.drawImage context im 0 0 width height)
    canvas))

(defn rot180 [p]
  (rot (rot p)))

(defn rot270 [p]
  (rot (rot (rot p))))

(defn image-painter [image-name context]
  (fn [{[ox oy] :origin
        [e1x e1y] :e1
        [e2x e2y] :e2}]
    (load-image image-name
                (fn [image]
                  (let [width (.-width image)
                        height (.-height image)]
                    (.save context)
                    (.translate context ox oy)
                    (.transform context
                                (/ e1x width)
                                (/ e1y height)
                                (/ e2x width)
                                (/ e2y height)
                                0
                                0)
                    (.drawImage context image 0 0)
                    (.restore context))))))

(defn beside [left right]
  (let [l-height (.-height left)
        l-width (.-width left)
        r-height (.-height right)
        r-width (.-width right)
        multiple (/ r-height l-height)
        l-height (* l-height multiple)
        l-width (* l-width multiple)
        canvas (.createElement js/document "canvas")
        context (.getContext canvas "2d")
        canvas-width (+ l-width r-width)
        canvas-height r-height]
    (set! (.-width canvas) canvas-width)
    (set! (.-height canvas) canvas-height)
    (doto context
      (.drawImage left 0 0 l-width l-height)
      (.drawImage right l-width 0 r-width r-height))
    canvas))

(defn below [p1 p2]
  (rot270 (beside (rot p2)
                  (rot p1))))

(def man (image "man.png"))
(def woman (image "woman.png"))
(def tree (image "tree.png"))

(defn draw [picture]
  (.drawImage context picture 0 0))

(defn manstack [n]
  (if (= n 0)
    man
    (below man (manstack (- n 1)))))

(defn manrow [n]
  (if (= n 0)
    man
    (beside man (manrow (- n 1)))))

(defn log [o]
  (.log js/console o))

(js/setTimeout
 #(do
    (draw (beside (below (beside (below (beside (below man
                                                        woman)
                                                man)
                                        (beside woman tree))
                                 (beside (below (below man woman)
                                                woman)
                                         (below man tree)))
                          (manrow 5))
                  (manstack 2))))
 500
 )
