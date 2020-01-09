(ns kemplittle.routes
  (:require
   [reitit.frontend :as rf]
   [reitit.coercion.schema :as rsc]
   [schema.core :as s]
   [kemplittle.pages.frontpage :as kf]
   [kemplittle.pages.docscan :refer [docscan-page]]
   [kemplittle.pages.admin :refer [show-admin-page]]
   [kemplittle.components.utils :refer [not-found]]
   [kemplittle.state :as state]
   [kemplittle.pages.items :as ki]
   [xframe.core.alpha :as xf]))

(defn log-fn [& params]
  (fn [_]
    (apply js/console.log params)))

(def routes
  (rf/router
   ["/"
    [""
     {:name ::frontpage
      :view kf/front-page
      :controllers [{:start (do
                              (log-fn "start" "frontpage controller")
                              )
                     :stop (log-fn "stop" "frontpage controller")
                     }]}]
    ["docscan"
     {:name ::docscan
      :view docscan-page
      :controllers [{:start
                     (xf/dispatch [:fetch-session])
                     :stop (log-fn "stop" "docscan controller")
                     }]}]
    ["items"
      ;; Shared data for sub-routes
     {:view ki/item-page
      :controllers [{:start (log-fn "start" "items controller")
                     :stop (log-fn "stop" "items controller")
                     }]}
     [""
      {:name ::item-list
       :controllers [{:start (log-fn "start" "item-list controller")
                      :stop (log-fn "stop" "item-list controller")
                      }]}]
     ["/:id"
      {:name ::item
       :parameters {:path {:id s/Int}
                    :query {(s/optional-key :foo) s/Keyword}}
       :controllers [{:parameters {:path [:id]}
                      :start (fn [{:keys [path]}]
                               (js/console.log "start" "item controller" (:id path)))
                      :stop (fn [{:keys [path]}]
                              (js/console.log "stop" "item controller" (:id path)))}]}]]
    ["admin"
     {:name ::admin
      :view show-admin-page
      :controllers [{:start (do
                              (xf/dispatch [:no-flash])
                              (log-fn "start" "admin-page controller"))
                     :stop (do
                             (xf/dispatch [:no-flash])
                             (log-fn "stop" "admin-page controller")
                             )
                     }]}]
    ["/not-found"
     {:name ::not-found
      :view not-found
      :controllers [{:start (log-fn "start" "error 404 controller")
                     :stop (do
                             (log-fn "stop" "error 404 controller"))}]}]
    ]
   {:data {:controllers [{:start (log-fn "start" "root-controller")
                          :stop (do
                                  (log-fn "stop" "root controller")
                                  )}]
           :coercion rsc/coercion}}))