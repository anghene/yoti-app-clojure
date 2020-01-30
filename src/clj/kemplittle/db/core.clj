(ns kemplittle.db.core
  (:require
   [korma.db :as db]
   [korma.core
    :refer :all
    :rename {update dbupdate}]
   [clojure.java.jdbc :as jdbc]
   [conman.core :as conman]
   [taoensso.timbre :refer [info]]
   [java-time.pre-java8 :as jt]
   [mount.core :refer [defstate]]
   [kemplittle.config :refer [env]]))

(defn start-db []
  (db/defdb kemplittledb (db/sqlite3 {:db "kemplittle_dev.db"})))

(defstate ^:dynamic *db*
          :start (start-db)
          :stop nil)

(defentity admin)

(defentity uuid
  (pk :id)
  (table :uuid)
  (has-one admin (fk :id)))

(def max-id (atom 0))

(def users (atom []))

(def act (atom []))


  ; "Need to be generated when secretary starts process :
  ;    initiated-by-id is the old ref-id - we use it to get admin email
  ;    uuid - randomly generated when start
  ;    client-name - need to get this from form submitted by admin at start"
  ; ; {:uuid "23l5n25-235l-2nnn64nb" :initiated-by-id "1" :client-name ""}

(defn delete-uuid
  ([]
   (info "deleted all uuids")
   (delete uuid
           (where {:id [> 0]})))
  ([id]
   (delete uuid
           (where {:id [= id]}))))

(defn delete-admin
  ([]
   (info "deleted all admins")
   (delete admin
           (where {:id [> 0]})))
  ([id]
   (delete admin
           (where {:id [= id]}))))

(defn get-admin
  ([]
    (get-admin nil))
  ([id]
   (if (= id nil)
     (->
      (select admin))
     (-> (select admin
                 (where {:id [= id]}))
         first))))

(defn get-admin-by-username [uname]
  (-> (select admin
              (where {:username [= uname]}))
      first))

(defn get-uuid
  ([]
   (get-uuid nil))
  ([ask-uuid]
   (if (= ask-uuid nil)
     (->
      (select uuid))
     (-> (select uuid

                 (where {:uuid [= ask-uuid]})
                 )
         first))))

(defn get-uuid-by-initiator
  "returns all uuids by that initator"
  [id]
  (-> (select uuid
              (where {:initiated-by-id [= id]})
              )
      ))

(defn insert-uuid [{recvd-uuid :uuid cn :client-name ibid :initiated-by-id}]
  (insert uuid
          (values {:uuid recvd-uuid
                   :client-name cn
                   :initiated-by-id ibid})))

(defn insert-admin [{:keys [username password email name access]}]
  (insert
   admin
   (values {:username username :password password :email email :name name :access access})))

; (def authdata (get-admin))

; (defn match-authdata-initiated-by-id
;   "get user-data from state based on initiated-by-id from session-data"
;   [ref-id]
;   (let [user-data (second (first (filter #(= ref-id (:id (val %)))
;                                          authdata)))]
;     {:admin-name (:name user-data)
;      :admin-email (:email user-data)}))

