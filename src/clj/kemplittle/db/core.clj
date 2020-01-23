(ns kemplittle.db.core
  (:require
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]
    [java-time.pre-java8 :as jt]
    [mount.core :refer [defstate]]
    [kemplittle.config :refer [env]]))

(defstate ^:dynamic *db*
          :start (conman/connect! {:jdbc-url (env :database-url)})
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

(def max-id (atom 0))

(def users (atom []))

(def act (atom []))


  ; "Need to be generated when secretary starts process : 
  ;    initiated-by-id is the old ref-id - we use it to get admin email
  ;    uuid - randomly generated when start
  ;    client-name - need to get this from form submitted by admin at start"
  ; ; {:uuid "23l5n25-235l-2nnn64nb" :initiated-by-id "1" :client-name ""}
(def session-data (atom []))

(def authdata {:admin {:id "1"
                       :password "open4me"
                       :email "vlad.anghene@gmail.com"
                       :name "Vlad"
                       :access "admin"}
               :gerard {:id "2"
                        :password "greentree"
                        :email "Gerard.Frith@kemplittle.com"
                        :name "Gerard Frith"
                        :access "admin"}
               :chris {:id "3"
                       :password "whitesnow"
                       :email "Chris.Gray@kemplittle.com"
                       :name "Chris Gray"
                       :access "admin"}
               :sylvia {:id "4"
                        :password "zenStone"
                        :email "Sylvia.Eghiayan@kemplittle.com"
                        :name "Sylvia Eghiayan"
                        :access "secretary"}
               :secretary1 {:id "5"
                            :password "secret"
                            :email "Gerard.Frith@kemplittle.com"
                            :name "Gerard Frith"
                            :access "secretary"}
               :secretary2 {:id "6"
                            :password "nuclear"
                            :email "Chris.Gray@kemplittle.com"
                            :name "Chris Gray"
                            :access "secretary"}
               :secretary3 {:id "7"
                            :password "thisthat"
                            :email ""
                            :name "John"
                            :access "secretary"}
               :vlad {:id "8"
                      :password "open4me"
                      :email "vlad.anghene@gmail.com"
                      :name "VladSECRETARY"
                      :access "secretary"}})

(defn match-authdata-initiated-by-id
  "get user-data from state based on initiated-by-id from session-data"
  [ref-id]
  (let [user-data (second (first (filter #(= ref-id (:id (val %)))
                                         authdata)))]
    {:admin-name (:name user-data)
     :admin-email (:email user-data)}))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Timestamp
  (result-set-read-column [v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (result-set-read-column [v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (result-set-read-column [v _2 _3]
    (.toLocalTime v)))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [v]
    (java.sql.Timestamp. (.getTime v)))
  java.time.LocalTime
  (sql-value [v]
    (jt/sql-time v))
  java.time.LocalDate
  (sql-value [v]
    (jt/sql-date v))
  java.time.LocalDateTime
  (sql-value [v]
    (jt/sql-timestamp v))
  java.time.ZonedDateTime
  (sql-value [v]
    (jt/sql-timestamp v)))

