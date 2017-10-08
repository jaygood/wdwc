(ns wdwc.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST]]))


(defn submit-form! [fields errors messages]
      (POST "/message"
            {:params @fields
             :format :json
             :headers {"Accept" "application/transit+json"
                       "x-csrf-token" (.-value (.getElementById js/document "token"))}
             :handler #(do
                         (.log js/console (str "response: " %))
                         (swap! messages conj (assoc @fields :timestamp (js/Date.)))
                         (reset! errors nil))
             :error-handler #(do
                               (js/console.error (str "error: " %))
                               (reset! errors (get-in % [:response :errors])))}))

(defn get-messages [messages]
      (GET "/messages"
           {:headers {"Accept" "application/transit+json"}
            :handler #(reset! messages (vec %))}))


(defn message-list [messages]
      [:ul.content
       (for [{:keys [timestamp message name]} @messages]
            ^{:key timestamp}
            [:li message " - " name])])

(defn errors-component [errors id]
      (when-let [error (id @errors)]
                [:div.alert.alert-danger (clojure.string/join error)]))

(defn message-form [messages]
      (let [fields (atom {}) errors (atom nil)]
           (fn []
               [:div.content
                [:div.form-group
                 [errors-component errors :name]
                 [:p "name " (:name @fields)]
                 [:p "message " (:message @fields)]
                 [:p "NAme: "
                  [:input.form-control
                   {:type :text
                    :name :name
                    :on-change #(swap! fields assoc :name (-> % .-target .-value))
                    :value (:name @fields)}]]
                 [errors-component errors :message]
                 [:p "Message: "
                  [:textarea.form-control
                   {:rows 4
                    :cols 50
                    :name :message
                    :value (:message @fields)
                    :on-change #(swap! fields assoc :message (-> % .-target .-value))}]]
                 [:input.btn.btn-primary {:type :submit :value "comment" :on-click #(submit-form! fields errors messages)}]]])))


(defn home []
      (let [messages (atom nil)]
           (get-messages messages)
           (fn []
               [:div.row
                [:div.span12
                 [message-list messages]]
                [:div.span12
                 [message-form messages]]])))



(reagent/render [home]
                (.getElementById js/document "content"))
