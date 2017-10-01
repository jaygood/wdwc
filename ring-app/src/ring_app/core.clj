(ns ring-app.core
    (:require [ring.adapter.jetty :as jetty]
      [ring.util.http-response :as response]
      [compojure.core :as compojure]
      [ring.middleware.reload :refer [wrap-reload]]
      [ring.middleware.format :refer [wrap-restful-format]]))

(defn home [request-map]
  (str "<html><body> your IP, mofo: " (:remote-addr request-map) "</body></html>"))

(defn id-route [request-map id] id)

(defn api [request-map]
      (let [uri (:uri request-map)] (println uri)
           (cond
             (clojure.string/starts-with? uri "/error") (response/internal-server-error "you suck")
             (clojure.string/starts-with? uri "/api/") (response/ok {:result (:params request-map) :success true})
             :else (response/ok
                     (case uri
                           "/" (str "<html><body> your IP, mofo: " (:remote-addr request-map) "</body></html>")
                           "/favicon.ico" "screw you"
                           "/error" "screw you"
                           "nothin to see here")))))

(def handler
  (compojure/routes
    (compojure/GET "/" request home)
    (compojure/GET "/id/:id" [id] (str "id: " id))
    (compojure/ANY "/api/" request api)))

;(compojure/defroutes handler
;  (compojure/GET "/" request handler)
;  (compojure/GET "/:id" [id] (str "yo " id))
;  (compojure/POST "/json" [id] (response/ok {:result id})))

(defn display-profile [id] (str "user profile " id))
(defn display-settings [id request] (str "user settings " id  " - " (:params request)))

(compojure/defroutes user-routes
  (compojure/context "/user/:id" [id]
    (compojure/GET "/profile" [] (do (println "something" id) (display-profile id)))
    (compojure/GET "/settings/:hello" request (display-settings id request))))
    ;(compojure/GET "/settings/:hello" [hello] (display-settings id hello))))



(defn wrap-formats [hand]
      (wrap-restful-format
        hand
        :formats [:json-kw] :response-options {:json-kw {:pretty true}}))


(defn middle [hand]
      (fn [request]
          (-> request
              hand
              (assoc-in [:headers "Pragma"] "no-cache"))))

(defn -main []
      (jetty/run-jetty
        (-> #'user-routes middle wrap-reload wrap-formats)
        {:port 3000 :join? false}))

