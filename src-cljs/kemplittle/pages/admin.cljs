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

(defn login-form []
  (let [state* (uix/state {:user "" :pass ""})
        user* (derive-state state* [:user])
        pass* (derive-state state* [:pass])
        adm-det (<sub [:admin-details])
        error (<sub [:error])
        greeting (:greeting adm-det)
        is-admin? (:is-admin? adm-det)
        auth-token (:tkn adm-det)
        ]
    (info "error: " error)
    (info "token: " auth-token)
    (info "is-admin?: " is-admin?)
    [:div
     {:class "w-1/6 max-w-xs m-auto"}
     [:form {:class (tw! "bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4")
             :on-submit #(do
                           (.preventDefault %)
                           (xf/dispatch [:fetch-admin-token @user* @pass*]))}
      (if is-admin?
          (xf/dispatch
           [:fetch-admin-page
            auth-token]))
      [:div greeting]
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
                :on-change #(reset! pass* (.. % -target -value))}]
       ]
      [:div {:class (tw! "flex items-center justify-between")}
       [:button {:class (tw! "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline")
                 :type  "submit"} "Sign In"]
       ]]]))