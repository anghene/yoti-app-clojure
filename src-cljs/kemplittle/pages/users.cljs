(ns kemplittle.pages.users
  (:require
   [uix.core.alpha :as uix.core]
   ))

(defn show-user [id]
  (let [uid (uix.core/state nil)]
    [:div (str "user " (reset! uid id))]))