(ns kitchen.core
  (:require [datomic.api :as d]))

(def conn nil)

(defn add-pet-owner [owner-name]
  @(d/transact conn [{:owner/name owner-name}]))

(defn find-all-pet-owners []
  (d/q '[:find ?owner-name
         :where [_ :owner/name ?owner-name]]
       (d/db conn)))
