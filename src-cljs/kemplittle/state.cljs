(ns kemplittle.state
  (:require
   [xframe.core.alpha :as xf :refer [<sub]]
   [reitit.frontend.controllers :as rfc]
   [kemplittle.routes :as rts :refer [routes log-fn]]))


(defn register-subs []
;; Subscriptions
  (xf/reg-sub :db/app-state
              (fn []
                (:app-state (xf/<- [::xf/db]))))

  (xf/reg-sub :error
              (fn []
                (:error (xf/<- [:db/app-state]))))

  (xf/reg-sub :current-page
              (fn []
                (:current-page (xf/<- [:db/app-state])))))

;; Event handlers
(defn register-events []
  (xf/reg-event-db
   :db/init
   (fn [_ _]
     {:app-state {:current-page {:data {:name :kemplittle.routes/frontpage
                                        :view kemplittle.pages.frontpage/front-page
                                        :controllers [{:start (log-fn "start" "frontpage controller")
                                                       :stop (log-fn "stop" "frontpage controller")}]}}
                  :error nil}}))

  (xf/reg-event-db
   :set-value
   (fn [db [_ value]]
     (assoc-in db [:repos :value] value)))

  (xf/reg-event-db
   :set-curr-page
   (fn [db [_ value]]
     (assoc-in db [:app-state :current-page]
               (assoc value
                      :controllers
                      (rfc/apply-controllers
                       (-> db :app-state :current-page :controllers)
                       value)))))

  (xf/reg-event-fx :fetch-repos
                   (fn [db [_ uname]]
                     {:db (assoc-in db [:repos :loading?] true)
                      :http {:url (str "https://api.github.com/users/" uname "/repos")
                             :on-ok :fetch-repos-ok
                             :on-failed :fetch-repos-failed}})))

  (defonce init-db
    (xf/dispatch [:db/init]))