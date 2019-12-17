(ns kemplittle.client.session
  (:require
   [clojure.data.json :refer [write-str read-json]]
   [kemplittle.config :refer [env]]
   [kemplittle.certs.core :refer [sign base64encode]]
   [taoensso.timbre :as timbre]
   [clj-http.client :as http]
   [buddy.core.nonce :refer [random-nonce]]
   [kemplittle.config :refer [env]]
   [clojure.data.json :as json])
  (:import java.util.UUID))

(defn add-my-server-confs [json confs]
  (merge-with into json confs))

(defn into-data-field [payload]
  (assoc {} :data payload))

(defn get-digest [request]
  (sign request))

(defn get-session []
  (let [sdkid (:client-sdk env)
        my-server-urls
        {:sdk_config {:success_url (:success-url env)
                      :error_url (:error-url env)}
         :notifications {:endpoint (:webhook-url env)
                         :auth_token (str (:webhook-usr env) ":" (:webhook-pw env))}}
        usertracking-uuid (.toString (java.util.UUID/randomUUID))
        base-url "https://api.yoti.com/idverify/v1"
        json-payload (slurp (clojure.java.io/file "resources/request"))
        payload (-> (read-json json-payload)
                    (assoc :user_tracking_id usertracking-uuid)
                    (add-my-server-confs my-server-urls)
                    (dissoc :notifications)
                    (write-str :escape-slash false))
        nonce (.toString (java.util.UUID/randomUUID))
        timestamp (System/currentTimeMillis)
        method "POST"
        uri-path "/sessions"
        q-string (str "sdkId=" sdkid "&nonce=" nonce "&timestamp=" timestamp)
        endpoint (str
                  uri-path "?"
                  q-string)
        request (str method "&" endpoint "&" (base64encode payload))
        options {:url (str base-url endpoint)
                 :method "POST"
                 :body payload
                 :user-agent "User-Agent-string"
                 :headers {"X-Yoti-Auth-Digest" (get-digest request)
                           "Content-Type" "application/json"}}
        http-response (http/request options)]
    #_(timbre/info "endpoint: " endpoint)
    #_(timbre/info "Request for signing: " request)
    #_(timbre/info "to send payload: " payload)
    #_(timbre/info "nonce: " nonce)
    #_(timbre/info "time: " timestamp)
    (-> (:body http-response)
        read-json)))
