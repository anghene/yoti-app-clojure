(ns kemplittle.client.yotiapp
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            )
  (:import [com.yoti.api.client ActivityDetails Date FileKeyPairSource
            HumanProfile Image YotiClient YotiClientBuilder]
           [com.yoti.api.client.spi.remote.call RawResourceFetcher SignedRequestResponse]))

(def max-id (atom 0))

(def users (atom {}))

(def client
  (let [ycb (. YotiClientBuilder newInstance)
        forapp (doto ycb (.forApplication (:yotiapp-sdk-id env)))
        wkp (. forapp withKeyPair (FileKeyPairSource/fromFile
                                   (java.io.File. (:yotiapp-priv-key-path env))))]
    (.build wkp)))

(defn pass-token [token]
  (let [activity-details (.getActivityDetails client token)]
    (swap! users conj {@max-id activity-details})
    (swap! max-id inc)
    (timbre/info "users so far: " @users)))