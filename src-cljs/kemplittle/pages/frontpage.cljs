(ns kemplittle.pages.frontpage
  (:require
   [tailwind.core :refer [tw tw! spit-css!]]
   [reitit.frontend.easy :as rfe]
   [cljsjs.emotion]
   [taoensso.timbre :refer [info]]
   ))

(defn header []
  [:nav {:class (tw! "w-full flex items-center justify-between flex-wrap bg-vlad-100 p-6")}
   [:div {:class (tw! "flex items-center flex-shrink-0 text-white mr-6")}
    [:svg {:class   (tw! "fill-current h-auto w-auto mr-2")
           :width   "50"
           :height  "50"
           :viewBox "0 0 217 218"
           :xmlns   "http://www.w3.org/2000/svg"}
     [:path {:d "M 108.50001,218.23093 H 3.1882316e-6 V 109.23094 0.23093892 H 108.50001 217 V 109.23094 218.23093 Z m 0,-4 H 213 V 109.23094 4.2309389 H 108.50001 4.0000082 V 109.23094 214.23093 Z M 43.000008,173.23094 h -13 v -18 -17.99999 h 3.5 3.5 v 14.49999 14.5 h 9.5 9.5 v 3.5 3.5 z m 19,0 h -4 v -18 -17.99999 h 4 4 v 17.99999 18 z m 21,0 h -4 v -14.5 -14.49999 h -5.5 -5.5 v -3.5 -3.5 h 15 15 v 3.5 3.5 h -5.5 -5.5 v 14.49999 14.5 z m 31.500002,0 h -3.5 v -14.5 -14.49999 h -5.5 -5.5 v -3.5 -3.5 h 14.5 14.5 v 3.5 3.5 h -5.5 -5.5 v 14.49999 14.5 z m 30,0 h -12.5 v -18 -17.99999 h 3.5 3.5 v 14.49999 14.5 h 9 9 v 3.5 3.5 z m 28.99999,0 h -13.49999 v -18 -17.99999 H 173 186 v 3.5 3.5 h -9.5 -9.5 v 4 3.99999 h 8.5 8.5 v 3 3 h -8.5 -8.5 v 4.5 4.5 h 10 10 v 3 3 z m -64.99999,-62 H 72.000008 v -2 -2 h 36.500002 36.5 v 2 2 z M 46.500008,81.230939 h -3.5 v -18 -18 h 3.5 3.5 v 8.05936 8.05935 l 7.75,-8.14111 7.692267,-7.94583 h 8.50058 l -6.917547,8.10213 -6.97529,7.63391 5.15639,6.86609 5.15639,6.8661 2.43729,3.25 2.43729,3.25 h -4.51277 -4.51277 l -5.76417,-7.52564 -5.76418,-7.52564 -2.34174,2.19995 -2.34174,2.19995 v 5.32569 5.32569 z m 44,0 h -13.5 v -18 -18 h 13.5 13.500002 v 3 3 h -10.000002 -10 v 4.5 4.5 h 8.5 8.500002 v 3 3 h -8.500002 -8.5 v 4 4 h 10 10.000002 v 3.5 3.5 z m 20.000002,0 h -3.5 v -18 -18 h 4.0292 4.02921 c 3.37507,5.31055 9.7783,16.12491 9.7783,16.12491 0,0 6.91696,-10.76303 10.4057,-16.12531 h 3.87879 3.8788 v 18 18 h -3.5 -3.5 l -0.007,-12.25 -0.007,-12.25 -5.12888,7.75 c -2.02182,2.97558 -6.01282,8.96239 -6.01282,8.96239 0,0 -3.93129,-5.88047 -5.86375,-8.8407 l -4.98055,-7.6283 v 12.1283 12.12831 z m 39,0 h -3.5 v -18 -18 h 9.54966 C 165.181,44.531059 174,46.621939 174,57.376719 c 0.90141,9.35375 -10.9088,12.77772 -16.74999,12.84906 l -4.25,0.006 v 5.5 5.5 z m 8.27749,-18 c 4.52625,0.0763 9.08775,-2.04836 9.2225,-5.94714 -0.31974,-4.96104 -6.99585,-4.92438 -9.26422,-5.05286 h -4.73577 v 5.5 5.5 z"}]]
    [:span {:class (tw! "font-semibold text-xl tracking-tight")} "KEMP LITTLE"]]
   [:div {:class (tw! "block lg:hidden")}
    [:button {:class (tw! "flex items-center px-3 py-2 border rounded text-teal-200 border-teal-400 hover:text-white hover:border-white")}
     [:svg {:class   (tw! "fill-current h-3 w-3")
            :viewBox "0 0 20 20"
            :xmlns   "http://www.w3.org/2000/svg"}
      [:title "Menu"]
      [:path {:d "M0 3h20v2H0V3zm0 6h20v2H0V9zm0 6h20v2H0v-2z"}]]]]
   [:div {:class (tw! "w-full block flex-grow lg:flex lg:items-center lg:w-auto")}
    (let [a (tw! "block mt-4 lg:inline-block lg:mt-0 text-teal-200 hover:text-white mr-4")]
      [:div {:class (tw! "text-sm lg:flex-grow")}
       [:a {:href (rfe/href :kemplittle.routes/item-list) :class a} "Items"]
       ])
    [:div
     [:a {:href  "#"
          :class (tw! "inline-block text-sm px-4 py-2 leading-none border rounded"
                      "text-white border-white hover:border-transparent hover:text-teal-500"
                      "hover:bg-white mt-4 lg:mt-0")}
      "Download"]]]])

(defn front-page []
  [:div
   [:h2 "Welcome to frontend"]
   [:p "Look at console log for controller calls."]])

(spit-css! "resources/public/css/kl.css")