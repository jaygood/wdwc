(ns html-templating.core
 (:require [selmer.parser :as selmer]
           [selmer.filters :as filters]
           [selmer.middleware :refer [wrap-error-page]]))

(filters/add-filter! :empty? empty?)

(filters/add-filter! :foo
                     (fn [x] [:safe (.toUpperCase x)]))

(println (selmer/render "{% if files|empty? %}no files{% else %}files{% endif %}" {:files []}))
(println (selmer/render "{{x}}" {:x "<div>hello, am I safe?</div>"}))

(selmer/add-tag!
  :image
  (fn [args context-map]
      (str "<img src=" (first args) "/>")))

(println (selmer/render "{% image \"html://foo.com/logo.jpg\" %}" {}))

(selmer/add-tag!
  :uppercase
  (fn [args context-map content]
      (.toUpperCase (get-in content [:uppercase :content])))
  :enduppercase)

(println (selmer/render "{% uppercase %}foo {{bar}} baz {% enduppercase %}" {:bar "inject"}))

(println "alright")


; page 46
(defn renderer [] (wrap-error-page #({:status 200 :body (selmer/render-file % {})})))
((renderer) "html.html")