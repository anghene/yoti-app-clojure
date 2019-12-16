(ns kemplittle.routes.updates
  (:require
   [taoensso.timbre :as timbre]
   ))

(defn is-admin? [name pass]
  (and (= name "yotiupdate")
       (= pass "3HHXmXZJfJT1fX@Yd6#r2%0nG")))

(defn authenticated? [name pass]
  (if (is-admin? name pass)
    (do
        ; set some state
      true)
    false))

(defn notification-handler [req]
  (timbre/info "got update: " req))