(ns kemplittle.pages.frontpage
  (:require
   [tailwind.core :refer [tw tw! spit-css!]]
   [xframe.core.alpha :as xf :refer [<sub]]
   [cljsjs.emotion]
   [taoensso.timbre :refer [info]]
   [kemplittle.components.yoti :refer [scan-doc-button yoti-app-button first-init-yoti-button]]
   [kemplittle.components.utils :refer [header footer flash-div]]
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
      [scan-doc-button {:text "DOC SCAN" :ref (-> query :ref)}]
      [yoti-app-button {:ref (-> query :ref) :unique-yoti-id uid}]]
     (do
       (first-init-yoti-button uid)
       (info "ran init on yoti button :" uid))
     [:br] [:br]]))

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
         [view match]))
     [footer]]))

