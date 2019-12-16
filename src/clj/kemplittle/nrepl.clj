(ns kemplittle.nrepl
  (:require
   [nrepl.server :as nrepl]

   [clojure.reflect :as r])
  (:use [clojure.pprint :only [print-table]]))

(defn start
  "Start a network repl for debugging on specified port followed by
  an optional parameters map. The :bind, :transport-fn, :handler,
  :ack-port and :greeting-fn will be forwarded to
  clojure.tools.nrepl.server/start-server as they are."
  [{:keys [port bind transport-fn handler ack-port greeting-fn]}]
  (try
    #_(log/info "starting nREPL server on port" port)
    (nrepl/start-server :port port
                        :bind bind
                        :transport-fn transport-fn
                        :handler handler
                        :ack-port ack-port
                        :greeting-fn greeting-fn)

    (catch Throwable t
      #_(log/error t "failed to start nREPL")
      (throw t))))

(defn stop [server]
  (nrepl/stop-server server)
  #_(log/info "nREPL server stopped"))

(defn reflect-me [this]
  (print-table
   (sort-by :name
            (filter :parameter-types
                    (:members (r/reflect this))))))

