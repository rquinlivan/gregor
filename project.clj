(defproject gregor "0.10.0.1"
  :description "Treat a Kafka consumer stream as a blocking lazy sequence"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.apache.kafka/kafka-clients "0.10.0.1"]
                 [org.apache.kafka/kafka_2.11 "0.10.0.1"]])
