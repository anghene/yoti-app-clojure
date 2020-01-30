(defproject kemplittle "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :java-source-paths ["src/java/" "test/java"]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main kemplittle.core

  :plugins [[lein-tools-deps "0.4.5"]
            [lein-cljsbuild "1.1.7"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}
  :exclusions [ch.qos.logback/logback-classic
               org.slf4j.impl.StaticLoggerBinder]
  :profiles
  {:dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[cheshire "5.9.0"]
                                 [pjstadig/humane-test-output "0.10.0"]
                                 [prone "2019-07-08"]
                                 [ring/ring-devel "1.8.0"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                 [jonase/eastwood "0.3.5"]]
                  :source-paths ["env/dev/clj"]

                  :cljsbuild {:builds [{:id "main"
                                        :source-paths ["src-cljs"]
                                        :compiler {:asset-path "/js/out-prod"
                                                   :output-dir "resources/public/out-main"
                                                   :output-to "resources/public/cljs-out/dev-main.js"
                                                   :optimizations :advanced
                                                   :externs ["Yoti-externs.js"]
                                                   :pretty-print false}}]}
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})





