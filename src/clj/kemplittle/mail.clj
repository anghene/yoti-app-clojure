(ns kemplittle.mail
 (:require  [postal.core :as postal]
            [selmer.parser :as parser]
            [taoensso.timbre :as timbre :refer [info]]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [kemplittle.db.core :as db :refer [authdata match-authdata-initiated-by-id session-data]]))

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

(defn send-messages!
  [admin-email client-name validation-result]
  (info "PREPARING TO SEND: admin-mail:" admin-email " client-name:" client-name " validation-result:" validation-result)
  (postal/send-message
   (smtp-settings) {:from "hello@congruent.dev"
                    :to admin-email
                    :subject (str "Validation result for: " client-name)
                    :body [{:type "text/plain"
                            :content (format validation-result)}]})
  (timbre/info "Sent an mail to: " admin-email))


(defn match-session-track-data-uuid
  "Used by updates or yotiapp to get ref-id and client-name based on uuid"
  [uuid]
  (let [session-data (-> (filter #(= uuid (:uuid %))
                                 @session-data)
                         first)]
    session-data))

(defn send-validation-email [user-tracking-id user type]
  ; (info "send-validation-email gets called with this uuid: " user-tracking-id)
  (when (= "true" (:send-emails env))
    (let [failed? (not (:ok? user))
          address-line (if-not failed?
                         (case type
                           "YOTIAPP"
                           (->> (clojure.string/split (get (-> user
                                                               :structured_postal_address)
                                                           "formatted_address")
                                                      #"\n")
                                (clojure.string/join ", "))
                           "DOCSCAN"
                           (let [struct (some-> user :structured_postal_address)
                                 ad1 (some-> struct :address_line1)
                                 ad2 (some-> struct :address_line2)
                                 cntry (some-> struct :country_iso)]
                             (if (or (nil? struct)
                                     (and (nil? ad1) (nil? ad2)))
                               "N/A"
                               (clojure.string/join ", " [ad1 ad2 cntry])))))
          user-session (match-session-track-data-uuid user-tracking-id)
          ; if there's no uuid it means it started only with refid and that's what got passed as first param in this fn
          ref-id (if (nil? user-session) user-tracking-id nil)
          client-name (if (and (= "n/a" (:full_name user)) ; if there's only ref-id we fallback on n/a and keep it
                               (not ref-id))
                        (:client-name user-session)
                        (:full_name user))
          admin-email (if (not ref-id) ; if there's refid instead of uuid, it means we already have initiated-by-id so we pass it directly to match-authdata
                        (-> user-session
                            :initiated-by-id
                            match-authdata-initiated-by-id
                            :admin-email)
                        (-> ref-id
                            match-authdata-initiated-by-id
                            :admin-email))
          result (str "\nRESULT OF VALIDATION: " (if failed? "FAILED." "SUCCESSFUL.")
                      "\nMETHOD USED: " type
                      "\nFULL NAME: " client-name
                      (if failed?
                        (str "\nREASON: " (:reason user)
                             "\nDESCRIPTION: " (:description user)
                             "\nRECOMMENDATION: " (:recommendation user))
                        (str
                         "\nDOCUMENT TYPE: " (:document_type user)
                         "\nISSUING COUNTRY: " (:issuing_country user)
                         "\nEXPIRATION DATE: " (:expiration_date user)
                         "\nADDRESS: " address-line
                         "\nDATE OF BIRTH: " (get user :date_of_birth "N/A"))))]
      (send-messages!
         admin-email
         client-name
         result
         ))))

(defn filtered-opts [{:keys [is-uk-inc? is-dir-sec-leg? msg-map psc-names]}]
  (as-> msg-map $
    (if is-uk-inc? (dissoc $ :opt2 :opt3 :opt4 :opt5) (dissoc $ :opt1))
    (if is-dir-sec-leg? (dissoc $ :opt6) $)
    (if (not (nil? psc-names))
      (assoc-in $ [:opt1 :text :a] psc-names)
      $)))

(defn prompt-client-to-validate-himself [{:keys [admin-name ref-id admin-email client-email
                                                 client-name add-msg client-type psc-names
                                                 user-tracking-id msg-map is-uk-inc? is-dir-sec-leg?]}]

  (info "prompts with ref-id: " ref-id " and user-tracking-id: " user-tracking-id)
  (postal/send-message
   (smtp-settings)
   {:from "hello@congruent.dev"
    :to client-email
    :subject "Validation request from Kemp Little"
    :body [{:type "text/html"
            :content (parser/render-file (case client-type
                                           "1" "individual-email.html"
                                           "2" "corporate-email.html"
                                           nil)
                                         {:client-name client-name
                                          :admin-name admin-name
                                          :admin-email admin-email
                                          :user-tracking-id user-tracking-id
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

