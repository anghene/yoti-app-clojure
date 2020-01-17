(ns kemplittle.middleware
  (:require
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [buddy.auth.accessrules :refer [restrict]]
   [buddy.auth.backends.session :refer [session-backend]]
   [buddy.auth.backends.token :refer [jwe-backend]]
   [buddy.auth.backends.token :refer [jws-backend]]
   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   [buddy.core.nonce :refer [random-bytes]]
   [buddy.sign.jwt :as jwt]
   [buddy.sign.jwt :refer [encrypt]]
   [cheshire.generate :as cheshire]
   [clj-time.core :refer [plus now minutes]]
   [cognitect.transit :as transit]
   [kemplittle.config :refer [env]]
   [kemplittle.env :refer [defaults]]
   [kemplittle.layout :refer [error-page]]
   [kemplittle.middleware.formats :as formats]
   [taoensso.timbre :refer [info]]
   [muuntaja.middleware :refer [wrap-format wrap-params]]
   [ring-ttl-session.core :refer [ttl-memory-store]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   ))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        #_(log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))


(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(def secret "Though Chapter One of the novel famously begins with the words \"Call me Ishmael,\" only once in the whole book is the narrator called Ishmael")

(def token-backend
  (jws-backend {:secret secret :options {:alg :hs512}}))

(defn wrap-json-resp [handler]
  (-> handler
      (wrap-json-response {:pretty false})
      (wrap-json-body {:keywords? true :bigdecimals? true})))

(defn wrap-auth
  "Wrapper to check for auth jws token in header and return nice json"
  [handler]
  (let [backend token-backend]
    (-> handler
        (wrap-authentication backend)
        (wrap-authorization backend)
        wrap-json-resp)))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-auth
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (assoc-in  [:session :store] (ttl-memory-store (* 60 30)))))
      (wrap-cors :access-control-allow-origin [#"http://localhost:9500"]
                 :access-control-allow-methods [:get :put :post :delete])
      wrap-internal-error))
