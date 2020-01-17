(ns kemplittle.main
  (:require
   [cljsjs.emotion]
   [reitit.frontend.easy :as rfe]
   [kemplittle.routes :refer [routes]]
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   [taoensso.timbre :refer [info]]
   [xframe.core.alpha :as xf :refer [<sub]]
   [tailwind.core :refer [tw tw! spit-css!]]))

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
(defn init! []
  (rfe/start!
   routes
   (fn [new-match]
     (xf/dispatch [:setpage
                   new-match]))
   {:use-fragment true})
  (info "router started"))

(init!)

(defn app []
  [kemplittle.pages.frontpage/main-page])

(uix/add-transform-fn css-uix-transform)
(uix.dom/render [app] js/root)

