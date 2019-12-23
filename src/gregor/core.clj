(ns
  ^{:author "Robert Quinlivan"
    :email  "rquinlivan@gmail.com"
    :description "Treat a Kafka consumer stream as a blocking lazy sequence"
    :url "https://github.com/rquinlivan/gregor"}
  gregor.core
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer [chan >!! <!!]])
  (:import (org.apache.kafka.clients.consumer KafkaConsumer ConsumerRecords)
           (java.util Properties ArrayList UUID)
           (kafka.common KafkaException)
           (java.util.regex Pattern)
           (org.apache.kafka.clients.consumer.internals NoOpConsumerRebalanceListener)
           (clojure.lang Keyword)))

(defmacro thread [tname ^Keyword & body]
  "Create a thread with given name appended by a UUID and a fn runnable that executes the body"
  `(Thread. (fn [] ~@body)
            (str (name ~tname) "-" (UUID/randomUUID))))

(defn- create-underlying-consumer
  "Create an instance of the consumer class with the provided group-id"
  [group-id brokers]
  (let [p (doto (new Properties)
            (.put "bootstrap.servers" brokers)
            (.put "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
            (.put "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
            (.put "group.id" group-id)
            (.put "enable.auto.commit" false))]
    (try (new KafkaConsumer p)
         (catch KafkaException e
           (log/error "Couldn't create consumer. Does your broker exist?" e)))))

(defn- commit! [kafka-consumer]
  (.commitAsync kafka-consumer))

(defn- subscribe! [kafka-consumer topic]
  "Mutate the provided kafka-consumer by subscribing on the topic"
  (let [topic-pattern (Pattern/compile topic)]
    (.subscribe kafka-consumer topic-pattern (new NoOpConsumerRebalanceListener))))

(defn- poll-for-records [kafka-consumer]
  "Delegates to the underlying consumer's #poll method"
  (do
    (.poll kafka-consumer 1000)))

(defn- consumer-thread-factory
  "Create a Kafka consumer thread connected to brokers with provided group-id, subscribed to the provided topic.
  As records are consumed, processor-fn will be called."
  [topic group-id processor-fn brokers]
  (let [underlying-consumer (create-underlying-consumer group-id brokers)]
    (subscribe! underlying-consumer topic)
    (.start (thread :consumer-thread
                    (while true
                      (let [messages ^ConsumerRecords (poll-for-records underlying-consumer)]
                        (log/debug "Got back " (.count messages) " messages")
                        (doall (map processor-fn messages)) ;; Force eval with doall. Note this puts all records in memory
                        (commit! underlying-consumer)))))))

(defn- seq!!
  "Returns a lazy sequence read from a channel."
  [c]
  (lazy-seq
    (when-let [v (<!! c)]
      (cons v (seq!! c)))))

(defn consumer
  "Public factory fn to create consumers. Returns the data back as a blocking lazy sequence.
  For example:
  (take 1 (consumer {:topic 'mytopic' :brokers 'mybroker:9092' :group 'mygroup'}))

  The above code will block until 1 record is available.
  "
  ([topic group-id brokers]
   (let [channel          (chan 100)
         put-fn           (fn [message] (>!! channel message))]
     (consumer-thread-factory topic group-id put-fn brokers)
     (seq!! channel)))
  ([config-map]
   (consumer (config-map :topic)
             (config-map :group)
             (config-map :brokers))))
