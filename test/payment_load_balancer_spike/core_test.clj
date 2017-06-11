(ns payment-load-balancer-spike.core-test
  (:use [midje.sweet])
  (:require
    [payment-load-balancer-spike.core :refer :all]))

(defn
  next-bucket
  [m]
  (let [key-and-size (reduce (fn [acc [k v]] (conj acc {:key k :length (count v)})) '() m)
        by-size #(> (:length %2) (:length %1))]
    (:key (first (sort by-size key-and-size)))))

(defn
  process
  [candidate repository]
  (let [new-bucket (update @repository (next-bucket @repository) conj candidate)]
    (reset! repository new-bucket)
    ))

(defn
  add-payments
  [number]
  (map #(-> {:id %}) (range 100)))

(facts
  "about the rules"
  (fact
    "add 100 payments"
    (let [payments100 (atom {:bucket2 [] :bucket1 []})
          _ (doall
              (->>
                (add-payments 100)
                (map #(process % payments100))))]
      (fact
        "splits evenly by number of payments"
        (println @payments100)
        (count (get @payments100 :bucket1)) => 50
        (count (get @payments100 :bucket2)) => 50
        ))))


