(ns kemplittle.client.session
  (:require
   [clojure.data.json :refer [write-str read-json]]
   [environ.core :refer [env]]
   [kemplittle.db.core :refer [max-id users]]
   [kemplittle.certs.core :refer [sign base64encode]]
   [kemplittle.mail :as mail]
   [taoensso.timbre :as timbre]
   [clj-http.client :as http]
   [buddy.core.nonce :refer [random-nonce]]
   [clojure.data.json :as json]
   )
  (:import java.util.UUID))

(defn add-my-server-confs [json confs]
  (merge-with into json confs))

(defn into-data-field [payload]
  (assoc {} :data payload))

(defn get-digest [request]
  (sign request))

(defn get-new-session []
  (let [sdkid (:docscan-sdk-id env)
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
    ; (timbre/info "endpoint: " endpoint)
    ; (timbre/info "Request for signing: " request)
    ; (timbre/info "to send payload: " payload)
    ; (timbre/info "nonce: " nonce)
    ; (timbre/info "time: " timestamp)
    (-> (:body http-response)
        read-json)))

(defn session-details [{:keys [is-media? media-id]} session]
  (let [sdkid (:docscan-sdk-id env)
        usertracking-uuid (.toString (java.util.UUID/randomUUID))
        base-url "https://api.yoti.com/idverify/v1"
        nonce (.toString (java.util.UUID/randomUUID))
        timestamp (System/currentTimeMillis)
        method "GET"
        uri-path (str "/sessions" "/" session)
        uri-path (if is-media? (str uri-path "/media/" media-id "/content") uri-path)
        q-string (str "sdkId=" sdkid "&nonce=" nonce "&timestamp=" timestamp)
        endpoint (str
                  uri-path "?"
                  q-string)
        request (str method "&" endpoint)
        options {:url (str base-url endpoint)
                 :method method
                 :user-agent "User-Agent-string"
                 :headers {"X-Yoti-Auth-Digest" (get-digest request)}}
        req (try (http/request options)
                 (catch Exception e (str "Error: " e)))
        parse-resp (fn [maybe-body]
                     (try (read-json maybe-body)
                          (catch Exception e maybe-body)))]
    (timbre/info "endpoint: " endpoint)
    (timbre/info "Request for signing: " request)
    ; (timbre/info "to send payload: " payload)
    ; (timbre/info "nonce: " nonce)
    ; (timbre/info "time: " timestamp)
    (some-> req
            (get :body req)
            parse-resp)))

(defn with-media [sid mid]
  (session-details {:is-media? true
                    :media-id mid}
                   sid))