(ns kemplittle.pages.admin
  (:require
   [tailwind.core :refer [tw tw! spit-css!]]
   [uix.core.alpha :as uix]
   [taoensso.timbre :refer [info]]
   [xframe.core.alpha :as xf]
   [xframe.core.alpha :as xf :refer [<sub]])
  )

(do
  (deftype Cursor [ref path]
    IDeref
    (-deref [o]
      (get-in @ref path))
    IReset
    (-reset! [o new-value]
      (swap! ref update-in path (constantly new-value)))))

(defn derive-state [ref path]
  (uix/memo #(Cursor. ref path) [ref path]))

(defn secretary-panel [adm-details]
  (let [state* (uix/state {:client-email "" :client-name ""})
        client-email* (derive-state state* [:client-email])
        client-name* (derive-state state* [:client-name])
        token (-> adm-details :tkn)]
    [:div
     {:class "w-1/2 max-w-xs m-auto"}
     [:p {:class (tw! "pt-10 text-gray-700")}
      (str "Hello. Here you can initiate the validation process.")]
     [:form {:class (tw! "bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4")
             :on-submit #(do
                           (.preventDefault %)
                           (xf/dispatch [:initiate-client-action token @client-email* @client-name*]))}
      [:div {:class (tw! "mb-4")}
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "client-name"} "Client Name"]
       [:input {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                :id          "client-name"
                :type        "text"
                :placeholder "Client Name"
                :value @client-name*
                :on-change #(reset! client-name* (.. % -target -value))}]
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "client-email"} "Client Email"]
       [:input {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                :id          "client-email"
                :type        "text"
                :placeholder "Client Email"
                :value @client-email*
                :on-change #(reset! client-email* (.. % -target -value))}]]
      [:div {:class (tw! "flex items-center justify-between")}
       [:button {:class (tw! "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline")
                 :type  "submit"} "Send email"]]]]))

(defn admin-panel [adm-det]
  (let [greeting (:greeting adm-det)]
    (info "adm-det: " adm-det)
    [:div
     [:p "This is the admin panel"]]))

(defn login-form [adm-details]
  (let [state* (uix/state {:user "" :pass ""})
        user* (derive-state state* [:user])
        pass* (derive-state state* [:pass])
        token (get adm-details [:tkn])
        ]
    (info "token is: " token)
    (if token
      (xf/dispatch [:fetch-admin-access token])
      [:div
       {:class "w-1/6 max-w-xs m-auto"}
       [:form {:class (tw! "bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4")
               :on-submit #(do
                             (.preventDefault %)
                             (xf/dispatch [:fetch-admin-token @user* @pass*]))}
        [:div {:class (tw! "mb-4")}
         [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                  :for   "username"} "Username"]
         [:input {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                  :id          "username"
                  :type        "text"
                  :placeholder "Username"
                  :value @user*
                  :on-change #(reset! user* (.. % -target -value))}]]
        [:div {:class (tw! "mb-6")}
         [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                  :for   "password"} "Password"]
         [:input {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline")
                  :id          "password"
                  :type        "password"
                  :placeholder "******************"
                  :value @pass*
                  :on-change #(reset! pass* (.. % -target -value))}]]
        [:div {:class (tw! "flex items-center justify-between")}
         [:button {:class (tw! "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline")
                   :type  "submit"} "Sign In"]]]])))

(defn show-dashboard-page []
  (let [adm-det (<sub [:admin-details])
        usr-access (:access adm-det)]
    (if (nil? usr-access)
        [:div {:class (tw! "m-20")}
         [login-form adm-det]]
        (case usr-access
          "secretary" [secretary-panel adm-det]
          "admin" [admin-panel adm-det]))))

