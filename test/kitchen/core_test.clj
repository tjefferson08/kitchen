(ns kitchen.core-test
  (:use [clojure.test])
  (:require [kitchen.core :refer :all]
            [datomic.api :as d]))

(defn create-test-db []
  (let [uri "datomic:mem://test-db"]
    (d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/datomic/schema.edn")]
      (d/transact conn schema)
      conn)))

(deftest a-test
  (is (= #{["John"]}
         (do
           (with-redefs [conn (create-test-db)]
             (add-pet-owner "John")
             (find-all-pet-owners))))))