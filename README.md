# gregor

Gregor is a small wrapper for the Kafka consumer designed for use in Clojure projects. My design intention with Gregor is to simplify use of Kafka in Clojure projects by using a `lazy-seq` to wrap the consumer data.

## Versioning

Version numbers are tracked directly the Kafka client version. For example, the `0.10.0.1` release uses the Kafka client with version `0.10.0.1`.

## Usage

### Installing

To install, simply add the following to your `project.clj`, using your target Kafka version for the package version. For example:

```clojure
:dependencies [[gregor "0.10.0.1"]]
```

### Requiring

To use in your Clojure project, import the package and use the public `consumer` method to create a consumer. For example:

```clojure
(ns my.project
  (:require [gregor.core :refer [consumer]]))

(def message-seq
      (consumer
                {:topic    "my_topic"
                 :group    "my_app"
                 :brokers  "localhost:9092"}))
```

Now you can treat the consumer data as any other sequence.

### Future

Sequences in Clojure are a very powerful pattern for stream processing. I intend to expand this library to take more advantage of this pattern.

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
