(ns kemplittle.components.yoti
  (:require
   [tailwind.core :refer [tw tw! spit-css!]]
   [xframe.core.alpha :as xf :refer [<sub]]
   [reitit.frontend.easy :as rfe]
   [cljsjs.emotion]
   [uix.core.alpha :as uix]
   [cljs-bean.core :as bean]
   [clojure.browser.dom :as dom]
   [taoensso.timbre :refer [info]]))

(defn yoti-logo []
  [:span {:class (str "yoti-button__logo " (tw! "mr-4"))}
   [:svg {:xmlns "http://www.w3.org/2000/svg", :class (tw! "fill-current h-auto w-auto") :preserveAspectRatio "xMidYMid meet" :viewBox "0 0 200 200"}
    [:title {} "Yoti - Your Digital Identity"]
    [:path {:d "M59.41,100.14A40.59,40.59,0,0,1,100,59.55V9.9H9.9V190.1H100V140.73A40.59,40.59,0,0,1,59.41,100.14Z", :style {"fill" "rgb(191, 194, 196)"}}]
    [:circle {:cx "100", :cy "100.14", :r "40.59", :style {"fill" "rgb(255, 255, 255)"}}]
    [:path {:d "M100,9.9V59.55a40.59,40.59,0,0,1,0,81.19V190.1h90.1V9.9H100Z", :style {"fill" "rgb(45, 159, 255)"}}]
    [:path {:d "M0,0V200H200V0H0ZM100,130.83a30.69,30.69,0,1,1,30.69-30.69A30.73,30.73,0,0,1,100,130.83ZM19.8,19.8H90.1V50.63a50.49,50.49,0,0,0,0,99V180.2H19.8V19.8ZM180.2,180.2H109.9V149.65a50.49,50.49,0,0,0,0-99V19.8h70.3V180.2Z", :style {"fill" "rgb(51, 59, 64)"}}]]])

(defn scan-doc-button [{:keys [text ref]}]
  [:div {:id "docscan" :style {"width" "300px"}
         :class (tw! "mb-6")}
   [:a {:href
        (rfe/href
         :kemplittle.routes/docscan
         {}
         {:ref ref})}
    [:div {:class (str "yoti-button-layout "
                       (tw! "items-center"))}
     [:button {:class (str "yoti-button "
                           (tw! "w-full"))}
      (yoti-logo)
      [:span {:class "yoti-button__text"} text]]]]])


(defn yoti-app-button [{:keys [ref unique-yoti-id]}]
  [:div {:id "yoti-app-btn"}
   [:div {:id "yotiapp"}]])

(defn first-init-yoti-button [uid]
  (let [tkn-handler (fn [token ref]
                      (.send goog.net.XhrIo
                             (str "yotiapp?token="
                                  token
                                  "&ref=" ref)))
        elements
        {:elements [{:domId uid
                     :scenarioId "15f775ff-b8c4-42e7-b8d1-3c2df8e9010c"
                     :clientSdkId "8d26c691-0b72-4ddd-9b33-59a79f51eb96"
                     :button {:label "Use Yoti"
                              :align "center"
                              :width "full"}

                     :modal {:zIndex 9999}
                     :shareComplete {:closeDelay 4000
                                     :tokenHandler tkn-handler}}]}
        init-payload (-> elements
                         bean/->js)]

    (info "Calling init-yoti-button with id: " uid)
    (.addEventListener
     js/window
     "DOMContentLoaded"
     (fn []
       (.. js/window
           -Yoti
           -Share (init
                   init-payload))))))

(defn subsequent-init-yoti-button [uid]
  (let [tkn-handler (fn [token ref]
                      (.send goog.net.XhrIo
                             (str "yotiapp?token="
                                  token
                                  "&ref=" ref)))
        elements
        {:elements [{:domId uid
                     :scenarioId "15f775ff-b8c4-42e7-b8d1-3c2df8e9010c"
                     :clientSdkId "8d26c691-0b72-4ddd-9b33-59a79f51eb96"
                     :button {:label "Use Yoti"
                              :align "center"
                              :width "full"}

                     :modal {:zIndex 9999}
                     :shareComplete {:closeDelay 4000
                                     :tokenHandler tkn-handler}}]}
        init-payload (-> elements
                         bean/->js)]

    (info "Calling init-yoti-button with id: " uid)
    (set! (.. js/window -Yoti -Share -uid) nil)
    (.. js/window
        -Yoti
        -Share (init
                init-payload))))
