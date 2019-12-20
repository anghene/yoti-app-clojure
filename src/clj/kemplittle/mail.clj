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

(defn send-contact-message! [name email message]
  (postal/send-message
   (smtp-settings) {:from "yoti@kemplittle.com"
                    :to "vlad.anghene@gmail.com"
                    :subject "TEST"
                    :body [{:type "text/plain"
                            :content (format "From: %s\n Email: %s\n %s" name email message)}]}))