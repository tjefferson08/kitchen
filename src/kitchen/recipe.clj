(ns kitchen.recipe
  (:require [net.cgrand.enlive-html :as html]))

(def url "https://www.allrecipes.com/recipe/165190/spicy-vegan-potato-curry/")

(defn fetch-url [url]
  (-> (html/html-resource (java.net.URL. url))
      (names)))

(defn names [html-resource]
  ;; TODO OR with h2/h3 tags
  (map #(first (:content %1)) (html/select html-resource [[:h1 (html/attr= :itemprop "name")]])))

(defn image-urls [html-resource]
  (html/select html-resource [(html/attr= :itemtype "http://schema.org/Recipe") [:img (html/attr= :itemprop "image")]]))

(defn cook-times [html-resource]
  (html/select html-resource [(html/attr= :itemprop "cookTime")]))

(defn prep-times [html-resource]
  (html/select html-resource [(html/attr= :itemprop "prepTime")]))

(defn total-times [html-resource]
  (html/select html-resource [(html/attr= :itemprop "totalTime")]))

(defn ingredients [html-resource]
  (html/select html-resource #{[(html/attr= :itemprop "recipeIngredient")]
                               [(html/attr= :itemprop "ingredients")]}))
(defn instructions [html-resource]
  (html/select html-resource [(html/attr= :itemprop "recipeInstructions")]))

    ;; %{
    ;;   name: name,
    ;;   image_url: image_url,
    ;;   cook_time: cook_time,
    ;;   prep_time: prep_time,
    ;;   total_time: total_time,
    ;;   ingredients: ingredients,
    ;;   instructions: instructions
    ;; }
