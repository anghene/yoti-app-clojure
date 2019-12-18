(ns kemplittle.routes.home
  (:require
   [kemplittle.routes.updates :refer [authenticated? notification-handler]]
   [kemplittle.layout :as layout]
   [kemplittle.db.core :as db]
   [kemplittle.certs.core :as certs]
   [kemplittle.client.session :as session]
   [clojure.java.io :as io]
   [kemplittle.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]
   [taoensso.timbre :as timbre]
   [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
   [kemplittle.client.session :refer [get-new-session]])
  (:import [com.yoti.api.client ActivityDetails Date FileKeyPairSource
            HumanProfile Image YotiClient YotiClientBuilder]
           )
  )

(def client
  (let [ycb (. YotiClientBuilder newInstance)
        forapp (doto ycb (.forApplication "e2ce1112-1514-43b4-9c07-604ba1165685"))
        wkp (. forapp withKeyPair (FileKeyPairSource/fromFile
                                   (java.io.File. "/c/Users/VladAndrei/yotiapp.pem")))]
    (.build wkp)))

(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/home.md" io/resource slurp)}))

(defn yotiapp-page [request]
  (timbre/info "Got request from Yoti servers: " (:query-params request)))

(defn about-page [request]
  (let [session (get-new-session)]
    (timbre/info "session:" session)
    (layout/render request "docscan.html" {:session {:id (:session_id session)
                                                   :token (:client_session_token session)}})))

(defn updates-routes []
  ["/updates"
   {:middleware [middleware/wrap-csrf
                 #(-> % (wrap-basic-authentication authenticated?))]}
   ["" {:get notification-handler}]])

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/yotiapp" {:get yotiapp-page}]
   ["/docscan" {:get about-page}]])

