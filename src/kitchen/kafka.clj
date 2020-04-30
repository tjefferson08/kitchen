(ns kitchen.kafka
  (:require [clojure.core.async :as a]
            [clojure.data.fressian :as fressian])
  (:import [org.apache.kafka.clients.producer KafkaProducer ProducerRecord Callback RecordMetadata]
           [org.apache.kafka.common.serialization Deserializer Serializer]
           [java.nio ByteBuffer]))



;; HT: https://github.com/ztellman/byte-streams
(defn buf->bytes
  [^ByteBuffer buf]
  (if (.hasArray buf)
    (if (== (alength (.array buf)) (.remaining buf))
      (.array buf)
      (let [ary (byte-array (.remaining buf))]
        (doto buf
          .mark
          (.get ary 0 (.remaining buf))
          .reset)
        ary))
    (let [^bytes ary (byte-array (.remaining buf))]
      (doto buf .mark (.get ary) .reset)
      ary)))

(deftype FressianSerializer []
  Serializer
  (close [_])
  (configure [_ _ _])
  (serialize [_ _ data]
    (buf->bytes (fressian/write data :footer? true))))

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
  (let [{:keys [servers timeout-ms client-id config key-serializer value-serializer]
         :or {config {}
              client-id "commander-rest-producer"
              key-serializer   (FressianSerializer.)
              value-serializer (FressianSerializer.)}}
        producer-config]
    (#(KafkaProducer.
       ^java.util.Map
       (assoc config
         "request.timeout.ms" (str timeout-ms)
         "bootstrap.servers" servers
         "client.id" client-id
         "compression.type" "gzip"
         "acks" "all")
       ^Serializer key-serializer
       ^Serializer value-serializer))))


(comment
  (def c (kitchen.system/config-for :dev))
  (def p (construct-producer (:kitchen.system/kafka-producer c)))
  (.send p
         (ProducerRecord. "commands" "sup")
         (reify
           Callback
           (^void onCompletion [_ ^RecordMetadata rm ^Exception e]
             (println "complete " rm e))))
  (println "sup"))
