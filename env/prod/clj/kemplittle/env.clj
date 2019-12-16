(ns kemplittle.env
  (:require ))

(def defaults
  {:init
   (fn []
     #_(log/info "\n-=[kemplittle started successfully]=-"))
   :stop
   (fn []
     #_(log/info "\n-=[kemplittle has shut down successfully]=-"))
   :middleware identity})
