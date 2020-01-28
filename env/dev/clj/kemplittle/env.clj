(ns kemplittle.env
  (:require
   [selmer.parser :as parser]
   [kemplittle.dev-middleware :refer [wrap-dev]])
  (:gen-class))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     #_(log/info "\n-=[kemplittle started successfully using the development profile]=-"))
   :stop
   (fn []
     #_(log/info "\n-=[kemplittle has shut down successfully]=-"))
   :middleware wrap-dev})
