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
  (doall (map #(process repository rules %) payments)))

(def
  test-rules
  {:only-5-in-bucket2 (fn [history] (if (>= (count (get history :bucket2)) 5) :bucket1 :bucket2))})

(facts
  "about the rules"
  (fact
    "splitting evenly in two buckets"
    (fact
      "when both buckets are defined"
      (let [history (atom {:bucket2 [] :bucket1 []})]
        (process-payments history
                          [{:fn (get rules :smallest-bucket)}]
                          (generate-payments 100))
        (count (get @history :bucket1)) => 50
        (count (get @history :bucket2)) => 50
        )
      )
    (fact
      "when one of the buckets is not defined"
      (let [history (atom {:bucket2 []})]
        (process-payments history
                          [{:fn (get test-rules :only-5-in-bucket2)}]
                          (generate-payments 10))
        (count (get @history :bucket1)) => 5
        (count (get @history :bucket2)) => 5
        ))
    (fact
      "when none of the buckets is defined"
      (let [history (atom {})]
        (process-payments history
                          [{:fn (get test-rules :only-5-in-bucket2)}]
                          (generate-payments 10))
        (count (get @history :bucket1)) => 5
        (count (get @history :bucket2)) => 5
        ))
    ))



