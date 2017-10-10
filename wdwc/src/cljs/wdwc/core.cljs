(ns wdwc.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET]]
            [wdwc.ws :as ws]))

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

(defn message-form [fields errors]
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
        [:input.btn.btn-primary {:type :submit :value "comment" :on-click #(ws/send-message! @fields)}]]])


(defn response-handler [messages fields errors]
      (fn [message]
          (if-let [response-errors (:errors message)]
                  (reset! errors response-errors)
                  (do
                    (reset! errors nil)
                    (reset! fields nil)
                    (swap! messages conj message)))))


(defn home []
      (let [messages (atom nil)
            errors (atom nil)
            fields (atom nil)]
           (ws/connect! (str "ws://" (.-host js/location) "/ws")
                        (response-handler messages fields errors))
           (get-messages messages)
           (fn []
               [:div.row
                [:div.span12
                 [message-list messages]]
                [:div.span12
                 [message-form fields errors]]])))



(reagent/render [home]
                (.getElementById js/document "content"))
