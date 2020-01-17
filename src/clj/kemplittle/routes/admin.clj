(ns kemplittle.routes.admin
  (:require
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [buddy.auth.backends.token :refer [jws-backend]]
   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   [buddy.core.nonce :refer [random-bytes]]
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as time]
   [clojure.data.json :refer [write-str read-json]]
   [environ.core :refer [env]]
   [kemplittle.middleware :as middleware :refer [secret]]
   [ring.util.http-response :as response]
   [ring.util.response]
   [taoensso.timbre :as timbre :refer [info]]
   [cheshire.core :as json]
   [kemplittle.mail :as mail :refer [prompt-client-to-validate-himself contacts]]
   ))


;; Global var that stores valid users with their
;; respective passwords.

(def authdata {:admin {:password "open4me"
                       :email "vlad.anghene@gmail.com"
                       :name "Vlad"
                       :access "admin"}
               :secretary1 {:password "secret"
                            :email "Gerard.Frith@kemplittle.com"
                            :name "John"
                            :access "secretary"}
               :secretary2 {:password "nuclear"
                            :email "Chris.Gray@kemplittle.com"
                            :name "John"
                            :access "secretary"}
               :secretary3 {:password "thisthat"
                            :email ""
                            :name "John"
                            :access "secretary"}
               :chris {:password "whitesnow"
                       :email "Chris.Gray@kemplittle.com"
                       :name "Chris"
                       :access "admin"}
               :gerard {:password "greentree"
                        :email "Gerard.Frith@kemplittle.com"
                        :name "Gerard"
                        :access "admin"}})

(defn ok [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})
(defn unauthorized [d] {:status 401 :body d})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Controllers                                      ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Home page controller (ring handler)
;; If incoming user is not authenticated it raises a not authenticated
;; exception, else it simply shows a hello world message.

(defn check-user-email [request]
  (let [local-user (-> request
                       :identity
                       :user)
        user-email (get-in authdata [(keyword local-user) :email])]
    user-email
    ))

(defn check-user-access [request]
  (let [local-user (-> request
                       :identity
                       :user)
        access-level (get-in authdata [(keyword local-user) :access])]
    access-level
    ))

(defn prompt-client-via-email [{:keys [ref-email client-email client-name]}]
  (let [own-data (filter #(= (:email %) ref-email) contacts)
        ref-id (-> own-data first :id)]
    (info "prompt function gets: " ref-email ref-id client-email)
    (try (do (prompt-client-to-validate-himself ref-id ref-email client-email client-name)
             (timbre/info "Sent a mail to: " client-email))
         (catch Exception e (info "Unable to send prompt-client email: " e)))))

(defn start-client
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (if-not (:body request)
      (ok {:status "Failed"
           :message "No body sent."})
      (let [client-email (get-in request [:body :client-email])
            client-name (get-in request [:body :client-name])
            own-email (check-user-email request)]
        #_(info "gets to authorized level with client-email: " client-email " and : " own-email)
        (prompt-client-via-email {:ref-email own-email :client-email client-email :client-name client-name})
        (ok {:status "Ok"
             :message (str "Sent an email to client: " client-name " with your ref-id")})))))

(defn access-level
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (let [lvl (check-user-access request)]
      (ok {:access lvl}))))

(defn home
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (let [lvl (check-user-access request)]
      (info "level is: " lvl)
      (ok {:info "no info"}))))

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
   ["/get-info" {:get home}]
   ["/start-client" {:post start-client}]
   ["/get-access-level" {:get access-level}]])

(defn login-route []
  [""
   ["/login"
    ["" {:post login}]]])



