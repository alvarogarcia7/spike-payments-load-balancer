(ns payment-load-balancer-spike.multithread-test
  (:use [midje.sweet])
  (:require
    [payment-load-balancer-spike.core-test :refer :all]
    [payment-load-balancer-spike.core :refer :all]))


(defn proces-payments-in-parallel [nthreads niters history]
  (let [process-payment #(process-payments history
                          [{:fn (get test-rules :only-5-in-bucket2)}]
                          (generate-payments 10))]
    (dorun (apply pcalls (repeat nthreads #(dotimes [_ niters] (process-payment)))))
    @history
    ))

(facts
  "processing the payments in parallel"
  (fact
    "the code supports concurrency"
    (dotimes [_ 10]
      (let [history (atom {})]
        (proces-payments-in-parallel 10 100 history)
        (count (get @history :bucket2)) => 5))
    )
  )
