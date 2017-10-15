(ns swagger-service.core
  (:require [reagent.core :as r :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [swagger-service.ajax :refer [load-interceptors!]]
            [ajax.core :refer [GET POST]]
            [swagger-service.pager :refer [pager]])
  (:import goog.History))

(defn fetch-links! [links link-count]
      (GET "/api/cat-links"
           {:params {:link-count link-count}
            :handler #(do (reset! links (vec (partition-all 9 %))))}))

(defn image [link]
      [:div.col-sm-4 [:img {:width 400 :src link}]])

(defn images [links]
      [:div.text.xs-center
       (for [row (partition-all 3 links)]
            ^{:key row}
            [:div.row
             (for [link row]
                  ^{:key link}
                  [image link])])])

(defn home-page []
      (let [page (atom 0)
            links (atom nil)]

           (fetch-links! links 40)
           (fn []
               (if (not-empty @links)
                 [:div.container>div.row>div.col-md-12
                  [images (@links @page)]
                  [pager (count @links) page]]
                 [:div "Standy by"]))))

(defn mount-components []
      (r/render-component [home-page] (js/document.getElementById "app")))

(defn init! []
      (mount-components))

;(defn nav-link [uri title page collapsed?]
;  [:li.nav-item
;   {:class (when (= page (session/get :page)) "active")}
;   [:a.nav-link
;    {:href uri
;     :on-click #(reset! collapsed? true)} title]])
;
;(defn navbar []
;  (let [collapsed? (r/atom true)]
;    (fn []
;      [:nav.navbar.navbar-light.bg-faded
;       [:button.navbar-toggler.hidden-sm-up
;        {:on-click #(swap! collapsed? not)} "☰"]
;       [:div.collapse.navbar-toggleable-xs
;        (when-not @collapsed? {:class "in"})
;        [:a.navbar-brand {:href "#/"} "swagger-service"]
;        [:ul.nav.navbar-nav
;         [nav-link "#/" "Home" :home collapsed?]
;         [nav-link "#/about" "About" :about collapsed?]]]])))
;
;(defn about-page []
;  [:div.container
;   [:div.row
;    [:div.col-md-12
;     "this is the story of swagger-service... work in progress"]]])
;
;(defn home-page []
;  [:div
;   [:h2 "Welcome to ClojureScript"]
;   [:p "live codin"]])
;
;
;(def pages
;  {:home #'home-page
;   :about #'about-page})
;
;(defn page []
;  [(pages (session/get :page))])
;
;;; -------------------------
;;; Routes
;(secretary/set-config! :prefix "#")
;
;(secretary/defroute "/" []
;  (session/put! :page :home))
;
;(secretary/defroute "/about" []
;  (session/put! :page :about))
;
;;; -------------------------
;;; History
;;; must be called after routes have been defined
;(defn hook-browser-navigation! []
;  (doto (History.)
;        (events/listen
;          HistoryEventType/NAVIGATE
;          (fn [event]
;              (secretary/dispatch! (.-token event))))
;        (.setEnabled true)))
;
;;; -------------------------
;;; Initialize app
;(defn fetch-docs! []
;  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))
;
;(defn mount-components []
;  (r/render [#'navbar] (.getElementById js/document "navbar"))
;  (r/render [#'page] (.getElementById js/document "app")))
;
;(defn init! []
;  (load-interceptors!)
;  (fetch-docs!)
;  (hook-browser-navigation!)
;  (mount-components))
