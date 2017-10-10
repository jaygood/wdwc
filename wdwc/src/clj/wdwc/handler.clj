(ns wdwc.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [wdwc.layout :refer [error-page]]
            [wdwc.routes.home :refer [home-routes]]
            [wdwc.routes.ws :refer [websocket-routes]]
            [compojure.route :as route]
            [wdwc.env :refer [defaults]]
            [mount.core :as mount]
            [wdwc.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    #'websocket-routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(defn app [] (middleware/wrap-base #'app-routes))
