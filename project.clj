(defproject com.github.piotr-yuxuan/walter-ci (-> "./resources/walter-ci.version" slurp .trim)
  :description "Walter is the younger son of Helmut"
  :url "https://github.com/piotr-yuxuan/walter-ci"
  :license {:name "European Union Public License 1.2 or later"
            :url "https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12"
            :distribution :repo}
  :scm {:name "git"
        :url "https://github.com/piotr-yuxuan/walter-ci"}
  :pom-addition [:developers [:developer
                              [:name "胡雨軒 Петр"]
                              [:url "https://github.com/piotr-yuxuan"]]]
  :dependencies [[leiningen "2.9.6"]
                 [leiningen-core "2.9.6"]
                 [medley "1.3.0"]
                 [clj-http "3.12.1"]
                 [com.arohner/uri "0.1.2"]
                 [metosin/muuntaja "0.6.8"]
                 [camel-snake-kebab "0.4.2"]
                 [metosin/malli "0.4.0"]
                 [metosin/jsonista "0.3.2"]]
  :global-vars {*warn-on-reflection* true}
  :main piotr-yuxuan.walter-ci.main
  :profiles {:github {:github/topics ["github" "actions" "automation" "clojure"]}
             :provided {:dependencies [[org.clojure/clojure "1.10.3"]]}
             :dev {:jvm-opts ["-Dclojure.compiler.disable-locals-clearing=true"]}
             :uberjar {:global-vars {*warn-on-reflection* false}
                       :aot [piotr-yuxuan.walter-ci.main]
                       :jvm-opts ["-Dclojure.compiler.disable-locals-clearing=false"
                                  "-Dclojure.compiler.direct-linking=true"]}}
  :deploy-repositories [["clojars" {:sign-releases false
                                    :url "https://clojars.org/repo"
                                    :username :env/WALTER_CLOJARS_PASSWORD
                                    :password :env/WALTER_CLOJARS_USERNAME}]])
