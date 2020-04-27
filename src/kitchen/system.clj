(ns system
  (:require [com.stuartsierra.component :as component]
            ;; TODO: move to dev namespace
            ;; [com.stuartsierra.component.repl :refer [reset set-init start stop system]]
            [io.pedestal.http :as http]
            [kitchen.server :as server]
            [kitchen.routes :as routes]))

(defn new-system [env]
  (component/system-map
   :service-map {:env env
                 ::http/routes routes/routes
                 ::http/port 8890
                 ::http/join? false
                 ::http/type :jetty}
   :server (component/using
            (server/new-server)
            [:service-map])))
