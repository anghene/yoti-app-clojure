(ns kemplittle.client.session
  (:require
   [clj-http.client :as http]
   [clojure.data.json :refer [write-str read-json]]
   [environ.core :refer [env]]
   [kemplittle.db.core :refer [max-id users]]
   [kemplittle.certs.core :refer [sign base64encode]]
   [kemplittle.mail :as mail]
   [taoensso.timbre :as timbre]
   [buddy.core.nonce :refer [random-nonce]]
   )
  (:gen-class)
  (:import java.util.UUID))

(defn add-my-server-confs [json confs]
  (merge-with into json confs))

(defn into-data-field [payload]
  (assoc {} :data payload))

(defn get-digest [request]
  (sign request))

(defn get-new-session [uuid]
  (let [sdkid (:docscan-sdk-id env)
        my-server-urls
        {:sdk_config {:success_url (:success-url env)
                      :error_url (:error-url env)}
         :notifications {:endpoint (:webhook-url env)
                         :auth_token (str (:webhook-usr env) ":" (:webhook-pw env))}}
        usertracking-uuid (str uuid)
        base-url "https://api.yoti.com/idverify/v1"
        json-payload (slurp (clojure.java.io/file (:kl-request-file env)))
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
    ;   (timbre/info "===== Getting new session for the iframe =========")
    ;   (timbre/info "endpoint: " endpoint)
    ;   (timbre/info "Request for signing: " request)
    ;   (timbre/info "to send payload: " payload)
    ;   (timbre/info "nonce: " nonce)
    ;   (timbre/info "time: " timestamp)
    (-> (:body http-response)
        read-json)))

(defn session-details
  ([session-id]
   (session-details nil session-id))
  ([{:keys [is-media? media-id]} session]
   (let [sdkid (:docscan-sdk-id env)
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
     (when (= "true" (env :kl-debug-docscan))
       (timbre/info "===== " (if is-media? "MEDIA" "SESSION") " DETAILS =========")
       (timbre/info (some-> req
                            (get :body req) ; either get :body req or return full req
                            parse-resp)))
     (some-> req
             (get :body req) ; either get :body req or return full req
             parse-resp))))

(defn media-details [sid mid]
  (session-details {:is-media? true
                    :media-id mid}
                   sid))

; (filter (= (:type (:checks resp)) "ID_DOCUMENT_TEXT_DATA_CHECK"))
; get (:generated_media (:checks resp))

(defn is-completed?
  ([webhook]
   (= (:topic webhook) "session_completion"))
  ([session-id webhook]
   (and (= (:session_id webhook)
           session-id)
        (= (:topic webhook)
           "check_completion"))))

(defn is-approved-and-done? [check]
  (and (= (:state check) "DONE")
       (= (-> check :report :recommendation :value) "APPROVE")))

(defn parse-checks [webhook]
  (when (is-completed? webhook)
    (let [ses-details (session-details (:session_id webhook))
          resources (:resources ses-details)
          id-document-used (-> resources
                               :id_documents
                               first)
          id-of-doc (:id id-document-used)
          document-type (:document_type id-document-used)
          issuing-country (:issuing_country id-document-used)
          all-checks (:checks ses-details)
          text-data-check (first (filter
                                  #(= (:type %)
                                      "ID_DOCUMENT_TEXT_DATA_CHECK")
                                  all-checks))
          gen-media-id (some->> (first (filter
                                        #(= (:type %)
                                            "ID_DOCUMENT_TEXT_DATA_EXTRACTION")
                                        (:tasks id-document-used)))
                                :generated_media
                                (filter
                                 #(= (:type %)
                                     "JSON"))
                                first
                                :id)
          result (if
                  (and (not (nil? text-data-check))
                       (is-approved-and-done? text-data-check))
                   {:ok? true :id (-> text-data-check
                                      :generated_media
                                      first
                                      :id)}
                   (if (not (nil? gen-media-id))
                     {:ok? true :id gen-media-id}
                     {:ok? false :reason (-> text-data-check
                                             :report
                                             :recommendation
                                             :reason)}))]
      (when (= "true" (env :kl-debug-docscan))
          (timbre/info "RESPONSE HAD NO ID_DOCUMENT_TEXT_DATA_CHECK")
          (timbre/info "RESPONSE HAD NO ID_DOCUMENT_TEXT_DATA_CHECK"))
      (merge result
             {:uuid (:user_tracking_id ses-details)
              :document-type document-type
              :issuing_country issuing-country
              :document-id id-of-doc}))))

(defn user-profile [media-request]
  (read-json media-request))


; if state done and report recommendation value is approve
;    where id of checks is last id given where topic check completion
;   get first id of generated media of checks where type ID_DOCUMENT_TEXT_DATA_CHECK