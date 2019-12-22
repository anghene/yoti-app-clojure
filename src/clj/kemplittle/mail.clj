(ns kemplittle.mail
 (:require  [postal.core :as postal]
            [selmer.parser :as parser]
            [taoensso.timbre :as timbre]
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

(defn send-messages! [contacts client-name validation-result]
  (map
   #(postal/send-message
     (smtp-settings) {:from "yoti@kemplittle.com"
                      :to (:email %)
                      :subject "TEST"
                      :body [{:type "text/plain"
                              :content (format "Regarding: %s\n Validation result: %s\n %s" client-name validation-result)}]})
   contacts))

; {:type "text/html"
;  :content (parser/render-file
;            "welcome.email.html"
;            {:user user
;             :password password
;             :loginurl (str "https://" (env :chp-host) "/login")})}

