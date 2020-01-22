(ns kemplittle.routes.updates
  (:require
   [environ.core :refer [env]]
   [taoensso.timbre :as timbre]
   [clojure.data.json :refer [read-json]]
   [kemplittle.db.core :refer [max-id users]]
   [kemplittle.client.session :refer [is-completed? parse-checks media-details]]
   [kemplittle.mail :refer [send-validation-email]]))

(defn is-admin? [name pass]
  (and (= name "yotiupdate")
       (= pass "3HHXmXZJfJT1fX@Yd6#r2%0nG")))

(defn authenticated? [name pass]
  (if (is-admin? name pass)
    (do
        ; set some state
      true)
    false))

(def docscan-errors
  [{:reason "PHOTO_OVEREXPOSED"	:description "The photo is overexposed"	:recommendation "NOT_AVAILABLE"}
   {:reason "PHOTO_TOO_DARK"	:description "The photo is dark"	:recommendation "NOT_AVAILABLE"}
   {:reason "PHOTO_TOO_BLURRY"	:description "The photo is blurry"	:recommendation "NOT_AVAILABLE"}
   {:reason "DOCUMENT_TOO_DAMAGED"	:description "Document is too damaged (at the point we cannot check)"	:recommendation "NOT_AVAILABLE"}
   {:reason "GLARE_OBSTRUCTION"	:description "The photo has a glare"	:recommendation "NOT_AVAILABLE"}
   {:reason "OBJECT_OBSTRUCTION"	:description "Object in the way"	:recommendation "NOT_AVAILABLE"}
   {:reason "UNABLE_TO_LOAD"	:description "Unable to load file"	:recommendation "NOT_AVAILABLE"}
   {:reason "PARTIAL_PHOTO"	:description "The photo is partially taken"	:recommendation "NOT_AVAILABLE"}
   {:reason "IMAGE_RESOLUTION_TOO_LOW"	:description "The image resolution is too low"	:recommendation "NOT_AVAILABLE"}
   {:reason "COUNTRY_NOT_SUPPORTED"	:description "The country is not supported"	:recommendation "NOT_AVAILABLE"}
   {:reason "DOCUMENT_NOT_SUPPORTED"	:description "Yoti does not support this document"	:recommendation "NOT_AVAILABLE"}
   {:reason "INCORRECT_DOCUMENT_TYPE"	:description "The document type is incorrect"	:recommendation "NOT_AVAILABLE"}
   {:reason "INCORRECT_MRZ"	:description "MRZ on passport is incorrect"	:recommendation "NOT_AVAILABLE"}
   {:reason "DOCUMENT_VERSION_NOT_SUPPORTED"	:description "Yoti does not support this document template"	:recommendation "NOT_AVAILABLE"}
   {:reason "MISSING_DOCUMENT_SIDE"	:description "The full document was not sent"	:recommendation "NOT_AVAILABLE"}
   {:reason "BLACK_AND_WHITE_IMAGE"	:description "Black and white photo"	:recommendation "NOT_AVAILABLE"}
   {:reason "MISUSE"	:description "The photo has been misused"	:recommendation "NOT_AVAILABLE"}
   {:reason "INVALID"	:description "The photo is not a valid photo"	:recommendation "NOT_AVAILABLE"}
   {:reason "DOCUMENT_COPY"	:description "The photo has been photocopied"	:recommendation "REJECT"}
   {:reason "NO_HOLOGRAM_MOVEMENT"	:description "There is no hologram movement detected"	:recommendation "REJECT"}
   {:reason "TAMPERED"	:description "The photo seems to be tampered with"	:recommendation "REJECT"}
   {:reason "MISSING_HOLOGRAM"	:description "There is no hologram on the photo"	:recommendation "REJECT"}
   {:reason "EXPIRED_DOCUMENT"	:description "The document has expired"	:recommendation "REJECT"}])

(defn persist-to-state! [session-id user media-id doc-id]
  (swap! users conj {:id @max-id
                     :session-id session-id
                     :type :docscan
                     :user-details user
                     :media-id media-id
                     :document-id doc-id})
  (swap! max-id inc)
  (timbre/info "Persisted a docscan user to state."))

(defn notification-handler [req]
  (let [webhook (:body req)]
    (timbre/info "got params on webhook: " webhook)
    (when (is-completed? webhook)
      (let [session-id (:session_id webhook)
            server-response (parse-checks webhook)
            doc-id (:document-id server-response)
            failed? (not (:ok? server-response))
            media-id (if-not failed?
                       (:id server-response))
            uuid (some-> server-response
                         :uuid
                         clojure.string/trim)
            ref-id (some-> server-response
                           :ref
                           clojure.string/trim)
            user (if-not failed?
                   (assoc (media-details session-id media-id)
                          :ok? true)
                   (let [reason (:reason server-response)
                         reason-with-aid (first
                                          (filter #(= reason (:reason %))
                                                  docscan-errors))
                         desc (:description reason-with-aid)
                         recom (:recommendation reason-with-aid)]
                     {:full_name "n/a" :reason reason
                      :description desc :recommendation recom
                      :ok? (:ok? server-response)}))]
        (timbre/info "[DOCSCAN] user to persist: " user)
        (try (send-validation-email (if (not uuid) ref-id uuid) user "DOCSCAN")
             (catch Exception e (timbre/info (str "Error sending Docscan emails : " e))))
        #_(persist-to-state! session-id user media-id doc-id))
      (timbre/info "users thus far: " @users))
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    "Thanks for the update Yoti"}))
