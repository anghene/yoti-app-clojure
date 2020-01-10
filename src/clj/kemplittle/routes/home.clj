(ns kemplittle.routes.home
  (:require
   [kemplittle.routes.updates :refer [authenticated? notification-handler]]
   [kemplittle.layout :as layout]
   [kemplittle.mail :as mail]
   [kemplittle.db.core :as db :refer [max-id users]]
   [kemplittle.certs.core :as certs]
   [kemplittle.client.session :as session]
   [kemplittle.client.yotiapp :as yotiapp]
   [clojure.java.io :as io]
   [kemplittle.middleware :as middleware]
   [ring.util.response]
   [environ.core :refer [env]]
   [ring.util.http-response :as response]
   [taoensso.timbre :as timbre :refer [info]]
   [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
   [kemplittle.client.session :refer [get-new-session]]
   [clojure.data.json :refer [write-str read-json]]))

(defn home-page [{:keys [params query-params] :as request}]
  (layout/render request "index.html"))

(defn yotiapp-page
  "This route is used by the Yoti.Share.init() function on frontend,
   to send a token so we can retrieve a user from SDK."
  [{:keys [params]}]
  (timbre/info "Got request from Yoti servers: " (:token params))
  (yotiapp/pass-token (:token params) (:ref params))
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    "ok"})

(defn error-page [{:keys [params query-params] :as request}]
  (let [error (get params :yotiErrorCode)
        message (case error
                  "1000" "Mobile	No error occurred - the end-user cancelled the session for an unknown reason."
                  "2000" "Mobile/Web	Unauthorised request (wrong or expired session token)."
                  "2001" "Mobile/Web	Session not found."
                  "2002" "Mobile/Web	Session expired."
                  "2003" "Mobile/Web	SDK launched without session Token."
                  "2004" "Mobile/Web	SDK launched without session ID."
                  "3000" "Mobile/Web	Yoti's services are down or unable to process the request."
                  "3001" "Mobile "
                  "3002" "Mobile/Web	User has no network."
                  "4000" "Mobile/Web	The user did not grant permissions to the camera."
                  "5000" "Mobile/Web	No camera. When user's camera was not found and file upload is not allowed."
                  "5001" "Web	Unsupported browser/platform by the liveness flow"
                  "5002" "Mobile/Web	No more local tries for the liveness flow"
                  "5003" "Mobile	SDK is out-of-date - please update the SDK to the latest version"
                  "5004" "Mobile/Web	Unexpected internal error."
                  "5005" "Mobile/Web	Unexpected document scanning error."
                  "5006" "Mobile/Web	Unexpected liveness error."
                  "Unknown error")]
    (layout/render request "fancyerror.html" {:err message})))

(defn thankyou-page [request]
    (layout/render request "thankyou.html"))

(defn docscan-page [{:keys [params query-params] :as request}]
  (let [session (get-new-session (:ref params))]
    (timbre/info "session:" session)
    (layout/render request "docscan.html" {:session {:id (:session_id session)
                                                   :token (:client_session_token session)}})))

(defn new-session [{:keys [params query-params] :as request}]
  (let [sess (session/get-new-session (:ref params))
        id (:session_id sess)
        tkn (:client_session_token sess)]
    (timbre/info "(:ref params) asked for a new session and got : " sess)
    {:status (:status request)
     :headers {"Content-Type" "application/json"}
     :body (write-str {:id id :tkn tkn})}))

(defn updates-routes []
  ["/updates"
   {:middleware [#(-> % (wrap-basic-authentication authenticated?))]}
   ["" {:post notification-handler}]])

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/getsession" {:get new-session}]
   ["/yotiapp" {:get yotiapp-page}]
   ["/thankyou" {:get thankyou-page}]
   ["/error" {:get error-page}]
   ["/docscan" {:get docscan-page}]])

