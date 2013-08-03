(ns powergrid-client.websocket
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.log :as log]
            [cljs.reader :refer [read-string]]
            [cljs.core.async :as async :refer [<! >! chan close! put! take! sliding-buffer dropping-buffer timeout]])
  (:require-macros [cljs.core.async.macros :as m :refer [go]]))

(defn websocket
  [uri config]
  (let [ws (js/WebSocket. uri)]
    (doseq [[prop value] config]
      (aset ws (name prop) value))
    ws))

(defn websocket-chans
  [uri]
  (let [msg-c (chan)
        err-c (chan)
        send-c (chan)
        open-c (chan 1)
        close-c (chan)
        onopen (fn []
                 (log/debug :ws-open uri)
                 (go (while true (>! open-c :open))))
        onclose (fn []
                  (log/debug :ws-close uri)
                  ;; TODO cleanup, close channels
                  )
        onmessage (fn [m]
                    (let [data (.-data m)]
                      ;(log/debug :ws-message data)
                      (go (>! msg-c (read-string data)))))
        onerror (fn [e]
                  (log/error :ws-error e)
                  (go (>! err-c e)))
        ws (websocket uri {:onopen onopen
                           :onclose onclose
                           :onmessage onmessage
                           :onerror onerror})]
    ;; send channel
    (go
      (<! open-c)
      (while true
          (let [data (<! send-c)]
            (log/debug :ws-send data)
            (.send ws (pr-str data)))))
    ;; close channel
    (go (<! close-c) (.close ws))

    {:in msg-c
     :out send-c
     :open open-c
     :close close-c
     :error err-c}))
