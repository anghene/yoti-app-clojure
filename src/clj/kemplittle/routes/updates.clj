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

(defn persist-to-state! [session-id user]
  (swap! users conj {:id @max-id
                     :session session-id
                     :type :docscan
                     :user-details user})
  (swap! max-id inc)
  (timbre/info "Persisted a docscan user to state."))

(defn notification-handler [req]
  (let [webhook (:body req)]
    (timbre/info "got params on webhook: " webhook)
    (when (is-completed? webhook)
      (let [session-id (:session_id webhook)
            server-response (parse-checks webhook)
            failed? (not (:ok? server-response))
            media-id (if-not failed?
                      (:id server-response))
            dest-id (clojure.string/trim
                     (:dest-id server-response))
            user (if-not failed?
                    (media-details session-id media-id)
                     {:full_name n/a :reason (:reason server-response)})]
        (timbre/info "[DOCSCAN] user to persist: " user)
        (try (send-validation-email dest-id user "DOCSCAN")
             (catch Exception e (timbre/info (str "Error sending Docscan emails : " e))))
        (persist-to-state! session-id result))
      (timbre/info "users thus far: " @users))
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    "Thanks for the update Yoti"}))
