(ns kitchen.system-test
  (:require [kitchen.system :as system]
            [clojure.test :refer [deftest is]]
            [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]))

(defn service-fn [system]
  (get-in system [::system/server ::http/service-fn]))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (ig/init ~binding-expr)]
     (try
       ~@body
       (finally
         (ig/halt! ~bound-var)))))

(deftest test-create-command
  (with-system [test-sys (system/config-for :test)]
    (print test-sys)
    (is (= 200
           (:status (response-for
                     (service-fn test-sys)
                     :post "/commands"))))))
