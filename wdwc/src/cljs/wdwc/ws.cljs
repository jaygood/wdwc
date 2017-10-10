(ns wdwc.ws
  (:require [taoensso.sente :as sente]))

(defonce ws-chan (atom nil))
(def json-reader (t/reader :json))
(def json-writer (t/writer :json))

(let [connection (sente/make-channel-socket! "/ws" {:type :auto})]
     (def ch-chsk (:ch-recv connection))
     (def send-message! (:send-fn connection)))

(defn state-handler [{:keys [?data]}]
      (js/console.log (str "state change: " ?data)))

(defn handshake-handler [{:keys [?data]}]
      (js/console.log (str "connections establs: " ?data)))

(defn default-event-handler [ev-msg]
      (js/console.log (str "unhandleeeeed " (:event ev-msg))))

(defn event-msg-handler [& [{:keys [message state handshake]
                             :or {state state-handler
                                  handshake handshake-handler}}]]
      (fn [ev-msg]
          (case (:id ev-msg)
                :chsk/handshake (handshake ev-msg)
                :chsk/state (state ev-msg)
                :chsk/recv (message ev-msg)
                (default-event-handler ev-msg))))

(def router (atom nil))

(defn stop-router! []
      (when-let [stop-f @router] (stop-f)))
(defn start-router! [message-handler]
      (stop-router!)
      (reset! router (sente/start-chsk-router!
                       ch-chsk
                       (event-msg-handler
                         {:message message-handler
                          :state state-handler
                          :handshake handshake-handler}))))


;(defn connect! [url receive-handler]
;      (reset! ws-chan
;              (js/Object.assign (js/WebSocket. "ws://localhost:3000/ws") #js{"onmessage" js/console.info})))
