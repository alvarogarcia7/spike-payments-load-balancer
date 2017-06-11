(ns payment-load-balancer-spike.core-test
  (:use [midje.sweet])
  (:require
    [payment-load-balancer-spike.core :refer :all]))

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
  [candidate repository]
  (let [new-bucket (update @repository (smallest-bucket @repository) conj candidate)]
    (reset! repository new-bucket)
    ))

(defn
  generate-payments
  [number]
  (map #(-> {:id %}) (range 100)))

(facts
  "about the rules"
  (fact
    "add 100 payments"
    (let [payments100 (atom {:bucket2 [] :bucket1 []})
          _ (doall
              (->>
                (generate-payments 100)
                (map #(process % payments100))))]
      (fact
        "splits evenly by number of payments"
        (count (get @payments100 :bucket1)) => 50
        (count (get @payments100 :bucket2)) => 50
        ))))


