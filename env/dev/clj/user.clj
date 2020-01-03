(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [kemplittle.config :refer [env]]
   [clojure.pprint]
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [kemplittle.core :refer [start-app]]
   [kemplittle.db.core]
   [conman.core :as conman]
   [hickory.core :as hick]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [taoensso.timbre :refer [info]]
   [luminus-migrations.core :as migrations]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'kemplittle.core/repl-server))

(defn stop
  "Stops application."
  []
  (mount/stop-except #'kemplittle.core/repl-server))

(defn restart
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db
  "Restarts database."
  []
  (mount/stop #'kemplittle.db.core/*db*)
  (mount/start #'kemplittle.db.core/*db*)
  (binding [*ns* 'kemplittle.db.core]
    (conman/bind-connection kemplittle.db.core/*db* "sql/queries.sql")))

(defn reset-db
  "Resets database."
  []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate
  "Migrates database up for all outstanding migrations."
  []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback
  "Rollback latest database migration."
  []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration
  "Create a new up and down migration file with a generated timestamp and `name`."
  [name]
  (migrations/create name (select-keys env [:database-url])))

(defn read-file [file]
  (-> file io/resource slurp))

(defn parse-as-hiccup [file]
  (-> file
      read-file
      (hick/parse)
      (hick/as-hiccup)
      ))