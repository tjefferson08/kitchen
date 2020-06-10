(ns kitchen.server
  (:require [kitchen.commander :as commander]
            [clojure.data.json :as json]
            [io.pedestal.http :as http]))

(defonce database (atom {}))

(def db-interceptor
  {:name :database-interceptor
   :enter (fn [context]
            ;; context.request.database = @database
            (update context :request assoc ::database @database))
   :leave (fn [context]
            (if-let [[op & args] (::tx-data context)]
              (do
                ;; if someone has attached tx-data, "run the transaction" here
                ;; and attach the new db value
                (apply swap! database op args)
                (assoc-in context [:request ::database] @database))
              context))})

(defn ok [body]
  {:status 200
   :body body
   :headers {"Content-Type" "application/edn"}})

(def command-create
  {:name :command-create
   :enter (fn [context]
            (let [command-params (get-in context [:request :query-params :command-params])
                  new-command (commander/create-command command-params)]
              (println "sup" (get-in context [:request]))
              (assoc context
                     :response (ok {:message "YAY"}))))})
