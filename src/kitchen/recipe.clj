(ns kitchen.recipe
  (:require [net.cgrand.enlive-html :as html]))

(def ^:dynamic *base-url* "https://news.ycombinator.com/")
(def url "https://www.allrecipes.com/recipe/165190/spicy-vegan-potato-curry/")

(defn fetch-url [url]
  (-> (html/html-resource (java.net.URL. url))
      (html/select [:div])))
