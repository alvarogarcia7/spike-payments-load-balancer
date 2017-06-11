(defproject payment-load-balancer-spike "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name ""
            :url ""}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot payment-load-balancer-spike.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {
                   :dependencies [[midje "1.6.3"]]
                   :plugins      [[lein-midje "3.1.3"]
                                  [com.jakemccrary/lein-test-refresh "0.6.0"]]}})
