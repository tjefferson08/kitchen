(ns kitchen.core
  (:require
   [cljs.core.async :refer [go chan <! >! alts! timeout]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [reagent.dom :as rdom]
   [reagent.core :as r]))

(enable-console-print!)

(println "This text is printed from src/kitchen/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce state (r/atom {}))

;; Updates come through dedicated channel for actions
(def action-ch (chan))

(defn reducer [state action]
  (case (:type action)
    :import (assoc state :loading true)
    :load   (assoc state :loading false :data (:payload action))
    state))

;; Consume "action" events
(go
  (while true
    (let [action (<! action-ch)
          next (reducer @state action)]
      (js/console.log "action" (:type action))
      (js/console.log "next" next)
      (reset! state next))))

(defn fetch [url]
  (go
    (alts! [(timeout 5000)
            (go (let [p    (<p! (js/fetch url))
                      json (<p! (.json p))]
                  json))])))

(defn dispatch-fetch [ch url]
  (go
    (>! ch {:type :import})
    (let [[res _] (<! (fetch url))]
      (if res
        (>! ch {:type :load, :payload res})
        (>! ch {:type :error})))))

(defn app []
  (let [action {:type :import,
                ;; :url "https://www.allrecipes.com/recipe/165190/spicy-vegan-potato-curry/"}
                :url  "https://jsonplaceholder.typicode.com/todos/1"}
        on-click #(dispatch-fetch action-ch (:url action))]
    [:div
     [:div (str "State: " @state)]
     [:input {:type "text"}]
     [:button {:type "button" :on-click on-click}
      "Import URL"]]))

(defn mount-root []
  (rdom/render [app]
               (.getElementById js/document "app")))

(mount-root)

(defn on-js-reload []
;; optionally touch your app-state to force rerendering depending on
;; your application
;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
