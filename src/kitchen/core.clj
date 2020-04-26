(ns kitchen.core
  (:require [datomic.api :as d]
            [kitchen.recipe :as recipe]))

(defn bootstrap-db [uri]
  (d/delete-database uri)
  (d/create-database uri)
  (let [conn (d/connect uri)
        schema (load-file "resources/datomic/schema.edn")]
    (d/transact conn schema)
    conn))

(def db-uri "datomic:free://localhost:4334/kitchen-db")

(def conn
  (d/connect db-uri))

(defn add-recipe-by-url [url]
  @(d/transact conn (recipe/create-tx-data url)))

(defn find-all-recipes []
  (d/q '[:find [(pull ?rid [:recipe/name {:recipe/ingredients [:ingredient/text]}]) ...]
         :where
         [?rid :recipe/name ?recipe-name]]
       (d/db conn)))
