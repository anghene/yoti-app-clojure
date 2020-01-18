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
                (info "db was got: " db)
                (update db assoc :flash
                        "new nil")))

(xf/reg-fx
 :http-post
 (fn [_ [_ {:keys [url form-data on-ok on-failed tkn]}]]
   (let [ctype {:Content-Type "application/json"}
         authheader (if tkn {:Authorization (str "Token " tkn)} nil)
         opts {:method "POST"
               :body (-> form-data bean/->js js/JSON.stringify)
               :headers (merge ctype authheader)
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
   (info "get via http with token: " tkn)
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
   {:http-post {:url (str "http://localhost:3000/login")
                :form-data {:username uname :password pass}
                :on-ok :fetch-admin-ok
                :on-failed :fetch-admin-failed}}))

(xf/reg-event-db
 :set-value
 (fn [db [_ value]]
   (assoc-in db [:repos :value] value)))

(xf/reg-event-fx
 :initiate-client-action
 (fn [db [_ token client-email client-name]]
   (info "initiate client action called got: " token)
   {:http-post {:url (str "http://localhost:3000/api/start-client")
                :form-data {:client-email client-email :client-name client-name}
                :on-ok :initiate-client-action-ok
                :on-failed :initiate-client-action-failed
                :tkn token}}))

(xf/reg-event-fx
 :fetch-admin-access
 (fn [db [_ token]]
   (info "fetch-admin-access called got: " token)
   {:http {:url (str "http://localhost:3000/api/get-access-level")
           :on-ok :fetch-admin-access-ok
           :on-failed :fetch-admin-access-failed
           :tkn token}}))

; (xf/reg-event-fx
;  :clear-flash
;  (fn [db [_ _]])
;  )

(xf/reg-event-fx
 :no-flash
 (fn [db [_ _]]
   (info "db was got: " db)
   {:db (assoc-in db [:app-state :flash]
                  nil)}))

(xf/reg-event-fx
 :fetch-admin-logs
 (fn [db [_ _]]
   (info "fetch-admin-logs gets token: " (-> db :app-state :admin :tkn))
   {:http {:url (str "http://localhost:3000/api/get-info")
           :on-ok :fetch-admin-logs-ok
           :on-failed :fetch-admin-logs-failed
           :tkn (-> db :app-state :admin :tkn)}}))

(xf/reg-event-fx
 :fetch-session
 (fn [db [_ ref]]
  ;  (info "fetch-session called with got: " (name ref))
   {:http {:url (str "http://localhost:3000/getsession?ref=" (name ref))
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
   (info "gets to failed with got: " error)
   (update db :app-state assoc
           :flash {:msg error
                   :type "error"}
           :admin {:tkn nil})))

(xf/reg-event-db
 :fetch-admin-logs-ok
 (fn [db [_ response]]
   (update db :app-state
           assoc :admin
           {:tkn (-> db :app-state :admin :tkn)
            :access (-> db :app-state :admin :access)
            :admin-page (:info response)})))

(xf/reg-event-db
 :fetch-admin-logs-failed
 (fn [db [_ response]]
   (do (info "fetch admin-logs triggered got: " response)
       (update db :app-state
               assoc
               :admin {:tkn nil}
               :flash {:msg (:message response)
                       :type "error"}))))

(xf/reg-event-db
 :fetch-admin-ok
 (fn [db [_ response]]
   (do (info "fetch admin ok triggered got: " response)
       (update db :app-state
               assoc :admin
               {:tkn (:token response)
                :access (xf/dispatch [:fetch-admin-access (:token response)])}
               :flash {:msg "Login successful"
                       :type "success"})
       )))

(xf/reg-event-db
 :fetch-admin-failed
 (fn [db [_ response]]
   (do (info "fetch admin failed triggered got: " response)
       (update db :app-state
               assoc
               :admin {:tkn nil}
               :flash {:msg (:message response)
                       :type "error"}))))

(xf/reg-event-db
 :initiate-client-action-ok
 (fn [db [_ response]]
   (do (info "initiate-client-action ok triggered got: " response)
       (update db :app-state
               assoc
               :flash {:msg (:message response)
                       :type "success"}))))

(xf/reg-event-db
 :initiate-client-action-failed
 (fn [db [_ response]]
   (do (info "initiate-client-action failed triggered got: " response)
       (update db :app-state
               assoc
               :flash {:msg (:message response)
                       :type "error"}))))

(xf/reg-event-db
 :fetch-admin-access-ok
 (fn [db [_ response tkn]]
   (do
     (info "fetch admin-access ok triggered got: " response)
     (update db :app-state
               (fn [old-db]
                 (let [access-added (assoc-in old-db [:admin :access]
                                              (:access response))]
                   (if (= "admin" (:access response))
                     (merge access-added (xf/dispatch [:fetch-admin-logs tkn]))
                     access-added)))))))

(xf/reg-event-db
 :fetch-admin-access-failed
 (fn [db [_ response]]
   (do (info "fetch admin-access failed triggered got: " response)
       (update db :app-state
               assoc
               :flash {:msg (:message response)
                       :type "error"}))))