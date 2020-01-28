(ns kemplittle.client.yotiapp
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as timbre :refer [info]]
            [kemplittle.db.core :refer [max-id users act]]
            [kemplittle.mail :refer [send-validation-email]]
            )
  (:import [com.yoti.api.client ActivityDetails Date FileKeyPairSource
            HumanProfile Image YotiClient YotiClientBuilder DocumentDetails]
           [com.yoti.api.client.spi.remote.call RawResourceFetcher SignedRequestResponse])
  (:gen-class))



(def client
  (let [ycb (. YotiClientBuilder newInstance)
        forapp (doto ycb (.forApplication (:yotiapp-sdk-id env)))
        wkp (. forapp withKeyPair (FileKeyPairSource/fromFile
                                   (java.io.File. (:yotiapp-priv-key-path env))))]
    (.build wkp)))

(defn persist-to-state! [activity-details user]
  (swap! users conj {:id @max-id
                     :type :yotiapp
                     :user-details user})
  (swap! act conj {:id @max-id :act activity-details})
  (swap! max-id inc)
  (timbre/info "Persisted a yotiapp user to state."))

(comment
  "
	String parentRememberMeId = activityDetails.getParentRememberMeId();
	Date timestamp = activityDetails.getTimestamp();
	String receiptId = activityDetails.getReceiptId();

	Image selfie = profile.getSelfie().getValue();
	String selfieBase64Content = selfie.getBase64Content();
	String fullName = profile.getFullName().getValue();
	String givenNames = profile.getGivenNames().getValue();
	String familyName = profile.getFamilyName().getValue();
	String phoneNumber = profile.getPhoneNumber().getValue();
	String emailAddress = profile.getEmailAddress().getValue();
	Date dateOfBirth = profile.getDateOfBirth().getValue();
	String gender = profile.getGender().getValue();
	String address = profile.getPostalAddress().getValue();
	String nationality = profile.getNationality().getValue();
	AgeVerification over18Verification = profile.findAgeOverVerification(18);
	if (over18Verification != null) {
	boolean isAgedOver18 = over18Verification.getResult();
	}
	AgeVerification under55Verification = profile.findAgeUnderVerification(55);
	if (under55Verification != null) {
	boolean isAgedUnder55 = under55Verification.getResult();
	}
	Map<?
?> structuredPostalAddress = profile.getStructuredPostalAddress().getValue();
	DocumentDetails documentDetails = profile.getDocumentDetails().getValue();

	String applicationName = applicationProfile.getApplicationName().getValue();
	String applicationUrl = applicationProfile.getApplicationUrl().getValue();
	Image applicationLogo = applicationProfile.getApplicationLogo().getValue();
	String applicationReceiptBgColor = applicationProfile.getApplicationReceiptBgColor().getValue();

	// You can retrieve the sources and verifiers for each attribute as follows:
	Attribute<String> givenNamesAttr = profile.getGivenNames();
	List<Anchor> givenNamesSources = givenNamesAttr.getSources();
	List<Anchor> givenNamesVerifiers = givenNamesAttr.getVerifiers();
	List<Anchor> givenNamesAnchors = givenNamesAttr.getAnchors();

	// You can also retrieve further properties from these respective anchors in the following way:

	// Retrieving properties of the first anchor
	Anchor firstSourceAnchor = givenNamesSources.get(0);
	String type = firstSourceAnchor.getType();
	String value = firstSourceAnchor.getValue();
	String subType = firstSourceAnchor.getSubType();
	SignedTimestamp signedTimestamp = firstSourceAnchor.getSignedTimestamp();
	List<X509Certificate> originCertificates = firstSourceAnchor.getOriginCertificates();")

(defn get-user [activity-details]
  (let [; remember-me-id (some-> activity-details .getRememberMeId)
        ; parent-remember-me-id (some-> activity-details .getParentRememberMeId)
        ; timestamp (some-> activity-details .getTimestamp)
        ; receipt-id (some-> activity-details .getReceiptId)
        profile (some-> activity-details .getUserProfile)
        ; selfie-img (some-> profile .getSelfie .getValue)
        ; selfie-base64 (some-> selfie-img .getBase64Content)
        full-name (some-> profile .getFullName .getValue)
        ; givenNames (some-> profile .getGivenNames .getValue)
        ; familyName (some-> profile .getFamilyName .getValue)
        ; phoneNumber (some-> profile .getPhoneNumber .getValue)
        ; emailAddress (some-> profile .getEmailAddress .getValue)
        dob (some-> profile .getDateOfBirth .getValue str)
        ; gender (some-> profile .getGender .getValue)
        ; address (some-> profile .getPostalAddress .getValue)
        ; nationality (some-> profile .getNationality .getValue)
        structured-address (some-> profile .getStructuredPostalAddress .getValue)
        ; is-over-18? (when-let [age-ver (some-> profile (..findAgeOverVerification 18))] (some-> age-ver .getResult))
        ; is-under-55? (when-let [age-ver (some-> profile (..findAgeUnderVerification 55))] (some-> age-ver .getResult))
        doc-details (some-> profile .getDocumentDetails)
        doc-details-value (some-> doc-details .getValue)
        doc-expiration-date (-> doc-details-value .getExpirationDate str)
        doc-type (-> doc-details-value .getType str)
        doc-issuing-country (-> doc-details-value .getIssuingCountry str)
        doc-serial-number (-> doc-details-value .getDocumentNumber str)

        ; gvn-names (some-> profile .getGivenNames)
        ; gvn-names-verifiers (some-> gvn-names .getVerifiers)
        ; gvn-names-sources (some-> gvn-names .getSources)
        ; gvn-names-anchors (some-> gvn-names .getAnchors)

        ; first-source-anchor (some-> gvn-names-sources (.get 0))
        ; first-sa-type (some-> first-source-anchor .getType)
        ; first-sa-value (some-> first-source-anchor .getValue)
        ; first-sa-subtype (some-> first-source-anchor .getSubType)
        ; signed-timestamp (some-> first-source-anchor .getSignedTimestamp)
        ; origin-certificates (some-> first-source-anchor .getOriginCertificates)

        ; application-profile (some-> activity-details .getApplicationProfile)
        ; app-name (some-> application-profile .getApplicationName .getValue)
        ; app-url (some-> application-profile .getApplicationUrl .getValue)
        ; app-logo (some-> application-profile .getApplicationLogo .getValue)
        ; app-receipt-bg-color (some-> application-profile .getApplicationReceiptBgColor .getValue)
        ]

    ; WHAT WE NEED

    ; a. Client's full name
    ; b. Client's date of birth
    ; c. Type of document provided
    ; d. Document expiry date
    ; e. Residential address

    ; DOCSCAN RESULTS STRUCTURE

    ; {:id 0
    ;  :session-id "ed63821c-d05f-4b79-bf6d-67e75d18d2b7"
    ;  :type :docscan
    ;  :user-details {:document_number "AX562311"
    ;                 :nationality "ROU"
    ;                 :expiration_date "2024-07-18"
    ;                 :issuing_country "ROU"
    ;                 :given_names "VLAD-ANDREI"
    ;                 :family_name "ANGHENE"
    ;                 :document_type "NATIONAL_ID"
    ;                 :date_of_birth "1986-07-18"
    ;                 :gender "MALE"
    ;                 :full_name "VLAD-ANDREI ANGHENE"
    ;                 :structured_postal_address {:address_format 4
    ;                                             :address_line1 "Mun. Alba Iulia Jud. Alba"
    ;                                             :address_line2 "Cal. Motilor nr.24"
    ;                                             :country_iso "ROU"}}

    ;  :media-id "c796415e-5530-4900-9b7f-50a43bcd3556"
    ;  :document-id "e0d6b458-cb7f-45fd-b09d-2008262ae502"}


    ; YOTI RESULTS STRUCTURE

    ; {:id 2
    ; :type :yotiapp
    ; :user-details {:full_name "VLAD-ANDREI ANGHENE"
    ;               :structured_postal_address {"address_format" 4
    ;                                           "address_line1" "Mun. Alba Iulia Jud. Alba"
    ;                                           "address_line2" "Cal. Motilor nr.24"
    ;                                           "country_iso" "ROU"
    ;                                           "country" "Romania"
    ;                                           "formatted_address" "Mun. Alba Iulia Jud. Alba\nCal. Motilor nr.24\nRomania"}
    ;               :date_of_birth "1986-07-18"
    ;               :expiration_date "N/A"
    ;               :document_type "N/A"
    ;               :document_number "N/A"
    ;               :issuing_country "N/A"}
    {
     :ok? (if-not (nil? profile) true)
     :full_name full-name
     :structured_postal_address structured-address
     :date_of_birth (str dob)
     :expiration_date (-> doc-details ((fn [doc] (if (nil? doc) "N/A" doc-expiration-date))))
     :document_type (-> doc-details ((fn [doc] (if (nil? doc) "N/A" doc-type))))
     :document_number (-> doc-details ((fn [doc] (if (nil? doc) "N/A" doc-serial-number))))
     :issuing_country (-> doc-details ((fn [doc] (if (nil? doc) "N/A" doc-issuing-country))))}))

(defn pass-token
  "activated when received a token via yoti digital id.
   not using ref anymore, using just uuid"
  [{:keys [token uuid ref-id]}]
  (let [activity-details (.getActivityDetails client token)
        user (get-user activity-details)
        trimmed-uuid (clojure.string/trim uuid)
        trimmed-ref-id (clojure.string/trim ref-id)
        user-tracking-id (if (or (empty? trimmed-uuid) (nil? trimmed-uuid) (= "null" trimmed-uuid))
                           trimmed-ref-id
                           uuid)]
    (info "Got a New YotiApp user with uuid: " uuid " and/or ref: " trimmed-ref-id)
    (persist-to-state! activity-details user)
    (try (send-validation-email user-tracking-id user "YOTIAPP")
         (catch Exception e (timbre/info (str "Error sending yotiapp emails : " e))))
    (timbre/info "users so far: " @users)))
