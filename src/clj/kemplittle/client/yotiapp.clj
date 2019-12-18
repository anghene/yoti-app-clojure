(ns kemplittle.client.yotiapp
  (:require [environ.core :refer [env]]
             )
  (:import [com.yoti.api.client ActivityDetails Date FileKeyPairSource
            HumanProfile Image YotiClient YotiClientBuilder]
           [com.yoti.api.client.spi.remote.call RawResourceFetcher SignedRequestResponse]))

(def client
  (let [ycb (. YotiClientBuilder newInstance)
        forapp (doto ycb (.forApplication (:yotiapp-sdk-id env)))
        wkp (. forapp withKeyPair (FileKeyPairSource/fromFile
                                   (java.io.File. (:yotiapp-priv-key-path env))))]
    (.build wkp)))

(defn pass-token [token]
  (let [activity-details (.getActivityDetails client token)]))