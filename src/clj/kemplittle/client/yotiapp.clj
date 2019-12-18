(ns kemplittle.client.yotiapp
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            )
  (:import [com.yoti.api.client ActivityDetails Date FileKeyPairSource
            HumanProfile Image YotiClient YotiClientBuilder]
           [com.yoti.api.client.spi.remote.call RawResourceFetcher SignedRequestResponse]))

(def max-id (atom 0))

(def users (atom {}))

(def client
  (let [ycb (. YotiClientBuilder newInstance)
        forapp (doto ycb (.forApplication (:yotiapp-sdk-id env)))
        wkp (. forapp withKeyPair (FileKeyPairSource/fromFile
                                   (java.io.File. (:yotiapp-priv-key-path env))))]
    (.build wkp)))

(defn pass-token [token]
  (let [activity-details (.getActivityDetails client token)
        ]
    (swap! users conj {@max-id activity-details})
    (swap! max-id inc)
    (timbre/info "users so far: " @users)))

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
List<X509Certificate> originCertificates = firstSourceAnchor.getOriginCertificates();"
)

(defn get-user [activity-details]
   (let [user-profile (.. activity-details getUserProfile)
        application-profile (.. activity-details getApplicationProfile)
        remember-me-id (.. activity-details getRememberMeId)
        parent-remember-me-id (.. activity-details getParentRememberMeId)
        ]
   nil))