(ns kemplittle.main
  (:require
   [cljsjs.emotion]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend.controllers :as rfc]
   [kemplittle.routes :as rts :refer [routes log-fn]]
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   [taoensso.timbre :refer [info]]
   [xframe.core.alpha :as xf :refer [<sub]]
   [tailwind.core :refer [tw tw! spit-css!]]))

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

;; Event handlers
(xf/reg-event-db
 :db/init
 (fn [_ _]
   {:app-state {:current-page {:data {:name :kemplittle.routes/frontpage
                                      :view kemplittle.pages.frontpage/front-page
                                      :controllers [{:start (log-fn "start" "frontpage controller")
                                                     :stop (log-fn "stop" "frontpage controller")}]}}
                :error nil}}))
;; Init database
(spit-css! "resources/public/css/kl.css")

(defonce init-db
  (do (info "initializing db.")
      (xf/dispatch [:db/init])))

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
                           :on-failed :fetch-repos-failed}}))

;; from uix.recipes.dynamic-styles
(defn css-uix-transform [attrs]
  (if-not (contains? attrs :css)
    attrs
    (let [classes (:class attrs)
          css     (:css attrs)
          class   (->> (clj->js css)
                       js/emotion.css
                       (str classes " "))]
      (-> (dissoc attrs :css)
          (assoc :class class)))))

;; UI components

(defn app []
  (let [match (<sub [:current-page])
        error (<sub [:error])]
    (info "match: " match)
    [:<>
     [:div {:class (tw! "flex flex-col items-center")}
      [kemplittle.pages.frontpage/header]]
     (when error
       [:div {:class (tw! "w-1/6 m-auto bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative") :role "alert"}
        error])
     (if match
       (let [view (:view (:data match))]
         [view match]))
     [kemplittle.pages.frontpage/footer]]))

(defn init! []
  (rfe/start!
   routes
   (fn [new-match]
     (xf/dispatch [:set-curr-page
                   new-match]))
   {:use-fragment true})
  (uix/add-transform-fn css-uix-transform)
  (uix.dom/render [app] js/root))

(init!)