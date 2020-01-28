(ns kemplittle.certs.core
  (:require [buddy.core.keys :as keys]
            [buddy.core.codecs.base64 :refer :all]
            [clojure.data.json :refer [write-str read-json]]
            [buddy.core.codecs :as codecs]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [buddy.core.dsa :as dsa]
            [mount.core :as mount])
  )

(comment
  "Building the request yourself
   You can also build these requests yourself if you do not wish to use the Yoti SDKs.
   The Yoti Doc Scan API requires the following headers:

   Header
   X-Yoti-Auth-Digest

   Description
   Base64 encoded signed hash of the request, signed using the Key file pair
   associated with your Yoti Application. Steps to generate are described below

  Example
  qoMA1Pb8IaC4vv7DzgkZY5tD0hkHTbV1rnFX3EDMfr3lHilbdZakE......

  The X-Yoti-Auth-Digest should be a concatenation of the following:

  HTTP method, e.g. POST
  URI Path, e.g. /sessions/ (always exclude /idverify/v1 when signing)
  The query string (complete with the timestamp and nonce parameters)
  Base64 encoded value of the JSON payload
  These should be joined with an ‘&’.

  The concatenated string should then be signed using the
  Yoti Application PEM file,
  and the resulting signature encoded in Base64.

  Timestamps are Unix timestamps.

  Nonces are UUID strings.")

(defn base64encode [to-encode]
  (codecs/bytes->str (encode to-encode)))

(defn base64decode [to-decode]
  (codecs/bytes->str (decode to-decode)))

(defn sign [request]
  (let [priv-key-path (:docscan-priv-key-path env)
        key-to-use (keys/private-key priv-key-path)
        signed (-> (dsa/sign request {:key key-to-use :alg :rsassa-pkcs15+sha256})
                   (base64encode))]
    (timbre/info "signature: " signed)
    (timbre/info "key to use: " priv-key-path)
    signed))