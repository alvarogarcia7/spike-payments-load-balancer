(ns payment-load-balancer-spike.multithread-test
  (:require
    [payment-load-balancer-spike.core-test :refer :all]))


(defn run [nthreads niters]
  (let [history (atom {:bucket2 []})
        swap #(process-payments history
                          [{:fn (get test-rules :only-5-in-bucket2)}]
                          (generate-payments 10))]
    (dorun (apply pcalls (repeat nthreads #(dotimes [_ niters] (swap)))))
    @history
    ))
