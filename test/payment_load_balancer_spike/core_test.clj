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
    (fact
      "with the payments having the same amount"
      (let [history (atom {:bucket1 [] :bucket2 []})
            payments (fn [amount]
                       (map #(-> {:id % :amount 1}) (range amount)))]

        (process-payments history
                          [{:fn (get test-rules :by-amount)}]
                          (payments 10))
        (apply + (map :amount (get @history :bucket1))) => 5
        (apply + (map :amount (get @history :bucket2))) => 5
        ))
    (fact
      "with the payments having different amounts"
      (let [history (atom {:bucket1 [] :bucket2 []})
            payments (fn [amount]
                       (map #(-> {:id % :amount %}) (range amount)))]

        (process-payments history
                          [{:fn (get test-rules :by-amount)}]
                          (payments 10))
        (apply + (map :amount (get @history :bucket1))) => 25
        (apply + (map :amount (get @history :bucket2))) => 20
        ))
    ))

