(ns payment-load-balancer-spike.core-test
  (:use [midje.sweet])
  (:require
    [payment-load-balancer-spike.core :refer :all]))


(defn
  generate-payments
  [number]
  (map #(-> {:id %}) (range number)))


(defn
  process-payments
  [repository rules payments]
  (doall (map #(process rules % repository) payments)))

(def
  rules
  {:only-5-in-bucket2 (fn [m] (if (>= (count (get m :bucket2)) 5) :bucket1 :bucket2))})

(facts
  "about the rules"
  (fact
    "splitting evenly in two buckets"
    (fact
      "when both buckets are defined"
      (let [repository (atom {:bucket2 [] :bucket1 []})]
        (process-payments repository
                          [{:fn smallest-bucket}]
                          (generate-payments 100))
        (count (get @repository :bucket1)) => 50
        (count (get @repository :bucket2)) => 50
        )
      )
    (fact
      "when one of the buckets is not defined"
      (let [repository1 (atom {:bucket2 []})]
        (process-payments repository1
                          [{:fn (get rules :only-5-in-bucket2)}]
                          (generate-payments 10))
        (count (get @repository1 :bucket1)) => 5
        (count (get @repository1 :bucket2)) => 5
        ))
    (fact
      "when none of the buckets is defined"
      (let [repository1 (atom {})]
        (process-payments repository1
                          [{:fn (get rules :only-5-in-bucket2)}]
                          (generate-payments 10))
        (count (get @repository1 :bucket1)) => 5
        (count (get @repository1 :bucket2)) => 5
        ))
    ))



