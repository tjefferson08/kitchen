(ns kitchen.system-test
  (:use [clojure.test])
  (:require [kitchen.system :as system]
            [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]))

(defn build-system [] (system/new-system :test))

(defn service-fn [system]
  (get-in system [:server :service ::http/service-fn]))

(deftest test-create-command
  (let [test-sys (build-system)
        started-sys (component/start test-sys)]
    (is (= 200
           (:status (response-for
                     (service-fn started-sys)
                     :post "/commands"))))
    (component/stop test-sys)))
