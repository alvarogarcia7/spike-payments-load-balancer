(ns payment-load-balancer-spike.core
  (:gen-class))

(defn
  smallest-bucket
  [m]
  (let [key-and-size (reduce (fn [acc [k v]] (conj acc {:key k :length (count v)})) '() m)
        decreasing-by-size #(> (:length %2) (:length %1))]
    (->>
      key-and-size
      (sort decreasing-by-size)
      first
      :key)))

(defn
  process
  [rules candidate repository]
  (let [bucket-name ((:fn (first rules)) @repository)
        _ (if-not (find @repository bucket-name)
            (swap! repository assoc bucket-name []))]
    (let [add-to-bucket (fn [bucket-name] (swap! repository update-in [bucket-name] conj candidate))]
      (add-to-bucket bucket-name))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
