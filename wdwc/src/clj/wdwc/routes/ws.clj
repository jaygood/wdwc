(ns wdwc.routes.ws
    (:require [wdwc.layout :as layout]
        [bouncer.core :as b]
        [bouncer.validators :as v]
        [compojure.core :refer [defroutes GET POST]]
        [clojure.java.io :as io]
        [ring.util.response :refer [response status]]
        [wdwc.db.core :as db]
        [immutant.web.async :as async]
        [clojure.tools.logging :as log]
        [cognitect.transit :as transit]
        [taoensso.sente :as sente]
        [taoensso.sente.server-adapters.immutant :refer [sente-web-server-adapter]]))

(defonce channels (atom #{}))

(let [connection (sente/make-channel-socket!
                   sente-web-server-adapter
                   {:user-id-fn (fn [ring-req] (get-in ring-req [:params :client-id]))})]
     (def ring-ajax-post (:ajax-post-fn connection))
     (def ring-ajax-get-or-ws-handshake (:ajax-get-or-ws-handshake-fn connection))
     (def ch-chsk (:ch-recv connection))
     (def chsk-send! (:send-fn connection))
     (def connection-uids (:connected-uids connection)))



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

(defn handle-message! [{:keys [id client-id ?data]}]
      (when (= id :wdwc/add-message)
            (let [response (-> ?data
                               (assoc :timestamp (java.util.Date.))
                               save-the-message!)]
                 (if (:errors response)
                   (chsk-send! client-id [:wdwc/error response])
                   (doseq [uid (:any @connected-uids)]
                          (chsk-send! uid [:wdwc/add-message response]))))))

(defn stop-router! [stop-fn]
      (when stop-fn (stop-fn)))

(defn start-router! []
      (sente/start-chsk-router! ch-chsk handle-message!))

(defstate start-router!
          :start (start-router!)
          :stop (stop-router! router))

(defn ws-handler [request]
      (async/as-channel
          request
          {:on-open connect!
           :on-close disconnect!
           :on-message handle-message!}))

(defroutes websocket-routes
           (GET "/ws" req (ring-ajax-get-or-ws-handshake req))
           (POST "/ws" req (ring-ajax-post req)))
