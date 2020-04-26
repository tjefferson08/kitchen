(ns kitchen.commander
  (:require [datomic.api :as d]
            [kitchen.recipe :as recipe]
            [clj-uuid :as uuid]))


;; TODO kafka topic
(defn- record-for-command
  [id command]
  {:key   id
   :value command})

(defn create-command [cmd-params]
  (let [id (uuid/v1)
        record (record-for-command id cmd-params)]
        ;; TODO write to log
    (assoc cmd-params
           :id id)))
