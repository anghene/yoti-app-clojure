(ns kemplittle.pages.items
  (:require
   [clojure.string :as str]
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix.core]
   [reitit.frontend.easy :as rfe]
   [tailwind.core :refer [tw tw! spit-css!]]
   [reagent.core :as r]))

(defn item-page [match]
  (let [{:keys [path query]} (:parameters match)
        {:keys [id]} path]
    [:div {:class (tw! "w-1/2 m-auto p-6")}
     [:ul
      [:li [:a {:href (rfe/href :kemplittle.routes/item {:id 1})} "Item 1"]]
      [:li [:a {:href (rfe/href :kemplittle.routes/item {:id 2} {:foo "bar"})} "Item 2"]]]
     (if id
       [:h2 "Selected item " id])
     (if (:foo query)
       [:p "Optional foo query param: " (:foo query)])]))


