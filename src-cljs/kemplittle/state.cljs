(ns kemplittle.state
  (:require
   [xframe.core.alpha :as xf :refer [<sub]]
   [reitit.frontend.controllers :as rfc]
   [taoensso.timbre :refer [info]]
   [kemplittle.pages.frontpage :as fp]
   [cljs-bean.core :as bean]))

;; Event handlers
(xf/reg-event-db
 :db/init
 (fn [_ _]
   {:app-state {:current-page {:data {:name :kemplittle.routes/frontpage
                                      :view fp/main-page
                                      :controllers [{:start nil
                                                     :stop nil}]}}
                :error nil
                :session nil}}))

;; Init database

(defonce init-db
  (do (info "initialized db.")
      (xf/dispatch [:db/init])))

;; Subscriptions
(xf/reg-sub :db/app-state
            (fn []
              (:app-state (xf/<- [::xf/db]))))

(xf/reg-sub :error
            (fn []
              (:error (xf/<- [:db/app-state]))))

(xf/reg-sub :current-page
            (fn []
              (:current-page (xf/<- [:db/app-state]))))

(xf/reg-sub :session
            (fn []
              (:session (xf/<- [:db/app-state]))))

;; Effect handlers
(xf/reg-fx
 :http
 (fn [_ [_ {:keys [url on-ok on-failed]}]]
   (-> (js/fetch url)
       (.then #(if (.-ok %)
                 (.json %)
                 (xf/dispatch [on-failed %])))
       (.then bean/->clj)
       (.then #(xf/dispatch [on-ok %])))))

;; Event handlers
(xf/reg-event-db
 :set-value
 (fn [db [_ value]]
   (assoc-in db [:repos :value] value)))

(xf/reg-event-fx
 :fetch-session
 (fn [db [_ _]]
   (info "fetch-session called")
   {:db (assoc-in db [:session :loading?] true)
    :http {:url (str "http://localhost:3000/getsession")
           :on-ok :fetch-session-ok
           :on-failed :fetch-session-failed}}))

(xf/reg-event-db
 :setpage
 (fn [db [_ value]]
   (assoc-in db [:app-state :current-page]
             (assoc value
                    :controllers
                    (rfc/apply-controllers
                     (-> db :app-state :current-page :controllers)
                     value)))))

(xf/reg-event-db :fetch-session-ok
                 (fn [db [_ session]]
                     (info session)
                     (update db :app-state
                             assoc :session
                             {:id (:id session)
                              :tkn (:tkn session)
                              :error nil})))

(xf/reg-event-db :fetch-session-failed
                 (fn [db [_ error]]
                   (update db :error error)))
