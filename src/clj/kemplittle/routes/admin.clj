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
   ))



(defn ok [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})
(defn unauthorized [d] {:status 401 :body d})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Controllers                                      ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Home page controller (ring handler)
;; If incoming user is not authenticated it raises a not authenticated
;; exception, else it simply shows a hello world message.

(defn home
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (ok {:status "Logged"
         :message (str "Hello "
                       (-> request
                           :identity
                           :name))})))

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

;; Global var that stores valid users with their
;; respective passwords.

(def authdata {:admin {:password "open4me"
                       :email "vlad.anghene@gmail.com"
                       :name "Vlad"}
               :secretary1 {:password "secret"
                            :email ""
                            :name "John"}
               :secretary2 {:password "nuclear"
                            :email ""
                            :name "John"}
               :secretary3 {:password "thisthat"
                            :email ""
                            :name "John"}
               :chris {:password "whitesnow"
                       :email ""
                       :name "Chris"}
               :gerard {:password "greentree"
                        :email "Gerard.Frith @kemplittle.com"
                        :name "Gerard"}})

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
                    :exp (time/plus (time/now) (time/seconds 3600))
                    :email (get-in authdata [(keyword username) :email])
                    :name (get-in authdata [(keyword username) :name])}
            token (jwt/sign claims secret {:alg :hs512})]
        (ok (-> {:token token}
                )))
      (bad-request {:message "wrong auth data"}))))

(defn admin-routes []
  ["/api"
   {:middleware [middleware/wrap-auth]}
   ["/get-info" {:get home}]])

(defn login-route []
  ["/login"
   ["" {:post login}]])



