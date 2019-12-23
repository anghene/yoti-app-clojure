(ns kemplittle.routes.updates
  (:require
   [taoensso.timbre :as timbre]
   [clojure.data.json :refer [read-json]]
   [kemplittle.db.core :refer [max-id users]]
   [kemplittle.client.session :refer [is-completed? text-check-id media-details]]
   [kemplittle.mail :as mail]))

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
  (let [webhook (read-json (slurp (:body req)))]
    (timbre/info "got params on webhook: " webhook)
    (when (is-completed? webhook)
      (let [session-id (:session_id webhook)
            media-id (text-check-id session-id webhook)
            user-profile (media-details session-id media-id)]
        (persist-to-state! session-id user-profile))
      (timbre/info "users thus far: " @users))
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    "Thanks for the update Yoti"}))