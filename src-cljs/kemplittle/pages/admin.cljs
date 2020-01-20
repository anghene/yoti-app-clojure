(ns kemplittle.pages.admin
  (:require
   [tailwind.core :refer [tw tw! spit-css!]]
   [uix.core.alpha :as uix]
   [taoensso.timbre :refer [info]]
   [xframe.core.alpha :as xf]
   [xframe.core.alpha :as xf :refer [<sub]]))


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
  (let [state* (uix/state {:client-email ""
                           :client-name ""
                           :add-msg ""})
        client-email* (derive-state state* [:client-email])
        client-name* (derive-state state* [:client-name])
        add-msg* (derive-state state* [:add-msg])
        token (-> adm-details :tkn)]
    [:div
     {:class (tw! "w-1/2 m-auto")}
     [:p {:class (tw! "pt-10 text-gray-700")}
      (str "Hello. Here you can initiate the validation process.")]
     [:form {:class (tw! "bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4")
             :on-submit #(do
                           (.preventDefault %)
                           (reset! state*  {:client-email ""
                                            :client-name ""
                                            :add-msg ""})
                           (xf/dispatch [:initiate-client-action token @client-email* @client-name* @add-msg*]))}
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
                :on-change #(reset! client-email* (.. % -target -value))}]
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "add-msg"} "Editable Section / Free Text"]
       [:textarea {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                   :id          "add-msg"
                   :type        "text"
                   :rows "10" :cols "50"
                   :placeholder "Editable Section / Free Text"
                   :value @add-msg*
                   :on-change #(reset! add-msg* (.. % -target -value))}]]
      [:div {:class (tw! "flex items-center justify-between")}
       [:button {:class (tw! "bg-blue-500 hover:bg-blue-700 text-white font-bold my-4 py-2 px-4 rounded focus:outline-none focus:shadow-outline")
                 :type  "submit"} "Send email"]]]]))

(defn p-text [text]
  [:div {:class (str (tw! "mb-4"))}
   [:div {:class (str (tw! "flex items-center justify-between
                               bg-gray-200 pl-3 pr-2 py-3 w-full rounded
                               text-gray-600 font-bold cursor-pointer
                              hover:bg-gray-300"))}
          ; :on-click #(xf/dispatch [:set-flash {:msg "Clicked"
          ;                                      :type "success"}])

    [:p (str text)]]])

(defn para-text [text]
  (p-text (first text)))

(defn draw-user-row [user]
  [:tr {}
   [:td {:class (str (tw! "border px-4 py-2"))} (:id user)]
   [:td {:class (str (tw! "border px-4 py-2"))} (:session user)]
   [:td {:class (str (tw! "border px-4 py-2"))} (:name user)]
   [:td {:class (str (tw! "border px-4 py-2"))} (:type user)]
   [:td {:class (str (tw! "border px-4 py-2"))} (:user-details user)]])

(defn draw-table [users]
  [:table {:class (str (tw!  "m-auto table-auto"))}
   [:thead {}
    [:tr {}
     [:th {:class (str (tw! "px-4 py-2"))} "ID"]
     [:th {:class (str (tw! "px-4 py-2"))} "Session"]
     [:th {:class (str (tw! "px-4 py-2"))} "Name"]
     [:th {:class (str (tw! "px-4 py-2"))} "Type"]
     [:th {:class (str (tw! "px-4 py-2"))} "User-details"] ] ]
   [:tbody {}
    (map draw-user-row users)]])

(defn customize-email-panel [opts]
  (let [state* (uix/state (merge {} opts))
        opt1-above-a (derive-state state* [:opt1 :desc :above :a])
        opt1-above-b (derive-state state* [:opt1 :desc :above :b])
        opt1-text-a (derive-state state* [:opt1 :text :a])
        opt1-text-b (derive-state state* [:opt1 :text :b])
        opt1-below-a (derive-state state* [:opt1 :desc :below :a])
        opt1-below-b (derive-state state* [:opt1 :desc :below :b])
        opt2-above-a (derive-state state* [:opt2 :desc :above :a])
        opt2-above-b (derive-state state* [:opt2 :desc :above :b])
        opt2-text-a (derive-state state* [:opt2 :text :a])
        opt2-text-b (derive-state state* [:opt2 :text :b])
        opt2-below-a (derive-state state* [:opt2 :desc :below :a])
        opt2-below-b (derive-state state* [:opt2 :desc :below :b])
        opt3-above-a (derive-state state* [:opt3 :desc :above :a])
        opt3-above-b (derive-state state* [:opt3 :desc :above :b])
        opt3-text-a (derive-state state* [:opt3 :text :a])
        opt3-text-b (derive-state state* [:opt3 :text :b])
        opt3-below-a (derive-state state* [:opt3 :desc :below :a])
        opt3-below-b (derive-state state* [:opt3 :desc :below :b])
        opt4-above-a (derive-state state* [:opt4 :desc :above :a])
        opt4-above-b (derive-state state* [:opt4 :desc :above :b])
        opt4-text-a (derive-state state* [:opt4 :text :a])
        opt4-text-b (derive-state state* [:opt4 :text :b])
        opt4-below-a (derive-state state* [:opt4 :desc :below :a])
        opt4-below-b (derive-state state* [:opt4 :desc :below :b])
        opt5-above-a (derive-state state* [:opt5 :desc :above :a])
        opt5-above-b (derive-state state* [:opt5 :desc :above :b])
        opt5-text-a (derive-state state* [:opt5 :text :a])
        opt5-text-b (derive-state state* [:opt5 :text :b])
        opt5-below-a (derive-state state* [:opt5 :desc :below :a])
        opt5-below-b (derive-state state* [:opt5 :desc :below :b])
        opt6-above-a (derive-state state* [:opt6 :desc :above :a])
        opt6-above-b (derive-state state* [:opt6 :desc :above :b])
        opt6-text-a (derive-state state* [:opt6 :text :a])
        opt6-text-b (derive-state state* [:opt6 :text :b])
        opt6-below-a (derive-state state* [:opt6 :desc :below :a])
        opt6-below-b (derive-state state* [:opt6 :desc :below :b])
        ]
    [:div
     {:class (tw! "w-1/2 m-auto")}
     [:p {:class (tw! "pt-10 text-gray-700")}
      (str @state*)]
     [:form {:class (tw! "bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4")
             :on-submit #(do
                           (.preventDefault %)
                           #_(reset! state*  {:client-email ""
                                              :client-name ""
                                              :add-msg ""})
                           )}
      [:div {:class (tw! "mb-4")}
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "opt1-above-desc-a"} "UK Company option"]
       [:textarea {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                   :id          "opt1-above-desc-a"
                   :type        "text"
                   :placeholder "Option 1"
                   :value @opt1-above-a
                   :on-change #(reset! opt1-above-a (.. % -target -value))}]
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "client-name"} "[client name]"]
       [:textarea {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                   :id          "opt2-above-desc-a"
                   :type        "text"
                   :placeholder "Option 2"
                   :value @opt1-above-b
                   :on-change #(reset! opt1-above-b (.. % -target -value))}]
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "client-name"} "[psc-names]"]
       [:textarea {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                   :id          "opt2-below-desc-a"
                   :type        "text"
                   :placeholder "Option 2"
                   :value @opt1-below-a
                   :on-change #(reset! opt1-below-a (.. % -target -value))}]
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "client-name"} "[client-name]"]
       [:textarea {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                   :id          "opt2-below-desc-b"
                   :type        "text"
                   :placeholder "Option 2"
                   :value @opt1-below-b
                   :on-change #(reset! opt1-below-b (.. % -target -value))}]]
      [:div {:class (tw! "mb-4")}
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "opt1-above-desc-a"} "UK Company option"]
       [:textarea {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                   :id          "opt1-above-desc-a"
                   :type        "text"
                   :placeholder "Option 1"
                   :value @opt2-above-a
                   :on-change #(reset! opt2-above-a (.. % -target -value))}]
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "client-name"} "[client name]"]
       [:textarea {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                   :id          "opt2-above-desc-a"
                   :type        "text"
                   :placeholder "Option 2"
                   :value @opt2-above-b
                   :on-change #(reset! opt2-above-b (.. % -target -value))}]
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "client-name"} "[psc-names]"]
       [:textarea {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                   :id          "opt2-below-desc-a"
                   :type        "text"
                   :placeholder "Option 2"
                   :value @opt2-below-a
                   :on-change #(reset! opt2-below-a (.. % -target -value))}]
       [:label {:class (tw! "block text-gray-700 text-sm font-bold mb-2")
                :for   "client-name"} "[client-name]"]
       [:textarea {:class (tw!       "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline")
                   :id          "opt2-below-desc-b"
                   :type        "text"
                   :placeholder "Option 2"
                   :value @opt2-below-b
                   :on-change #(reset! opt2-below-b (.. % -target -value))}]]]]
    ))

(defn show-customize-email []
  (let [adm-det (<sub [:admin-details])
        usr-access (:access adm-det)
        opts (:custom-email-page adm-det)]
    (case usr-access
      "admin" (customize-email-panel opts)
      "secretary" [:div  {:class (tw! "w-1/2 m-auto")}
                   "Only admins have access to emails."]
      [:div  {:class (tw! "w-1/2 m-auto")}
       "You are not logged in."])))

(defn show-admin-users []
  (let [adm-det (<sub [:admin-details])
        usr-access (:access adm-det)
        users (-> adm-det :users-page)
        ; users [{:name "Vlad"
        ;         :id 1
        ;         :type "yoti"
        ;         :user-details "Blabla"}
        ;        {:name "Alex"
        ;         :id 2
        ;         :type "docscan"
        ;         :user-details "Blablalablalablalablalabla"}]
        users-server [{0 "d942c552-2445-46d5-ab1a-afc4a3d863e9",
                       :type :docscan,
                       :user-details "Error: clojure.lang.ExceptionInfo: clj-http: status 404"}]

        ]
    [:div {:class (tw! "whitespace-pre-wrap")}
     [:div {:class (str (tw!  "bg-white mx-auto p-6"))}
      (case usr-access
        "admin" (draw-table users)
        "secretary" [:div {:class (tw! "w-1/2 m-auto")}
                     "Only admins have access to users."]
        [:div  {:class (tw! "w-1/2 m-auto")}
         "You are not logged in."])]]))


(defn show-admin-logs [log]
  (let [adm-det (<sub [:admin-details])
        usr-access (:access adm-det)
        log (-> adm-det :log-page)]
    [:div {:class (tw! "whitespace-pre-wrap")}
     [:div {:class (str (tw!  "bg-white mx-auto p-6"))}
      (case usr-access
        "admin" (map para-text log)
        "secretary" [:div  {:class (tw! "w-1/2 m-auto")}
                     "Only admins have access to logs."]
        [:div  {:class (tw! "w-1/2 m-auto")}
         "You are not logged in."])]]))

(defn admin-panel [adm-det]
  [:div {:class (tw! "whitespace-pre-wrap")}
   [:div {:class (str (tw!  "bg-white mx-auto p-6"))}
    "Welcome Admin"]])

(defn login-form [adm-details]
  (let [state* (uix/state {:user "" :pass ""})
        user* (derive-state state* [:user])
        pass* (derive-state state* [:pass])
        token (get adm-details [:tkn])]

    (info "adm details: " adm-details)
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

(defn show-dashboard-page [match]
  (let [adm-det (<sub [:admin-details])
        usr-access (:access adm-det)]
    (if (nil? usr-access)
      [:div {:class (tw! "m-20")}
       [login-form adm-det]]
      (case usr-access
        "secretary" [secretary-panel adm-det]
        "admin" [admin-panel adm-det]
        [:div "Unknown user level"]))))

