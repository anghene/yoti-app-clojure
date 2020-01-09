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
                                                     :stop nil}]}}}
                :flash nil
                :session nil
                :admin nil}))

;; Init database

(defonce init-db
  (do (info "initialized db.")
      (xf/dispatch [:db/init])))

;; Subscriptions
(xf/reg-sub :db/app-state
            (fn []
              (:app-state (xf/<- [::xf/db]))))

(xf/reg-sub :flash
            (fn []
              (:flash (xf/<- [:db/app-state]))))

(xf/reg-sub :current-page
            (fn []
              (:current-page (xf/<- [:db/app-state]))))

(xf/reg-sub :session
            (fn []
              (:session (xf/<- [:db/app-state]))))

(xf/reg-sub :admin-details
            (fn []
              (:admin (xf/<- [:db/app-state]))))

;; Effect handlers

(xf/reg-fx
 :clear-flash (fn [db [_ _]]
                (info "db was: " db)
                (update db assoc :flash
                        "new nil")))

(xf/reg-fx
 :http-post
 (fn [_ [_ {:keys [url form-data on-ok on-failed]}]]
   (let [opts {:method "POST"
               :body (-> form-data bean/->js js/JSON.stringify)
               :headers {:Content-Type "application/json"}
               :cors "no-cors"}
         promise (js/fetch url (bean/->js opts))
         status (.then promise #(.-ok %))]
     (.then promise
            (fn [response]
              (let [status-code (-> response .-status bean/->clj)]
                (if (.-ok response)
                  (-> promise
                      (.then #(.json %))
                      (.then bean/->clj)
                      (.then #(xf/dispatch [on-ok %])))
                  (-> promise
                      (.then #(.json %))
                      (.then bean/->clj)
                      (.then #(xf/dispatch [on-failed %]))))))))))

(xf/reg-fx
 :http
 (fn [_ [_ {:keys [url on-ok on-failed tkn]}]]
   (let [opts (if tkn
                {:headers {:Authorization (str "Token " tkn)}}
                nil)
         promise (js/fetch url (if (not (nil? opts))
                                 (bean/->js opts)))
         status (.then promise #(.-ok %))]
     (.then promise
            (fn [response]
              (let [status-code (-> response .-status bean/->clj)]
                (if (.-ok response)
                  (-> promise
                      (.then #(.json %))
                      (.then bean/->clj)
                      (.then #(xf/dispatch [on-ok %])))
                  (-> promise
                      (.then #(.json %))
                      (.then bean/->clj)
                      (.then #(xf/dispatch [on-failed %]))))))))))

;; Event handlers
(xf/reg-event-fx
 :fetch-admin-token
 (fn [db [_ uname pass]]
   {:http-post {:url (str "https://identity.kemplittle.com/login")
                :form-data {:username uname :password pass}
                :on-ok :fetch-admin-ok
                :on-failed :fetch-admin-failed}}))

(xf/reg-event-db
 :set-value
 (fn [db [_ value]]
   (assoc-in db [:repos :value] value)))

(xf/reg-event-fx
 :fetch-admin-page
 (fn [db [_ token]]
   (info "fetch-admin-page called: " token)
   {:http {:url (str "https://identity.kemplittle.com/api/get-info")
           :on-ok :fetch-adminpage-ok
           :on-failed :fetch-adminpage-failed
           :tkn token}}))

; (xf/reg-event-fx
;  :clear-flash
;  (fn [db [_ _]])
;  )

(xf/reg-event-fx
 :no-flash

   (fn [db [_ _]]
     (info "db was: " db)
    {:db (assoc-in db [:app-state :flash]
                   nil)}))

(xf/reg-event-fx
 :fetch-session
 (fn [db [_ _]]
   (info "fetch-session called")
   {:db (assoc-in db [:session :loading?] true)
    :http {:url (str "https://identity.kemplittle.com/getsession")
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

(xf/reg-event-db
 :nomatch-error
 (fn [db [_ value]]
   (update db assoc :flash
             {:msg "Not found"
              :type "error"})))

(xf/reg-event-db
 :fetch-session-ok
 (fn [db [_ response]]
   (update db :app-state
           assoc-in [:session :docscan]
           {:id (:id response)
            :tkn (:tkn response)})))

(xf/reg-event-db
 :fetch-session-failed
 (fn [db [_ error]]
   (info "gets to failed with: " error)
   (update db :app-state assoc
           :flash {:msg error
                   :type "error"}
           :admin {:tkn nil})))

(xf/reg-event-db
 :fetch-admin-ok
 (fn [db [_ response]]
   (do (info "fetch admin ok triggered: " response)
       (update db :app-state
               assoc :admin
               {:is-admin? true
                :tkn (:token response)}
               :flash {:msg "Login successful"
                       :type "success"}))))

(xf/reg-event-db
 :fetch-admin-failed
 (fn [db [_ response]]
   (do (info "fetch admin failed triggered: " response)
       (update db :app-state
               assoc
               :admin {:tkn nil
                       :is-admin? false}
               :flash {:msg (:message response)
                       :type "error"}))))

(xf/reg-event-db
 :fetch-adminpage-ok
 (fn [db [_ response]]
   (do (info "fetch admin-page ok triggered: " response)
       (update db :app-state
               assoc :admin
               {:greeting {:msg (:message response)
                           :type "success"}}))))

(xf/reg-event-db
 :fetch-adminpage-failed
 (fn [db [_ response]]
   (do (info "fetch admin-page failed triggered: " response)
       (update db :app-state
               assoc
               :flash {:msg (:message response)
                       :type "error"}))))