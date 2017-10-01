(ns wdwc.routes.home
  (:require [wdwc.layout :as layout]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [wdwc.db.core :as db]))

(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn home-page [{:keys [flash]}]
  (layout/render
    "home.html"
    (merge {:docs (-> "docs/docs.md" io/resource slurp)
            :messages (db/get-messages)}
           (select-keys flash [:name :message :errors]))))


(defn about-page []
  (layout/render "about.html"))

(defn save-the-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> (response/found "/")
        (assoc :flash (assoc params :errors errors)))
    (do
      (db/save-message!
        (assoc params :timestamp (java.util.Date.)))
      (response/found "/"))))

(defroutes home-routes
  (GET "/" request (home-page request))
  (POST "/message" request (save-the-message! request))
  (GET "/about" [] (about-page)))

