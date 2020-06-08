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

(comment
  (def example-url "https://www.allrecipes.com/recipe/165190/spicy-vegan-potato-curry/"))

;; Updates come through dedicated channel for actions
(def action-ch (chan))
(def ^:const ^:private COMMANDS_URL "http://localhost:8890/commands")

(defn reducer [state action]
  (case (:type action)
    :import     (assoc state :loading true)
    :load       (assoc state :loading false :data (:payload action) :error nil)
    :error      (assoc state :error (:message action) :data nil)
    :update-url (assoc state :url (:value action))
    state))

;; Consume "action" events
(go
  (while true
    (let [action (<! action-ch)
          next (reducer @state action)]
      (js/console.log "action" (:type action))
      (js/console.log "next" next)
      (reset! state next))))

;; HTTP stuff with fetch and core.async
(defn post-config [body]
  (let [enc-body (js/JSON.stringify (clj->js body))
        config   {:method "POST" :headers {"Content-Type" "application/json"}}]
    (clj->js (merge config {:body enc-body}))))

;; TODO use builtin encoding other than JSON
(defn POST [url body]
  (go
    (alts! [(timeout 5000)
            (go (<p! (js/fetch url (post-config body))))])))

;; Basically a thunk?
(defn dispatch-fetch [ch url]
  (go
    (>! ch {:type :import})
    (let [[res _] (<! (POST COMMANDS_URL {:url url}))]
      (js/console.log res _)
      (cond
        (nil? res) (>! ch {:type :error, :message "Timeout"})
        (.-ok res) (let [json (<p! (.json res))
                         payload (js->clj json)]
                     (>! ch {:type :load, :payload payload}))
        :else (>! ch {:type :error, :message "Request error"})))))

(defn app []
  (let [on-click #(dispatch-fetch action-ch (:url @state))]
    [:div
     [:div (str "State: " @state)]
     [:input {:type "text"
              :value (:url @state)
              :on-change #(let [val (.-value (.-target %))] (go (>! action-ch {:type :update-url, :value val})))}]
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
