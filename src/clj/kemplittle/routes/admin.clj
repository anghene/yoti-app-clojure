(ns kemplittle.routes.admin
  (:require
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [buddy.auth.backends.token :refer [jws-backend]]
   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as time]
   [kemplittle.db.core :refer [users authdata session-data]]
   [clojure.data.json :refer [write-str read-json]]
   [environ.core :refer [env]]
   [kemplittle.middleware :as middleware :refer [secret]]
   [ring.util.http-response :as response]
   [ring.util.response]
   [taoensso.timbre :as timbre :refer [info]]
   [cheshire.core :as json]
   [kemplittle.mail :as mail :refer [prompt-client-to-validate-himself]]
   ))


;; Global var that stores valid users with their
;; respective passwords.

(defn new-user-track-session!
  "When secretary starts validation process,
   we use uuid to keep tabs on which secretary and which client name
   to retrieve it later for cases when validation failed."
  [ref-id client-name]
  (let [uuid (.toString (java.util.UUID/randomUUID))
        new-track-session {:uuid uuid
                           :initiated-by-id ref-id
                           :client-name client-name}]
    (swap! session-data conj new-track-session)
    (info "started new user-track-session on: " uuid " for ref-id:" ref-id " tracking: " client-name)
    new-track-session))

(defn match-session-data-uuid
  "Used by updates or yotiapp to get ref-id and client-name based on uuid"
  [uuid]
  (let [sdata (-> (filter #(= uuid (:uuid %))
                                 @session-data)
                         first)
        ]
    sdata))

(def admin-msg-opts
  (atom {:opt1 {:desc {:above {:a "In order to assist you with the process we will obtain any relevant company documents which are available from Companies House. To this end, we have reviewed the Persons of Significant Control (PSC) register of"
                               :b "and note that the following individuals are shown on the PSC register:"}
                       :below {:a "Can you confirm that the PSC register of"
                               :b "is up to date ?"}}}
         :opt2 {:desc {:above {:a "Please could you also provide us with the following in respect of"
                               :b ":"}
                       :below {:a nil
                               :b nil}}
                :text {:a "The name(s) of any individual who, directly or indirectly, holds more than 25% of the shares or voting rights in"
                       :b "or otherwise controls"}}
         :opt3 {:desc {:above {:a nil
                               :b nil}
                       :below {:a nil
                               :b nil}}
                :text {:a "Certificate of good standing or equivalent"}}
         :opt4 {:desc {:above {:a nil
                               :b nil}
                       :below {:a nil
                               :b nil}}
                :text {:a "most recent annual accounts or financial statement; and"}}
         :opt5 {:desc {:above {:a nil
                               :b nil}
                       :below {:a nil
                               :b nil}}
                :text {:a "By-laws or other company governance instrument."}}
         :opt6 {:text {:a "Further, we will also require evidence of the authority given to you to instruct us from the director of"
                       :b "who has provided their identification documents. This can take the form of a letter or an email and should be received before we are able to proceed with this matter."}}}))

(defn ok [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})
(defn unauthorized [d] {:status 401 :body d})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Controllers                                      ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Home page controller (ring handler)
;; If incoming user is not authenticated it raises a not authenticated
;; exception, else it simply shows a hello world message.

(defn match-authdata-usr-header
  "get name from state based on :username from header"
  [local-user]
  (let [user-name (get-in authdata [(keyword local-user) :name])
        user-email (get-in authdata [(keyword local-user) :email])
        user-id (get-in authdata [(keyword local-user) :id])]
    {:name user-name
     :email user-email
     :id user-id}
    ))

(defn match-header-usr-access
  "get access from state based on :username from header"
  [local-user]
  (let [
        access-level (get-in authdata [(keyword local-user) :access])]
    access-level
    ))

(defn prompt-client-via-email [{:keys [admin-name user-tracking-id admin-email client-email
                                       client-name add-msg client-type psc-names ref-id
                                       msg-map is-uk-inc? is-dir-sec-leg?]}]
    ; (info "prompt function gets: " admin-email user-tracking-id client-email)
    (try (do (prompt-client-to-validate-himself {:user-tracking-id user-tracking-id
                                                 :ref-id ref-id
                                                 :admin-email admin-email
                                                 :admin-name admin-name
                                                 :client-email client-email
                                                 :client-name client-name
                                                 :client-type client-type
                                                 :add-msg add-msg
                                                 :is-uk-inc? is-uk-inc?
                                                 :is-dir-sec-leg? is-dir-sec-leg?
                                                 :msg-map msg-map
                                                 :psc-names psc-names})
             (timbre/info "Sent a prompt validation mail to: " client-email))
         (catch Exception e (info "Unable to send prompt-client email: " e))))

(defn start-client
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (if-not (:body request)
      (bad-request {:status "Failed"
           :message "No data sent."})
      (let [client-email (get-in request [:body :client-email])
            client-name (get-in request [:body :client-name])
            add-msg (get-in request [:body :add-msg])
            local-user (-> request
                           :identity
                           :user)
            admin-data (match-authdata-usr-header local-user)
            admin-email (:email admin-data)
            admin-name (:name admin-data)
            ref-id (:id admin-data)
            user-session (new-user-track-session! ref-id client-name)]
        (info "this is admin-data: " admin-data)
        (prompt-client-via-email {:add-msg add-msg
                                  :admin-name admin-name
                                  :admin-email admin-email
                                  :user-tracking-id (:uuid user-session)
                                  :ref-id ref-id
                                  :client-email client-email
                                  :client-name client-name
                                  :msg-map @admin-msg-opts
                                  :client-type "2"
                                  :is-uk-inc? true
                                  :psc-names nil
                                  :is-dir-sec-leg? true})
        (ok {:status "Ok"
             :message (str "Successfully started a validation process: " (:uuid user-session))})))))

(defn start-session
  "Endpoint to simply start a new session.
   Looks for client-name in request and admin-data in header.
   Returns a user-tracking-id to be used by secretary."
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (if-not (:body request)
      (bad-request {:status "Failed"
           :message "No data sent."})
      (let [client-name (get-in request [:body :client-name])
            local-user (-> request
                           :identity
                           :user)
            admin-data (match-authdata-usr-header local-user)
            ; admin-email (:email admin-data)
            ; admin-name (:name admin-data)
            ref-id (:id admin-data)
            user-session (new-user-track-session! ref-id client-name)]
        ; (info "this is admin-data: " admin-data)
        (ok {:status "Ok"
             :message (str "Generated a new uuid for: " client-name)
             :new-uuid (:uuid user-session)
             :client-name client-name})))))

(defn access-level
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (let [local-user (-> request
                         :identity
                         :user)
          lvl (match-header-usr-access local-user)]
      (ok {:access lvl}))))

(defn parse-log [text]
  (->>
   (re-seq #"(?<=)(.*)(?=\n)" text)
   (map
    #(drop-while empty? %))
   (remove empty?)
   (take-last 30)
   ))

(defn parse-user-for-client [user]
  (when user
    (assoc {}
           :id nil
           :session nil
           :name (-> user :user-details :full_name)
           :type (-> user :type)
           :user-details (-> user :user-details))
         ))

(defn admin-info-page
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (let [log (parse-log (slurp "new-server.log"))]
      (ok {:logs log
           :opts @admin-msg-opts
           :users (map parse-user-for-client @users)}))))

(defn get-user-data
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (ok {:status "User Data"
         :message "Status OK"
         :user-data (-> request
                        :identity
                        )})))

(defn show-connected
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (ok {:status "Clients"
         :message "Status OK"
         :user-data (-> request
                        :identity
                        )})))

;; Authenticate Handler
;; Responds to post requests in same url as login and is responsible for
;; identifying the incoming credentials and setting the appropriate authenticated
;; user into session. `authdata` will be used as source of valid users.

(defn login
  [request]
  (info "gets to login: " (:body request))
  (let [username (get-in request [:body :username])
        password (get-in request [:body :password])
        valid? (some-> authdata
                       (get (keyword username))
                       :password
                       (= password))]
    (if valid?
      (let [claims {:user (keyword username)
                    :exp (time/plus (time/now) (time/seconds 3600))}
            token (jwt/sign claims secret {:alg :hs512})]
        (ok (-> {:token token}
                )))
      (bad-request {:message "wrong auth data"}))))

(defn admin-routes []
  ["/api"
   ["/get-info" {:get admin-info-page}]
   ["/start-client" {:post start-client}]
   ["/start-session" {:post start-session}]
   ["/get-access-level" {:get access-level}]])

(defn login-route []
  [""
   ["/login"
    ["" {:post login}]]])



