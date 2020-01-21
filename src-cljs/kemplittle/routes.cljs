(ns kemplittle.routes
  (:require
   [reitit.frontend :as rf]
   [reitit.coercion.schema :as rsc]
   [schema.core :as s]
   [kemplittle.pages.frontpage :as kf]
   [kemplittle.pages.docscan :refer [docscan-page]]
   [kemplittle.pages.admin :refer [show-dashboard-page
                                   show-admin-logs
                                   show-admin-users
                                   show-customize-email]]
   [kemplittle.components.utils :refer [not-found]]
   [kemplittle.state :as state]
   [kemplittle.pages.items :as ki]
   [taoensso.timbre :refer [info]]
   [xframe.core.alpha :as xf]
   [tailwind.core :refer [spit-css!]]
  ;  [kemplittle.components.yoti :refer [init-yoti-button]]
   ))

(defn log-fn [& params]
  (fn [_]
    (apply js/console.log params)))

(def routes
  (rf/router
   ["/"
    [""
     {:name ::frontpage
      :clear-flash true
      :view kf/front-page
      :parameters {:path {(s/optional-key :id) s/Int}
                   :query {(s/optional-key :ref) s/Keyword}}
      :controllers [{:parameters {:query [:path]}
                     :start (fn [a]
                              (js/console.log "start" "frontpage controller"))
                     :stop (fn [a]
                             (do
                               (.. js/window
                                   -Yoti
                                   -Share
                                   -destroy)
                               (log-fn "stop" "frontpage controller")))}]}]
    ["docscan"
     {:name ::docscan
      :parameters {:query {(s/optional-key :ref) s/Keyword}}
      :view docscan-page
      :controllers [{:params (fn [match]
                               (:query (:parameters match)))
                     :start
                     (fn [params]
                       (xf/dispatch [:fetch-session (:ref params)]))
                     :stop (log-fn "stop" "docscan controller")}]}]
    ["items"
      ;; Shared data for sub-routes
     {:view ki/item-page
      :controllers [{:start (log-fn "start" "items controller")
                     :stop (log-fn "stop" "items controller")}]}
     [""
      {:name ::item-list
       :controllers [{:start (log-fn "start" "item-list controller")
                      :stop (log-fn "stop" "item-list controller")}]}]
     ["/:id"
      {:name ::item
       :parameters {:path {(s/optional-key :id) s/Int}
                    :query {(s/optional-key :foo) s/Keyword}}
       :controllers [{:parameters {:path [:id]}
                      :start (fn [{:keys [path]}]
                               (js/console.log "start" "item controller" (:id path)))
                      :stop (fn [{:keys [path]}]
                              (js/console.log "stop" "item controller" (:id path)))}]}]]
    ["admin"
     {
      :name ::admin
      :clear-flash true
      :controllers [{:start (do
                              (log-fn "start" "admin-page controller"))
                     :stop (do
                             (log-fn "stop" "admin-page controller"))}]}
     [""
      {:view show-dashboard-page
       :controllers [{:start (log-fn "start" "admin-sections controller")
                      :stop (log-fn "stop" "admin-sections controller")}]}]
     ["/logs"
      {:name ::admin-logs
       :view show-admin-logs}]
     ["/users"
      {:name ::admin-users
       :view show-admin-users}]
     ["/email"
      {:name ::admin-email
       :view show-customize-email}]
     ]
    ["/not-found"
     {:name ::not-found
      :view not-found
      :controllers [{:start (log-fn "start" "error 404 controller")
                     :stop (do
                             (log-fn "stop" "error 404 controller"))}]}]]
   {:data {:controllers [{:start (log-fn "start" "root-controller")
                          :stop (do
                                  (log-fn "stop" "root controller")
                                  )}]
           :coercion rsc/coercion}}))

