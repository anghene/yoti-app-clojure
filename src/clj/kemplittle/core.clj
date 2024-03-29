(ns kemplittle.core
  (:require
   [kemplittle.handler :as handler]
   [kemplittle.nrepl :as nrepl]
   [luminus.http-server :as http]
   [luminus-migrations.core :as migrations]
   [kemplittle.config :refer [env]]
   [clojure.tools.cli :refer [parse-opts]]
   [mount.core :as mount]
   [taoensso.timbre :as timbre :refer [info]])
  (:gen-class)
  )


;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread ex]
      (timbre/info {:what :uncaught-exception
                  :exception ex
                  :where (str "Uncaught exception on " (.getName thread))}))))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate ^{:on-reload :noop} http-server
  :start
  (http/start
    (-> env
        (assoc  :handler (handler/app))
        (update :io-threads #(or % (* 2 (.availableProcessors (Runtime/getRuntime)))))
        (update :port #(or (-> env :options :port) %))))
  :stop
  (http/stop http-server))

(mount/defstate ^{:on-reload :noop} repl-server
  :start
  (when (env :nrepl-port)
    (nrepl/start {:bind (env :nrepl-bind)
                  :port (env :nrepl-port)}))
  :stop
  (when repl-server
    (nrepl/stop repl-server)))


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (timbre/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (timbre/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& args]
  (mount/start #'kemplittle.config/env)
  (cond
    (nil? (:database-url env))
    (do
      (timbre/info "Database configuration not found, :database-url environment variable must be set before running")
      (System/exit 1))
    (some #{"init"} args)
    (do
      (migrations/init (select-keys env [:database-url :init-script]))
      (System/exit 0))
    (migrations/migration? args)
    (do
      (migrations/migrate args (select-keys env [:database-url]))
      (System/exit 0))
    :else
    (start-app args)))