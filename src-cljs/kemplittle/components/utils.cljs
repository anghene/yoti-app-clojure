(ns kemplittle.components.utils
  (:require
   [tailwind.core :refer [tw tw! spit-css!]]
   [cljsjs.emotion]
   [reitit.frontend.easy :as rfe]
   [uix.core.alpha :as uix]
   [taoensso.timbre :refer [info]]))

(defn not-found []
  (info "gets here")
  [:div "error page not found"])

(defn header [is-admin?]
  [:nav {:class (tw! "w-full flex items-center justify-between flex-wrap bg-klgreen-100 p-6")}
   [:div {:class (tw! "flex items-center flex-shrink-0 text-white mr-6")}
    [:a {:href  "#"}
     [:svg {:class   (tw! "fill-current h-auto w-auto mr-6")
            :width   "50"
            :height  "50"
            :viewBox "0 0 217 218"
            :xmlns   "http://www.w3.org/2000/svg"}
      [:path {:d "M 108.50001,218.23093 H 3.1882316e-6 V 109.23094 0.23093892 H 108.50001 217 V 109.23094 218.23093 Z m 0,-4 H 213 V 109.23094 4.2309389 H 108.50001 4.0000082 V 109.23094 214.23093 Z M 43.000008,173.23094 h -13 v -18 -17.99999 h 3.5 3.5 v 14.49999 14.5 h 9.5 9.5 v 3.5 3.5 z m 19,0 h -4 v -18 -17.99999 h 4 4 v 17.99999 18 z m 21,0 h -4 v -14.5 -14.49999 h -5.5 -5.5 v -3.5 -3.5 h 15 15 v 3.5 3.5 h -5.5 -5.5 v 14.49999 14.5 z m 31.500002,0 h -3.5 v -14.5 -14.49999 h -5.5 -5.5 v -3.5 -3.5 h 14.5 14.5 v 3.5 3.5 h -5.5 -5.5 v 14.49999 14.5 z m 30,0 h -12.5 v -18 -17.99999 h 3.5 3.5 v 14.49999 14.5 h 9 9 v 3.5 3.5 z m 28.99999,0 h -13.49999 v -18 -17.99999 H 173 186 v 3.5 3.5 h -9.5 -9.5 v 4 3.99999 h 8.5 8.5 v 3 3 h -8.5 -8.5 v 4.5 4.5 h 10 10 v 3 3 z m -64.99999,-62 H 72.000008 v -2 -2 h 36.500002 36.5 v 2 2 z M 46.500008,81.230939 h -3.5 v -18 -18 h 3.5 3.5 v 8.05936 8.05935 l 7.75,-8.14111 7.692267,-7.94583 h 8.50058 l -6.917547,8.10213 -6.97529,7.63391 5.15639,6.86609 5.15639,6.8661 2.43729,3.25 2.43729,3.25 h -4.51277 -4.51277 l -5.76417,-7.52564 -5.76418,-7.52564 -2.34174,2.19995 -2.34174,2.19995 v 5.32569 5.32569 z m 44,0 h -13.5 v -18 -18 h 13.5 13.500002 v 3 3 h -10.000002 -10 v 4.5 4.5 h 8.5 8.500002 v 3 3 h -8.500002 -8.5 v 4 4 h 10 10.000002 v 3.5 3.5 z m 20.000002,0 h -3.5 v -18 -18 h 4.0292 4.02921 c 3.37507,5.31055 9.7783,16.12491 9.7783,16.12491 0,0 6.91696,-10.76303 10.4057,-16.12531 h 3.87879 3.8788 v 18 18 h -3.5 -3.5 l -0.007,-12.25 -0.007,-12.25 -5.12888,7.75 c -2.02182,2.97558 -6.01282,8.96239 -6.01282,8.96239 0,0 -3.93129,-5.88047 -5.86375,-8.8407 l -4.98055,-7.6283 v 12.1283 12.12831 z m 39,0 h -3.5 v -18 -18 h 9.54966 C 165.181,44.531059 174,46.621939 174,57.376719 c 0.90141,9.35375 -10.9088,12.77772 -16.74999,12.84906 l -4.25,0.006 v 5.5 5.5 z m 8.27749,-18 c 4.52625,0.0763 9.08775,-2.04836 9.2225,-5.94714 -0.31974,-4.96104 -6.99585,-4.92438 -9.26422,-5.05286 h -4.73577 v 5.5 5.5 z"}]]]]
   (if is-admin?
     [:div {:class (tw! "flex items-center flex-shrink-0 text-white mr-6")}
      [:a {:class (tw! "flex items-center px-3 py-2 border rounded text-teal-200 border-teal-400 hover:text-white hover:border-white")}
        [:span "Logs"]]]
     [:div {:class (tw! "block lg:hidden")}
      [:button {:class (tw! "flex items-center px-3 py-2 border rounded text-teal-200 border-teal-400 hover:text-white hover:border-white")}
       [:svg {:class   (tw! "fill-current h-3 w-3")
              :viewBox "0 0 20 20"
              :xmlns   "http://www.w3.org/2000/svg"}
        [:title "Menu"]
        [:path {:d "M0 3h20v2H0V3zm0 6h20v2H0V9zm0 6h20v2H0v-2z"}]]]])])


(defn footer []
  (let [expanded? (uix/state false)]
    [:footer
     {:class "aos-init aos-animate"
      :role "contentinfo"
      :id "site_footer"
      :data-aos "fade-up"}
     [:div {:id "site_footer_top"}
      [:div {:class "container container_capped clearfix"}
       [:a {:href "https://www.kemplittle.com/", :id "footer_logo"}
        [:img {:src "https://www.kemplittle.com/wp-content/themes/kemplittle/images/logo_kemp_little_w.png"
               :alt "Kemp Little"}]]
       [:p {:id "footer_strapline"}
        "Lawyers" [:br {}] "and thought leaders who are passionate about technology"]
       [:div {:class (str (when @expanded? "open ")
                          (tw! "cursor-pointer")) :id "footer_toggle"
              :on-click #(swap! expanded? not)}
        (str (if @expanded? "Close" "Expand") " footer")]
       [:div {:id "footer_sra"}
        [:div {:style {"maxWidth" "275px" "maxHeight" "163px"}}
         [:div {:style {"position" "relative" "paddingBottom" "59.1%" "height" "auto" "overflow" "hidden"}}
          [:iframe {:frameBorder "0"
                    :scrolling "no"
                    :allowtransparency "true"
                    :src "https://cdn.yoshki.com/iframe/55849r.html"
                    :style {"border" "0px"
                            "margin" "0px"
                            "padding" "0px"
                            "backgroundColor" "transparent"
                            "top" "0px"
                            "left" "0px"
                            "width" "100%"
                            "height" "100%"
                            "position" "absolute"}}]]]]
       [:div {:id "footer_reveal" :style (if @expanded?
                                           {"display" "block"}
                                           {"display" "none"})}
        [:div {:id "footer_reveal_wrapper", :class "clearfix"}
         [:div {:class "footer_col"}
          [:h3 {}
           "Kemp Little"]
          [:p {}
           "138 Cheapside"
           [:br {}] "City of London"
           [:br {}] "EC2V 6BJ"]
          [:p {}
           [:a {:href "tel:020 7600 8080"}
            "020 7600 8080"]]
          [:p {}
           [:a {:href "mailto:hello@kemplittle.com"}
            "hello@kemplittle.com"]]]
         [:div {:class "footer_col"}
          [:h3 {}
           "Services"]
          [:ul {}
           [:li {}
            [:a {:href "/services/commercial-technology/"}
             "Commercial technology"]]
           [:li {}
            [:a {:href "/services/consulting/"}
             "Consulting"]]
           [:li {}
            [:a {:href "/services/disputes/"}
             "Disputes"]]
           [:li {}
            [:a {:href "/services/intellectual-property/"}
             "Intellectual property"]]
           [:li {}
            [:a {:href "/services/employment/"}
             "Employment"]]
           [:li {}
            [:a {:href "/services/immigration/"}
             "Immigration"]]]]
         [:div {:class "footer_col"}
          [:h3 {:class "mob_mask"}
           " "]
          [:ul {}
           [:li {}
            [:a {:href "/services/sourcing/"}
             "Sourcing"]]
           [:li {}
            [:a {:href "/services/corporate/"}
             "Corporate"]]
           [:li {}
            [:a {:href "/services/data-protection-privacy/"}
             "Data protection & privacy"]]
           [:li {}
            [:a {:href "/services/financial-regulation/"}
             "Financial regulation"]]
           [:li {}
            [:a {:href "/services/private-equity-venture-capital/"}
             "Private equity & venture capital"]]
           [:li {}
            [:a {:href "/services/tax/"}
             "Tax"]]]]
         [:div {:class "footer_col"}
          [:h3 {}
           "Sitemap"]
          [:ul {}
           [:li {}
            [:a {:href "/people/"}
             "Our people"]]
           [:li {}
            [:a {:href "/insights/"}
             "Insights"]]
           [:li {}
            [:a {:href "/events/"}
             "Events"]]
           [:li {}
            [:a {:href "/about/"}
             "About us"]]
           [:li {}
            [:a {:href "/contact/"}
             "Contact us"]]
           [:li {}
            [:a {:href "/legal/"}
             "Cookies"]]
           [:li {}
            [:a {:href "/privacy/"}
             "Privacy"]]
           [:li {}
            [:a {:href "/terms/"}
             "Terms of use"]]
           [:li {}
            [:a {:href "https://www.kemplittle.com/compliants-policy/"}
             "Compliants"]]
           [:li {}
            [:a {:href "https://www.kemplittle.com/services/disputes/debt-recovery-charges/"}
             "Debt recovery charges"]]]]
         [:div {:class "footer_col"}
          [:h3 {}
           "Follow us"]
          [:ul {}
           [:li {}
            [:a {:href "https://twitter.com/kemplittle"
                 :target "_blank"}
             "Twitter"]]
           [:li {}
            [:a {:href "https://www.linkedin.com/company/kemp-little-llp/"
                 :target "_blank"}
             "LinkedIn"]]
           [:li {:class "li_gap"}
            [:a {:href "/flightdeck/"}
             "FlightDeck"]]
           [:li {}
            [:a {:href "#", :class "newsletter_reveal"}
             "Sign up for our newsletters"]]]]]]
       [:div {:id "footer_sra_mobile"}
        [:div {:style {"maxWidth" "275px"
                       "maxHeight" "163px"}}
         [:div {:style {"position" "relative"
                        "paddingBottom" "59.1%"
                        "height" "auto"
                        "overflow" "hidden"}}
          [:iframe {:frameBorder "0"
                    :scrolling "no"
                    :allowtransparency "true"
                    :src "https://cdn.yoshki.com/iframe/55849r.html"
                    :style {"border" "0px"
                            "margin" "0px"
                            "padding" "0px"
                            "backgroundColor" "transparent"
                            "top" "0px"
                            "left" "0px"
                            "width" "100%"
                            "height" "100%"
                            "position"  "absolute"}}]]]]
       [:div {:class "footer_smallprint"}
        [:p {}
         "Kemp Little LLP is a limited liability partnership registered in England and Wales (registered number\n\t\t\t\t\tOC300242) and is authorised and regulated by the Solicitors Regulation Authority. Its registered office\n\t\t\t\t\tis 138 Cheapside, London EC2V 6BJ. The SRA "
         [:i {}
          "Standards and Regulations"]
         " can be accessed by "
         [:a {:href "https://www.sra.org.uk/solicitors/standards-regulations/", :target "_blank", :rel "noopener"}
          "clicking here."]]]]]
     [:div {:id "site_footer_base"
            :class (tw! "lg:block sm:hidden")}
      [:div {:class "container container_capped"}
       [:ul {
             :id "footer_logos"
             :class (str "logos-slider")}
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/08/cyber_essentials-bw-logo-webiste-footer.png"
                  :alt "Cyber Essentials logo"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/10/Legal-500-2020-top-tier-BW-web.jpg"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/06/TN_STACKED_LOGO_COLOUR_DARK_RGB_SML_72DPI-website-bw.png"
                  :alt "Tech Nation logo"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2018/10/LORCA-Secondary-Logo-Pos-1.jpg"
                  :alt "LORCA logo"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/10/ABTA_PartnerPlus_Port_WithTag_Mono-web.jpg"
                  :alt "ABTA Partner+ logo"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2018/10/Make-Your-Ask-logo_small.jpg"
                  :alt "Make Your Ask logo"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/10/FT-Innovative-Lawyers-2019-winner.png"
                  :alt "FT Innovative Lawyers 2019 winners logo"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/03/Plexal-logo-BW.jpg"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/05/The-Lawyer-2019-finalist-bw.jpg"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/08/Excellence_Awards_website_BW.jpg"
                  :alt "Law Society Excellence Awards shortlisted"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/07/EP-awards-logo-2019-BW-website.jpg"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/08/lba19_tmt_hc-webite-footer.png"
                  :alt "Legal Business :Awards  highly commended"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/02/Chamber-logo-bw.jpg"}]]]]
        [:li {:class (tw! "lg:px-0")}
         [:span {:class "logo_table"}
          [:span {:class "logo_cell"}
           [:img {:src "https://www.kemplittle.com/wp-content/uploads/2019/02/Chambers-fintech-logo-bw.jpg"}]]]]]]]]))

