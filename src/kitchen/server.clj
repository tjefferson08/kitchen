(ns kitchen.server
  (:require [kitchen.commander :as commander]
            [com.stuartsierra.component :as component]
            [clojure.data.json :as json]
            [io.pedestal.http :as http]))

(defn test?
  [service-map]
  (= :test (:env service-map)))

(defrecord Server [service-map service]
  component/Lifecycle
  (start [this]
    (print "starting server")
    (if service
      this
      (cond-> service-map
        true http/create-server
        (not (test? service-map)) http/start
        true ((partial assoc this :service)))))

  (stop [this]
    (print "stopping server")
    (when (and service (not (test? service-map)))
      (http/stop service))
    (assoc this :service nil)))

(defn new-server []
  (map->Server {}))

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
  {:status 200 :body body
   :headers {"Content-Type" "text/html"}})

(def command-create
  {:name :command-create
   :enter (fn [context]
            (let [command-params (get-in context [:request :query-params :command-params])
                  new-command (commander/create-command command-params)]
              (assoc context
                     ::tx-data [assoc (:id new-command) new-command]
                     :response (ok "YAY"))))})
