(ns kemplittle.pages.frontpage
  (:require
   [tailwind.core :refer [tw tw! spit-css!]]
   [xframe.core.alpha :as xf :refer [<sub]]
   [cljsjs.emotion]
   [taoensso.timbre :refer [info]]
   [kemplittle.components.yoti :refer [scan-doc-button]]
   [kemplittle.components.utils :refer [header footer]]))

(defn front-page []
  [:div {:class (tw! "w-full m-auto p-6")}
   [:h1 {:class (tw! "text-lg font-semibold")} "Kemp Little Identity Verification"]
   [:p "As part of our client onboarding process, we need to confirm your identity and validate your identification document."]
   [:p "We use our identity partner Yoti to confirm your identity."]
   [:p "There are two methods that you can use."]
   [:ol {:class (tw! "p-2")}
    [:li "1. Unless you are already a Yoti user, please click on the “Doc Scan” link to follow a simple, fully secure identity verification process."]
    [:li "2. If you are already a user of the Yoti app, then click “Use Yoti” below to share your identity with us via the Yoti app."]]
   [:br]
   [:div {:class (tw! "flex flex-col items-center")}
    [scan-doc-button {:text "DOC SCAN" :id  "thisid" :tkn "thistkn"} ]
    [:div {:id "yotiapp"}]]
   [:br] [:br]]
  )

(defn main-page []
  (let [match (<sub [:current-page])
        error (<sub [:error])]
  [:<>
   [header]
   (when error
     [:div {:class (tw! "w-1/6 mt-6 m-auto bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative")
            :role "alert"}
      error])
   (if match
     (let [view (:view (:data match))]
         [view match]))
   [footer]]))

(spit-css! "resources/public/css/kl.css")
