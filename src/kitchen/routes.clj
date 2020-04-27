(ns kitchen.routes
  (:require [io.pedestal.http.route :as route]
            [kitchen.server :as server]))

(def routes
  (route/expand-routes
   #{["/commands" :post [server/db-interceptor server/command-create]]}))
