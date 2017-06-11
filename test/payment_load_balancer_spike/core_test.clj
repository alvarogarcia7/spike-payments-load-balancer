(ns payment-load-balancer-spike.core-test
  (:use [midje.sweet])
  (:require
    [payment-load-balancer-spike.core :refer :all]))


(defn
  generate-payments
  [number]
  (map #(-> {:id %}) (range number)))

(defn
  generate-payments-with-fixed-amount
  [number]
  (map #(-> {:id % :amount 1}) (range number)))

(defn
  process-payments
  [repository rules payments]
  (doall (map #(process repository rules %) payments)))

(defn
  key-and-amount
  [history]
  (map (fn [[k v]] {:key k :sum (apply + (map #(get % :amount 0) v))}) history))

(def
  test-rules
  {:only-5-in-bucket2 (fn [history] (if (>= (count (get history :bucket2)) 5) :bucket1 :bucket2))
   :by-amount (fn [history]
                (let [decreasing-by-size #(> (:sum %2) (:sum %1))]
                  (->>
                    history
                    key-and-amount
                    (sort decreasing-by-size)
                    first
                    :key)))
   }
  )


(facts
  "about the rules"
  (fact
    "splitting evenly in two buckets, based on payments"
    (fact
      "when both buckets are defined in advance"
      (let [history (atom {:bucket2 [] :bucket1 []})]
        (process-payments history
                          [{:fn (get rules :smallest-bucket)}]
                          (generate-payments 100))
        (count (get @history :bucket1)) => 50
        (count (get @history :bucket2)) => 50
        )
      )
    (fact
      "when one of the buckets is not defined in advance"
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
    )
  (fact
    "splitting evenly in two buckets, by bucket amount"
    (let [history (atom {:bucket1 [] :bucket2 []})]

      (process-payments history
                        [{:fn (get test-rules :by-amount)}]
                        (generate-payments-with-fixed-amount 10))
      (println @history)
      (apply + (map :amount (get @history :bucket1))) => 5
      (apply + (map :amount (get @history :bucket2))) => 5
      )
    ))



