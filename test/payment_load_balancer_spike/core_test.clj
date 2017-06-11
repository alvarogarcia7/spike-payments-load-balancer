(ns payment-load-balancer-spike.core-test
  (:use [midje.sweet])
  (:require
    [payment-load-balancer-spike.core :refer :all]))

(facts
  "about the rules"
  (fact
    "splits evenly by number of payments"
    (->
      (add-payments 100)
      process) => (assert (and (= 50 (get @payments :bucket1))
                               (= 50 (get @payments :bucket2))))))
