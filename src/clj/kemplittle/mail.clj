(ns kemplittle.mail
 (:require  [postal.core :as postal]
            [selmer.parser :as parser]
            [taoensso.timbre :as timbre :refer [info]]
            [environ.core :refer [env]]))

(def contacts
  [{:id "1" :email "vlad.anghene@gmail.com"}
   {:id "2" :email "Gerard.Frith@kemplittle.com"}
   {:id "3" :email "Chris.Gray@kemplittle.com"}])

(defn smtp-settings []
  (let [host (env :kl-smtp-host)
        port (Integer/valueOf (env :kl-smtp-port))
        user (env :kl-smtp-user)
        pass (env :kl-smtp-pass)
        ssl (env :kl-smtp-ssl)
        base-params {:host host :port port :user user :pass pass}]
    (cond
      (= ssl "ssl") (assoc base-params :ssl true)
      (= ssl "tls") (assoc base-params :tls true)
      :else base-params)))

(defn send-messages! [dest-id client-name validation-result]
  (info "send-messages! gets dest-id:" dest-id " client-name:" client-name " validation-result:" validation-result)
  (doall (map
          #(do (postal/send-message
                (smtp-settings) {:from "vlad@anghene.com"
                                 :to (:email %)
                                 :subject "Validation result"
                                 :body [{:type "text/plain"
                                         :content (format "Client name: %s\nValidation result: %s\n" client-name validation-result)}]})
               (timbre/info "Sent a mail to: " (:email %)))
          (filter #(= (:id %) dest-id) contacts))))

(defn send-validation-mail [dest-id user type]
  (when (= "true" (:send-emails env))
    (send-messages!
     dest-id
     user
     (str "SUCCESSFUL VALIDATION with " type))))

(defn prompt-client-to-validate-himself [ref-id ref-email client-email client-name]
  (postal/send-message
   (smtp-settings)
   {:from "vlad@anghene.com"
    :to client-email
    :subject "Validation request from Kemp Little"
    :body [{:type "text/html"
            :content (format
                      "Hello %s.
                       <br>
                       %s would like to ask you to kindly initiate the validation process over at https://identity.kemplittle.com/#/?ref=%s.
                       <br>
                       Please follow the link to get started." client-name ref-email ref-id)}]}))

; {:type "text/html"
;  :content (parser/render-file
;            "welcome.email.html"
;            {:user user
;             :password password
;             :loginurl (str "https://" (env :chp-host) "/login")})}

