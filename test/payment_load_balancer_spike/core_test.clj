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
  (swap! repository update-in [(smallest-bucket @repository)] conj candidate))

(defn
  generate-payments
  [number]
  (map #(-> {:id %}) (range 100)))

(facts
  "about the rules"
  (fact
    "splitting evenly in two buckets"
    (let [repository (atom {:bucket2 [] :bucket1 []})
          _ (doall
              (->>
                (generate-payments 100)
                (map #(process % repository))))]
      (count (get @repository :bucket1)) => 50
      (count (get @repository :bucket2)) => 50
      )))


