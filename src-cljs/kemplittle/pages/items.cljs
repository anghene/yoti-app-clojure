(ns kemplittle.pages.items
  (:require
   [clojure.string :as str]
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix.core]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]))

(defn item-page [match]
  (let [{:keys [path query]} (:parameters match)
        {:keys [id]} path]
    [:div
     [:ul
      [:li [:a {:href (rfe/href :kemplittle.routes/item {:id 1})} "Item 1"]]
      [:li [:a {:href (rfe/href :kemplittle.routes/item {:id 2} {:foo "bar"})} "Item 2"]]]
     (if id
       [:h2 "Selected item " id])
     (if (:foo query)
       [:p "Optional foo query param: " (:foo query)])]))


