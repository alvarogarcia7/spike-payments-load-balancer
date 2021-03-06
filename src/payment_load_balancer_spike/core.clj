(ns payment-load-balancer-spike.core
  (:gen-class))



(def
  rules
  {:smallest-bucket
   (fn [history]
     (let [field :length
           key-and-size (fn [history]
                          (map (fn [[bucket-name payments]] {:key bucket-name field (count payments)}) history))
           decreasing-by-size #(> (field %2) (field %1))]
       (->>
         history
         key-and-size
         (sort decreasing-by-size)
         first
         :key)))})

(defn
  process
  [repository rules candidate]
  (let [bucket-name ((:fn (first rules)) @repository)
        _ (if-not (find @repository bucket-name)
            (swap! repository assoc bucket-name []))]
    (let [add-to-bucket (fn [bucket-name] (swap! repository update-in [bucket-name] conj candidate))]
      (add-to-bucket bucket-name))))

(defn -main
  [& args]
  (println "Hello, World!"))
