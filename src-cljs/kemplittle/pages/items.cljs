(ns kemplittle.pages.items
  (:require
   [reitit.frontend.easy :as rfe]
   [taoensso.timbre :refer [info]]
   [tailwind.core :refer [tw tw! ]]))

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



