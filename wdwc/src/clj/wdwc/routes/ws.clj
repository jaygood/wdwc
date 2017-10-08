(ns wdwc.routes.ws
    (:require [wdwc.layout :as layout]
        [bouncer.core :as b]
        [bouncer.validators :as v]
        [compojure.core :refer [defroutes GET POST]]
        [clojure.java.io :as io]
        [ring.util.response :refer [response status]]
        [wdwc.db.core :as db]
        [immutant.web.async :as async]
        [cognitect.transit :as transit]))

(defonce channels (atom #{}))

(defn encode-transit [message]
      (let [out (java.io.ByteArrayOutputStream. 4096)
            writer (transit/writer out :json)]
           (transit/write writer message)
           (.toString out)))
(defn decode-transit [message]
      (let [in (java.io.ByteArrayInputStream. (.getBytes message))
            reader (transit/reader in :json)]
           (transit/read reader)))

(defn validate-message [params]
      (first
          (b/validate
              params
              :name v/required
              :message [v/required [v/min-count 10]])))

(defn save-the-message! [message]
      (if-let [errors (validate-message message)]
              {:errors errors}
              (do
                  (db/save-message! message)
                  message)))

(defn connect! [channel]
      (log/info "channel open, bitch")
      (swap! channels conj channel))

(defn disconnect! [channel {:keys [code reason]}]
      (log/info "close code: " code " resason: " reason)
      (swap! channels clojure.set/difference #{channel}))

(defn handle-message! [channel message]
      (let [response (-> message
                         decode-transit
                         (assoc :timestamp (java.util.Date.))
                         save-message!)]
           (let [the-fun (fn [] (async/send! channel (encode-transit response)))]
                (if (:errors response)
                    (the-fun)
                    (doseq [channel @channels] (the-fun))))))

(defn ws-handler [request]
      (async/as-channel
          request
          {:on-open connect!
           :on-close disconnect!
           :on-message handle-message!}))

(defroutes websocket-routes
           (GET "/ws" [] ws-handler))