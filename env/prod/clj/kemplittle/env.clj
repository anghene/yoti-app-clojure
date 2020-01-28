(ns kemplittle.env
  )

(def defaults
  {:init
   (fn []
     #_(log/info "\n-=[kemplittle started successfully]=-"))
   :stop
   (fn []
     #_(log/info "\n-=[kemplittle has shut down successfully]=-"))
   :middleware identity})
