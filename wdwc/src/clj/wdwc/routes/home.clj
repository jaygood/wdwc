(ns wdwc.routes.home
    (:require [wdwc.layout :as layout]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [compojure.core :refer [defroutes GET POST]]
            [clojure.java.io :as io]
            [ring.util.http-response :as response]
            [ring.util.response :refer [status]]
            [wdwc.db.core :as db]))

(defn home-page []
  (layout/render
    "home.html"
    {:docs (-> "docs/docs.md" io/resource slurp)
     :messages (db/get-messages)}))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/messages" [] (response/ok (db/get-messages)))
  (GET "/about" [] (about-page)))
