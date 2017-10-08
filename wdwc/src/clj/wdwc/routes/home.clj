(ns wdwc.routes.home
    (:require [wdwc.layout :as layout]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [compojure.core :refer [defroutes GET POST]]
            [clojure.java.io :as io]
            [ring.util.response :refer [response status]]
            [wdwc.db.core :as db]))


(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn home-page []
  (layout/render
    "home.html"
    {:docs (-> "docs/docs.md" io/resource slurp)
     :messages (db/get-messages)}))



(defn about-page []
  (layout/render "about.html"))

(defn save-the-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (response/bad-request {:errors errors})
    (try
      (db/save-message!
        (assoc params :timestamp (java.util.Date.)))
      (response/ok {:status :ok})
      (catch Exception e (response/internal-server-error
                           {:errors {:server-error ["Failed to save message!"]}})))))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/messages" [] (response/ok (db/get-messages)))
  (POST "/message" request (save-the-message! request))
  (GET "/about" [] (about-page)))

