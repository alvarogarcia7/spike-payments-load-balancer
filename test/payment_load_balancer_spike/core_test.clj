(ns payment-load-balancer-spike.core-test
  (:use [midje.sweet])
  (:require
    [payment-load-balancer-spike.core :refer :all]))


(defn
  generate-payments
  [number]
  (map #(-> {:id %}) (range number)))

(facts
  "about the rules"
  (fact
    "splitting evenly in two buckets"
    (fact
      "when both buckets are defined"
      (let [repository (atom {:bucket2 [] :bucket1 []})
            process (partial process [{:fn smallest-bucket}])
            _ (doall
                (->>
                  (generate-payments 100)
                  (map #(process % repository))))]
        (count (get @repository :bucket1)) => 50
        (count (get @repository :bucket2)) => 50))
    (fact
      "when one of the buckets is not defined"
      (let [repository1 (atom {:bucket2 []})
            process (partial process [{:fn (fn [m] (if (>= (count (get m :bucket2)) 5) :bucket1 :bucket2))}])
            _ (doall
                (->>
                  (generate-payments 10)
                  (map #(process % repository1))))]
        (count (get @repository1 :bucket1)) => 5
        (count (get @repository1 :bucket2)) => 5))
    (fact
      "when none of the buckets is defined"
      (let [repository1 (atom {})
            process (partial process [{:fn (fn [m] (if (>= (count (get m :bucket2)) 5) :bucket1 :bucket2))}])
            _ (doall
                (->>
                  (generate-payments 10)
                  (map #(process % repository1))))]
        (count (get @repository1 :bucket1)) => 5
        (count (get @repository1 :bucket2)) => 5))

    ))


