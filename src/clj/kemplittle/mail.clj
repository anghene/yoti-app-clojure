(ns kemplittle.mail
 (:require  [postal.core :as postal]
            [selmer.parser :as parser]
            [taoensso.timbre :as timbre :refer [info]]
            [selmer.parser :as parser]
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
                                         :content (format validation-result)}]})
               (timbre/info "Sent an mail to: " (:email %)))
          (filter #(= (:id %) dest-id) contacts))))

(defn send-validation-email [dest-id user type]
  (when (= "true" (:send-emails env))
    (let [failed? (not (:ok? user))]
      (send-messages!
       dest-id
       (:full_name user)
       (str "\nRESULT OF VALIDATION: " (if failed? "FAILED." "SUCCESSFUL.")
            "\nMETHOD USED: " type
            "\nFULL NAME: " (:full_name user)
            (if (= "DOCSCAN" type)
              (str
               "\nDOCUMENT TYPE: " (:document-type user)
               "\nISSUING COUNTRY: " (:issuing-country user)))
            (if failed?
              (str "\nREASON: " (:reason user)
                   "\nDESCRIPTION: " (:description user)
                   "\nRECOMMENDATION: " (:recommendation user))
              (str "\nADDRESS: " (get user :address "Not available with this type of document used.")
                   "\nDATE OF BIRTH: " (get user :dob "Not available with this type of document used."))))))))

(defn filtered-opts [{:keys [is-uk-inc? is-dir-sec-leg? msg-map psc-names]}]
  (as-> msg-map $
    (if is-uk-inc? (dissoc $ :opt2 :opt3 :opt4 :opt5) (dissoc $ :opt1))
    (if is-dir-sec-leg? (dissoc $ :opt6) $)
    (if (not (nil? psc-names))
      (assoc-in $ [:opt1 :text :a] psc-names)
      $)))

(defn prompt-client-to-validate-himself [{:keys [ref-name ref-id ref-email client-email
                                                 client-name add-msg client-type psc-names
                                                 msg-map is-uk-inc? is-dir-sec-leg?]}]
  (postal/send-message
   (smtp-settings)
   {:from "vlad@anghene.com"
    :to client-email
    :subject "Validation request from Kemp Little"
    :body [{:type "text/html"
            :content (parser/render-file (case client-type
                                           "1" "individual-email.html"
                                           "2" "corporate-email.html"
                                           nil)
                                         {:client-name client-name
                                          :ref-name ref-name
                                          :ref-email ref-email
                                          :ref-id ref-id
                                          :add-msg add-msg
                                          :opts (case client-type
                                                  "2" (filtered-opts {:is-uk-inc? is-uk-inc?
                                                                      :is-dir-sec-leg? is-dir-sec-leg?
                                                                      :msg-map msg-map
                                                                      :psc-names psc-names})
                                                  nil)})}]}))

; {:type "text/html"
;  :content (parser/render-file
;            "welcome.email.html"
;            {:user user
;             :password password
;             :loginurl (str "https://" (env :chp-host) "/login")})}

