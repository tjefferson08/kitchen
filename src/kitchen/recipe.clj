(ns kitchen.recipe
  (:require [net.cgrand.enlive-html :as html]
            [datomic.api :as d]
            [clojure.spec.alpha :as s]))

(s/def ::name non-empty-string?)
(s/def ::ingredients (s/coll-of non-empty-string? :min-count 1))
(s/def ::instructions (s/coll-of non-empty-string? :min-count 1))
(s/def ::prep-time non-empty-string?)
(s/def ::cook-time non-empty-string?)
(s/def ::total-time non-empty-string?)
(s/def ::image-url non-empty-string?)

(s/def ::recipe
  (s/keys :req [::name ::ingredients ::instructions]
          :opt [::cook-time ::prep-time ::total-time ::image-url]))

(def url "https://www.allrecipes.com/recipe/165190/spicy-vegan-potato-curry/")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn names [html-resource]
  ;; TODO OR with h2/h3 tags
  (map #(first (:content %1)) (html/select html-resource [[:h1 (html/attr= :itemprop "name")]])))

(defn image-urls [html-resource]
  (map
   #(get-in % [:attrs :src])
   (html/select html-resource
                [(html/attr= :itemtype "http://schema.org/Recipe")
                 [:img (html/attr= :itemprop "image")]])))

(defn cook-times [html-resource]
  (map
   #(get-in % [:attrs :datetime])
   (html/select html-resource [(html/attr= :itemprop "cookTime")])))

(defn prep-times [html-resource]
  (map
   #(get-in % [:attrs :datetime])
   (html/select html-resource [(html/attr= :itemprop "prepTime")])))

(defn total-times [html-resource]
  (map
   #(get-in % [:attrs :datetime])
   (html/select html-resource [(html/attr= :itemprop "totalTime")])))

(defn ingredients [html-resource]
  (map
   #(first (:content %))
   (html/select html-resource #{[(html/attr= :itemprop "recipeIngredient")]
                                [(html/attr= :itemprop "ingredients")]})))

(defn blacklisted? [word]
  (#{"watch now"} (clojure.string/lower-case word)))

(defn instructions-old [html-resource]
  (map
   html/text
   (html/select html-resource [(html/attr= :itemprop "recipeInstructions")])))

(defn- text-node->ingredients-vec [text-node]
  (->> text-node
       (clojure.string/split-lines)
       (map clojure.string/trim)
       (filter #(and (not-empty %) (not (blacklisted? %))))))

(defn instructions [html-resource]
  (->> (html/select html-resource [(html/attr= :itemprop "recipeInstructions")])
       (map html/text)
       (map text-node->ingredients-vec)
       (flatten)))

(defn url->recipe [url]
  (let [html-resource (fetch-url url)]
    (->>
     {::name (first (names html-resource))
      ::image-url (first (image-urls html-resource))
      ::cook-time (first (cook-times html-resource))
      ::prep-time (first (prep-times html-resource))
      ::total-time (first (total-times html-resource))
      ::ingredients (ingredients html-resource)
      ::instructions (instructions html-resource)}
     (s/conform ::recipe))))

(defn create-tx-data [url]
  (let [recipe (url->recipe url)]
    [{:db/id (d/tempid :db.part/user)
      :recipe/name (::name recipe)}]))

(defn non-empty-string? [s]
  (and (string? s) (not-empty s)))
