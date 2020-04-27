(ns kitchen.system-test
  (:use [clojure.test])
  (:require [kitchen.system :as system]
            [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]))

(defn build-system [] (system/new-system :test))

(defn service-fn [system]
  (get-in system [:server :service ::http/service-fn]))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(deftest test-create-command
  (with-system [test-sys (build-system)]
    (is (= 200
           (:status (response-for
                     (service-fn test-sys)
                     :post "/commands"))))))
