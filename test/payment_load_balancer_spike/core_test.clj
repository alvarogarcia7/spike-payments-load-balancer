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
  {:only-5-in-bucket2 (fn [history] (if (>= (count (get history :bucket2)) 5) :bucket1 :bucket2))
   :by-amount (fn [history]
                (let [field :sum
                      decreasing-by-size #(> (field %2) (field %1))
                      key-and-amount (fn [history]
                        (map (fn [[k v]] {:key k field (apply + (map #(get % :amount 0) v))}) history))]
                  (->>
                    history
                    key-and-amount
                    (sort decreasing-by-size)
                    first
                    :key)))
   }
  )

(defn
  sum-amounts
  [history]
  (reduce merge (map (fn a [[k v]] {k (apply + (map :amount v))}) history)))


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
        (let [amounts (sum-amounts @history)]
          (get amounts :bucket1) => 5
          (get amounts :bucket2) => 5
          )))
    (fact
      "with the payments having different amounts"
      (let [history (atom {:bucket1 [] :bucket2 []})
            payments (fn [amount]
                       (map #(-> {:id % :amount %}) (range amount)))]

        (process-payments history
                          [{:fn (get test-rules :by-amount)}]
                          (payments 10))
        (let [amounts (sum-amounts @history)]
          (get amounts :bucket1) => 25
          (get amounts :bucket2) => 20
          )))
    )
  (fact
    "splitting by percentages in two buckets, by bucket amount"
    (fact
      "two buckets"
      (let [history (atom {:bucket1 [] :bucket2 []})
            payments (fn [amount]
                       (map #(-> {:id % :amount 1}) (range amount)))]

        (process-payments history
                          [{:fn (fn [history]
                                  (let [objective {:bucket1 0.60 :bucket2 0.40}
                                        amounts (sum-amounts history)
                                        total-payments (reduce (fn [acc [k v]] (+ acc v)) 1 amounts)
                                        percentages (reduce merge (map (fn [[k v]] {k (/ v total-payments)}) amounts))
                                        differences (reduce merge (map (fn [[[b1 v1] [b2 v2]]] {b1 (Math/abs (- v1 v2))}) (map #(-> [%1 %2]) (sort percentages) (sort objective))))
                                        bucket-most-different (key (first (reduce (fn [[k-acc v-acc] [k-ele v-ele]] (if (> v-ele v-acc) {k-ele v-ele} {k-acc v-acc})) differences)))]
                                    bucket-most-different))}]
                          (payments 10))
        (let [amounts (sum-amounts @history)]
          (get amounts :bucket1) => 6
          (get amounts :bucket2) => 4
          )))

    ))

