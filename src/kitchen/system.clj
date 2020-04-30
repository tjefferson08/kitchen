(ns kitchen.system
  (:require [io.pedestal.http :as http]
            [integrant.core :as ig]
            ;; TODO: add this maybe
            ;; https://github.com/weavejester/integrant-repl
            [kitchen.server :as server]
            [kitchen.routes :as routes]))

;; redoing all this with integrant for some reason
(defn config-for [env]
  {::service-map
   {::env env
    ::http/routes routes/routes
    ::http/port 8890
    ::http/join? false
    ::http/type :jetty}
   ::server {::service-map (ig/ref ::service-map)}
   ::kafka-producer {:timeout-ms 2000
                     :servers "kafka:9092"}})
                     ;; :servers (:kafka-servers env)}})

(defmethod ig/init-key ::server [_ {::keys [service-map]}]
  (print "starting server")
  (->> service-map
       (http/create-server)
       (http/start)))

(defmethod ig/init-key ::service-map [_ mp]
  (println "init :service-map")
  mp)

(defmethod ig/init-key ::kafka-producer [_ mp]
  (println "init ::kafka-producer")
  mp)

(defmethod ig/halt-key! ::server [_ srv]
  (println "halting server")
  (http/stop srv))
