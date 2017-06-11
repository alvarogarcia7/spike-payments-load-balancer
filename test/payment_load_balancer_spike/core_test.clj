(ns payment-load-balancer-spike.core-test
  (:use [midje.sweet])
  (:require
    [payment-load-balancer-spike.core :refer :all]))

(defn
  process
  [candidate repository]
  (let [next-bucket (fn [m] (:key (first (sort #(> (:length %2) (:length %1)) (reduce (fn [acc [k v]] (conj acc {:key k :length (count v)})) '() m)))))
        new-bucket (update @repository (next-bucket @repository) conj candidate)]
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


