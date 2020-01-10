(ns kemplittle.routes.updates
  (:require
   [environ.core :refer [env]]
   [taoensso.timbre :as timbre]
   [clojure.data.json :refer [read-json]]
   [kemplittle.db.core :refer [max-id users]]
   [kemplittle.client.session :refer [is-completed? text-check-id media-details]]
   [kemplittle.mail :refer [send-validation-mail]]))

(defn is-admin? [name pass]
  (and (= name "yotiupdate")
       (= pass "3HHXmXZJfJT1fX@Yd6#r2%0nG")))

(defn authenticated? [name pass]
  (if (is-admin? name pass)
    (do
        ; set some state
      true)
    false))

(defn persist-to-state! [session-id user]
  (swap! users conj {@max-id session-id
                     :type :docscan
                     :user-details user})
  (swap! max-id inc)
  (timbre/info "Persisted a docscan user to state."))

(defn notification-handler [req]
  (let [webhook (:body req)]
    (timbre/info "got params on webhook: " webhook)
    (when (is-completed? webhook)
      (let [session-id (:session_id webhook)
            text-check-id (text-check-id webhook)
            media-id (:media-id text-check-id)
            dest-id (:dest-id text-check-id)
            user (media-details session-id media-id)]
        (timbre/info "[DOCSCAN] user to persist: " user)
        (try (send-validation-mail dest-id (-> user :full_name) "DOCSCAN")
             (catch Exception e (timbre/info (str "Error sending Docscan emails : " e))))
        (persist-to-state! session-id user))
      (timbre/info "users thus far: " @users))
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    "Thanks for the update Yoti"}))
