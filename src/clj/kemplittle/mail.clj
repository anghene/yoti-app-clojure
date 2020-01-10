(ns kemplittle.mail
 (:require  [postal.core :as postal]
            [selmer.parser :as parser]
            [taoensso.timbre :as timbre :refer [info]]
            [environ.core :refer [env]]))

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

(def contacts
  [
   {:id " 1 " :email "vlad.anghene@gmail.com"}
   {:id " 3 " :email "Chris.Gray@kemplittle.com"}
   {:id " 2 " :email "Gerard.Frith@kemplittle.com"}
   ])

(defn send-messages! [dest-id client-name validation-result]
  (info "send-messages! gets dest-id:" dest-id "client-name:" client-name " validation-result: " validation-result)
  (let [to-send (filter #(= (:id %) dest-id) contacts)]
    (info "to-send: " to-send)
    (doall (map
            #(do (postal/send-message
                  (smtp-settings) {:from "vlad@anghene.com"
                                   :to (:email %)
                                   :subject "Validation result"
                                   :body [{:type "text/plain"
                                           :content (format "Client name: %s\nValidation result: %s\n" client-name validation-result)}]})
                 (timbre/info "Sent a mail to: " (:email %)))
            to-send
            ))))

(defn send-validation-mail [dest-id user type]
  (when (= "true" (:send-emails env))
    (send-messages!
     dest-id
     user
     (str "SUCCESSFUL VALIDATION with " type))))

; {:type "text/html"
;  :content (parser/render-file
;            "welcome.email.html"
;            {:user user
;             :password password
;             :loginurl (str "https://" (env :chp-host) "/login")})}

