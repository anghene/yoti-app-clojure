(ns kemplittle.pages.docscan
  (:require
   [xframe.core.alpha :as xf :refer [<sub]]
   [taoensso.timbre :refer [info]]
   [tailwind.core :refer [tw tw! spit-css!]]))

(defn docscan-page [match]
  (let [{:keys [path query]} (:parameters match)
        {:keys [id]} path
        ref (-> query :ref)
        session (<sub [:session :docscan])
        idsess (-> session :docscan :id)
        tkn (-> session :docscan :tkn)]
    ; (info "id: " id " & tkn: " tkn)
    [:div {:class (tw! "w-full m-auto p-6")}
     [:h1 {:class (tw! "text-lg font-semibold")} (str  "Verify your identity with Doc Scan powered by Yoti ")]
     [:p "Let’s get started! In order to verify your identity we’ll need you to take a photo of your identity document using the camera or webcam on your device. We will perform a “liveness” check by asking you to interact with your device’s camera or webcam."]
     [:iframe
      {:allow "camera;"
       :src (str "https://api.yoti.com/idverify/v1/web/index.html?sessionID=" idsess "&sessionToken=" tkn)
       :width "100%"
       :height "600"
       :frameBorder "0"}]
     [:br]]))

(spit-css! "resources/public/css/kl.css")