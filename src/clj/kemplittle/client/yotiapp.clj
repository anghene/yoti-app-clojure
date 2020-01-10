(ns kemplittle.client.yotiapp
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as timbre :refer [info]]
            [kemplittle.db.core :refer [max-id users]]
            [kemplittle.mail :as mail]
            )
  (:import [com.yoti.api.client ActivityDetails Date FileKeyPairSource
            HumanProfile Image YotiClient YotiClientBuilder]
           [com.yoti.api.client.spi.remote.call RawResourceFetcher SignedRequestResponse]))

(def client
  (let [ycb (. YotiClientBuilder newInstance)
        forapp (doto ycb (.forApplication (:yotiapp-sdk-id env)))
        wkp (. forapp withKeyPair (FileKeyPairSource/fromFile
                                   (java.io.File. (:yotiapp-priv-key-path env))))]
    (.build wkp)))

(defn persist-to-state! [activity user]
  (swap! users conj {@max-id activity
                     :type :yotiapp
                     :user-details user})
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
	Map<?, ?> structuredPostalAddress = profile.getStructuredPostalAddress().getValue();
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
  (let [; remember-me-id (.. activity-details getRememberMeId)
         ; parent-remember-me-id (.. activity-details getParentRememberMeId)
         ; timestamp (.. activity-details getTimestamp)
         ; receipt-id (.. activity-details getReceiptId)
        profile (.. activity-details getUserProfile)
         ; selfie-img (.. profile getSelfie getValue)
         ; selfie-base64 (.. selfie-img getBase64Content)
        full-name (.. profile getFullName getValue)
         ; givenNames (.. profile getGivenNames getValue)
         ; familyName (.. profile getFamilyName getValue)
         ; phoneNumber (.. profile getPhoneNumber getValue)
        emailAddress (.. profile getEmailAddress getValue)
         ; dob (.. profile getDateOfBirth getValue)
         ; gender (.. profile getGender getValue)
         ; address (.. profile getPostalAddress getValue)
         ; structured-address (.. profile getStructuredPostalAddress getValue)
         ; nationality (.. profile getNationality getValue)
         ; is-over-18? (when-let [age-ver (.. profile (.findAgeOverVerification 18))] (.. age-ver getResult))
         ; is-under-55? (when-let [age-ver (.. profile (.findAgeUnderVerification 55))] (.. age-ver getResult))
         ; doc-details (.. profile getDocumentDetails getValue)
         ; application-profile (.. activity-details getApplicationProfile)
         ; app-name (.. application-profile getApplicationName getValue)
         ; app-url (.. application-profile getApplicationUrl getValue)
         ; app-logo (.. application-profile getApplicationLogo getValue)
         ; app-receipt-bg-color (.. application-profile getApplicationReceiptBgColor getValue)
         ; gvn-names (.. profile getGivenNames)
         ; gvn-names-sources (.. gvn-names getSources)
         ; gvn-names-verifiers (.. gvn-names getVerifiers)
         ; gvn-names-anchors (.. gvn-names getAnchors)
         ; first-source-anchor (.. gvn-names-sources (.get 0))
         ; first-sa-type (.. first-source-anchor getType)
         ; first-sa-value (.. first-source-anchor getValue)
         ; first-sa-subtype (.. first-source-anchor getSubType)
         ; signed-timestamp (.. first-source-anchor getSignedTimestamp)
         ; origin-certificates (.. first-source-anchor getOriginCertificates)
        ]
    {:name full-name :email emailAddress}))

(defn pass-token
  "activated when received a token via yoti digital id"
  [token dest-id]
  (let [activity-details (.getActivityDetails client token)
        user (get-user activity-details)
        trimmed-dest-id (clojure.string/trim dest-id)]
    (info trimmed-dest-id " asked for a New YotiApp user.")
    (persist-to-state! activity-details user)
    (try (mail/send-validation-mail trimmed-dest-id (-> user :name) "Yoti APP")
         (catch Exception e (timbre/info (str "Error sending yotiapp emails : " e))))
    (timbre/info "users so far: " @users)))
