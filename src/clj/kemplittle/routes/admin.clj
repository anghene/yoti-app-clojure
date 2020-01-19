(ns kemplittle.routes.admin
  (:require
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [buddy.auth.backends.token :refer [jws-backend]]
   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   [buddy.core.nonce :refer [random-bytes]]
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as time]
   [kemplittle.db.core :refer [users]]
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

(def authdata {:admin {:password "open4me"
                       :email "vlad.anghene@gmail.com"
                       :name "Vlad"
                       :access "admin"}
               :secretary1 {:password "secret"
                            :email "Gerard.Frith@kemplittle.com"
                            :name "Gerard Frith"
                            :access "secretary"}
               :secretary2 {:password "nuclear"
                            :email "Chris.Gray@kemplittle.com"
                            :name "Chris Gray"
                            :access "secretary"}
               :secretary3 {:password "thisthat"
                            :email ""
                            :name "John"
                            :access "secretary"}
               :chris {:password "whitesnow"
                       :email "Chris.Gray@kemplittle.com"
                       :name "Chris Gray"
                       :access "admin"}
               :gerard {:password "greentree"
                        :email "Gerard.Frith@kemplittle.com"
                        :name "Gerard Frith"
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

(defn match-header-usr-name
  "get name from state based on :username from header"
  [request local-user]
  (let [
        user-name (get-in authdata [(keyword local-user) :name])]
    user-name
    ))

(defn match-header-usr-mail
  "get email from state based on :username from header"
  [request local-user]
  (let [
        user-email (get-in authdata [(keyword local-user) :email])]
    user-email
    ))

(defn match-header-usr-access
  "get access from state based on :username from header"
  [request local-user]
  (let [
        access-level (get-in authdata [(keyword local-user) :access])]
    access-level
    ))

(defn prompt-client-via-email [{:keys [ref-name ref-id ref-email client-email
                                       client-name add-msg client-type psc-names
                                       msg-map is-uk-inc? is-dir-sec-leg?]}]
    ; (info "prompt function gets: " ref-email ref-id client-email)
    (try (do (prompt-client-to-validate-himself {:ref-id ref-id
                                                 :ref-email ref-email
                                                 :ref-name ref-name
                                                 :client-email client-email
                                                 :client-name client-name
                                                 :client-type client-type
                                                 :add-msg add-msg
                                                 :is-uk-inc? is-uk-inc?
                                                 :is-dir-sec-leg? is-dir-sec-leg?
                                                 :msg-map msg-map
                                                 :psc-names psc-names})
             (timbre/info "Sent an mail to: " client-email))
         (catch Exception e (info "Unable to send prompt-client email: " e))))

(defn start-client
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (if-not (:body request)
      (ok {:status "Failed"
           :message "No body sent."})
      (let [client-email (get-in request [:body :client-email])
            client-name (get-in request [:body :client-name])
            add-msg (get-in request [:body :add-msg])
            local-user (-> request
                           :identity
                           :user)
            own-name (match-header-usr-name request local-user)
            own-email (match-header-usr-mail request local-user)
            own-data (filter #(= (:email %) own-email) contacts)
            ref-id (-> own-data first :id)]
        #_(info "gets to authorized level with client-email: " client-email " and : " own-email)
        (prompt-client-via-email {:add-msg add-msg
                                  :ref-name own-name
                                  :ref-id ref-id
                                  :ref-email own-email
                                  :client-email client-email
                                  :client-name client-name
                                  :msg-map admin-msg-opts
                                  :client-type "2"
                                  :is-uk-inc? nil
                                  :psc-names nil
                                  :is-dir-sec-leg? true
                                  })
        (ok {:status "Ok"
             :message (str "Successfully send email to : " client-name " with ref-id " ref-id)})))))

(defn access-level
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (let [local-user (-> request
                         :identity
                         :user)
          lvl (match-header-usr-access request local-user)]
      (ok {:access lvl}))))

(defn parse-log [text]
  (->>
   (re-seq #"(?<=)(.*)(?=\n)" text)
   (map
    #(drop-while empty? %))
   (remove empty?)
   (take-last 30)
   ))

(defn parse-users-for-client [users]
  (map #(assoc {}
               :id (-> % first first)
               :name (get % [:user-details :name] "Couldn't get name")
               :type (-> % :type)
               :user-details (-> % :user-details))
       users))

(defn admin-info-page
  [request]
  (if-not (authenticated? request)
    (unauthorized {:message "Unauthorized"})
    (let [log (parse-log (slurp "new-server.log"))]
      (ok {:logs log
           :opts @admin-msg-opts
           :users (parse-users-for-client @users)}))))

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
   ["/get-access-level" {:get access-level}]])

(defn login-route []
  [""
   ["/login"
    ["" {:post login}]]])



