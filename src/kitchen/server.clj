(ns kitchen.server
  (:require [kitchen.commander :as commander]
            [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.content-negotiation :as conneg]))

(defonce database (atom {}))

(def db-interceptor
  {:name :database-interceptor
   :enter (fn [context]
            ;; context.request.database = @database
            (update context :request assoc :database @database))
   :leave (fn [context]
            (if-let [[op & args] (:tx-data context)]
              (do
                ;; if someone has attached tx-data, "run the transaction" here
                ;; and attach the new db value
                (apply swap! database op args)
                (assoc-in context [:request :database] @database))
              context))})

(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])

(def content-neg-intc (conneg/negotiate-content supported-types))

(defn ok [body]
  {:status 200 :body body
   :headers {"Content-Type" "text/html"}})

(defn get-command [request]
  (let [name (get-in request [:query-params :name])]
    (ok (str "Hi, " name))))

(def command-create
  {:name :command-create
   :enter (fn [context]
            (let [command-params (get-in context [:request :query-params :command-params])
                  new-command (commander/create-command command-params)]
              (assoc context
                     :tx-data [assoc (:id new-command) new-command]
                     :response (ok "YAY"))))})


(def routes
  (route/expand-routes
   #{["/commands" :get [content-neg-intc get-command] :route-name :command-index]
     ["/commands" :post [db-interceptor command-create]]}))

(def service-map
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8890})

(defn create-server []
  (http/create-server service-map))

(defonce server (atom nil))

(defn start []
  (http/start (create-server)))

(defn start-dev []
  (reset! server
          (http/start (http/create-server
                       (assoc service-map
                              ::http/join? false)))))

(defn stop-dev []
  (http/stop @server))

(defn restart-dev []
  (stop-dev)
  (start-dev))
