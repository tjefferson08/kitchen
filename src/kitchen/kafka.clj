(ns kitchen.kafka
  (:require [clojure.core.async :as a]
            [clojure.data.fressian :as fressian])
  (:import [org.apache.kafka.clients.producer KafkaProducer ProducerRecord]))


(deftype FressianSerializer []
  Serializer
  (close [_])
  (configure [_ _ _])
  (serialize [_ _ data]
    (util/buf->bytes (fressian/write data :footer? true))))

(deftype FressianDeserializer []
  Deserializer
  (close [_])
  (configure [_ _ _])
  (deserialize [_ _ data]
    (fressian/read data)))

(defn construct-producer
  "Constructs and returns a Producer according to config map (See
  https://kafka.apache.org/documentation.html#producerconfigs for
  details)."
  [producer-config]
  (let [{:keys [servers timeout-ms client-id config]
         :or {config {}
              client-id "commander-rest-producer"
              key-serializer   (FressianSerializer.)
              value-serializer (FressianSerializer.)}}
        producer-config]
    (#(KafkaProducer. ^java.util.Map
              (assoc config
                     "request.timeout.ms" (str timeout-ms)
                     "bootstrap.servers" servers
                     "client.id" client-id
                     "compression.type" "gzip"
                     "acks" "all")))))
