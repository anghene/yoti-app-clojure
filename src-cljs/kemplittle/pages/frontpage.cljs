(ns kemplittle.pages.frontpage
  (:require
   [tailwind.core :refer [tw tw! spit-css!]]
   [xframe.core.alpha :as xf :refer [<sub]]
   [cljsjs.emotion]
   [taoensso.timbre :refer [info]]
   [kemplittle.components.yoti :refer [yoti-instantiated destroy]]
   [kemplittle.pages.docscan :refer [docscan-button]]
   [kemplittle.components.utils :refer [header footer flash-div not-found]]
   [clojure.browser.dom :as dom]))

(defn front-page [match]
  (let [{:keys [query]} (:parameters match)
        uid (<sub [:unique-yoti-id])]
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
      [docscan-button {:text "DOC SCAN"
                       :ref-id (-> query :ref)
                       :uuid (-> query :uuid)}]
      [:div {:id "yotiapp"}]
      ]]))

(defn main-page []
  (let [match (<sub [:current-page])
        app-state (<sub [:db/app-state])
        flash (<sub [:flash])
        ]
    ; (info "match: " match)
    (info "app-state: " app-state)
    [:<>
     [header (-> app-state :admin :access)]
     (when flash
      ;  (info "flash: " flash)
       (flash-div flash))
     (if match
       (let [view (:view (:data match))]
         (if (and
              flash
              (-> match
                  :data
                  :clear-flash))
           (js/setTimeout
            (fn [_] (xf/dispatch [:no-flash]))
            3000))
         (if-not view
           [not-found match]
           [view match])))
     [footer]]))

(spit-css! "resources/public/css/frontpage.css")