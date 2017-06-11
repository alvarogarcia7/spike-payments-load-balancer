(ns payment-load-balancer-spike.core
  (:gen-class))

(defn
  key-and-size
  [history]
  (reduce (fn [acc [k v]] (conj acc {:key k :length (count v)})) '() history))

(def
  rules
  {:smallest-bucket
   (fn [history]
     (let [key-and-size (key-and-size history)
           decreasing-by-size #(> (:length %2) (:length %1))]
       (->>
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
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
