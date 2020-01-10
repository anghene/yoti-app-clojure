(ns kemplittle.pages.frontpage
  (:require
   [tailwind.core :refer [tw tw! spit-css!]]
   [xframe.core.alpha :as xf :refer [<sub]]
   [cljsjs.emotion]
   [taoensso.timbre :refer [info]]
   [kemplittle.components.yoti :refer [scan-doc-button]]
   [kemplittle.components.utils :refer [header footer]]))

(defn front-page [match]
  (let [{:keys [query]} (:parameters match)
        ]
    [:div {:class (tw! "w-full m-auto p-6")}
     [:h1 {:class (tw! "text-lg font-semibold")} (str "Kemp Little Identity Verification")]
     [:p "As part of our client onboarding process, we need to confirm your identity and validate your identification document."]
     [:p "We use our identity partner Yoti to confirm your identity."]
     [:p "There are two methods that you can use."]
     [:ol {:class (tw! "p-2")}
      [:li "1. Unless you are already a Yoti user, please click on the “Doc Scan” link to follow a simple, fully secure identity verification process."]
      [:li "2. If you are already a user of the Yoti app, then click “Use Yoti” below to share your identity with us via the Yoti app."]]
     [:br]
     [:div {:class (tw! "flex flex-col items-center")}
      [scan-doc-button {:text "DOC SCAN" :ref (-> query :ref)}]
      [:div {:id "yotiapp"}]]
     [:br] [:br]]))

(defn flash-div [flash]
  [:div {:class
         (str
          (if (= "error" (:type flash))
            (tw! "bg-red-100 border-red-400 text-red-700 "))
          (if (= "success" (:type flash))
            (tw! "bg-green-100 border-green-400 text-green-700 "))
          " "
          (tw! " w-1/6 mt-6 m-auto border px-4 py-3 rounded relative"))
         :role "alert"}
   (:msg flash)
   [:span {:class (tw! "absolute top-0 bottom-0 right-0 px-4 py-3")
           :on-click (fn[] (xf/dispatch [:no-flash]))}
    [:svg {:class (tw!  "fill-current h-6 w-6 text-red-500")
           :role "button"
           :xmlns "http://www.w3.org/2000/svg"
           :viewBox= "0 0 20 20"}
     [:title "Close"]
     [:path {:d
             "M14.348 14.849a1.2 1.2 0 0 1-1.697 0L10 11.819l-2.651 3.029a1.2 1.2 0 1 1-1.697-1.697l2.758-3.15-2.759-3.152a1.2 1.2 0 1 1 1.697-1.697L10 8.183l2.651-3.031a1.2 1.2 0 1 1 1.697 1.697l-2.758 3.152 2.758 3.15a1.2 1.2 0 0 1 0 1.698z"}]]
    ]])

(defn main-page []
  (let [match (<sub [:current-page])
        app-state (<sub [:db/app-state])
        flash (<sub [:flash])
        ]
    ; (info "match: " match)
    (info "app-state: "(dissoc app-state :current-page))
    [:<>
     [header]
     (when flash
      ;  (info "flash: " flash)
       (flash-div flash))
     (if match
       (let [view (:view (:data match))]
         [view match]))
     [footer]]))

(spit-css! "resources/public/css/kl.css")
