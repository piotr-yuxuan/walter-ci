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
                 [clj-commons/clj-yaml "0.7.0"]
                 [vvvvalvalval/supdate "0.2.3"]
                 [clojurewerkz/balagan "1.0.5" :exclusions [org.clojure/clojure]]
                 [com.github.piotr-yuxuan/malli-cli "1.0.3-SNAPSHOT"] ; Command-line processing
                 [com.brunobonacci/safely "0.7.0-alpha3"]
                 [medley "1.3.0"]
                 [clj-http "3.12.0"]
                 [com.arohner/uri "0.1.2"]
                 [io.forward/semver "0.1.0"]
                 [metosin/muuntaja "0.6.8"]
                 [babashka/process "0.0.2"]
                 [camel-snake-kebab "0.4.2"]
                 [metosin/malli "0.4.0"]
                 [caesium "0.14.0"]
                 [metosin/jsonista "0.3.2"]]
  :main piotr-yuxuan.walter-ci.main
  :plugins [[lein-deploy-uberjar "2.0.0"]]
  :profiles {:github {:github/topics ["github" "actions" "automation" "clojure"]}
             :provided {:dependencies [[org.clojure/clojure "1.10.3"]]}
             :dev {:global-vars {*warn-on-reflection* true}}
             :uberjar {:aot [piotr-yuxuan.walter-ci.main]
                       :jvm-opts ["-Dclojure.compiler.disable-locals-clearing=false"
                                  "-Dclojure.compiler.direct-linking=true"]}}
  :deploy-repositories [["clojars" {:sign-releases false
                                    :url "https://clojars.org/repo"
                                    :username :env/WALTER_CLOJARS_USERNAME
                                    :password :env/WALTER_CLOJARS_PASSWORD}]
                        ["github" {:sign-releases false
                                   :url "https://maven.pkg.github.com/piotr-yuxuan/walter-ci"
                                   :username :env/GITHUB_ACTOR
                                   :password :env/WALTER_GITHUB_PASSWORD}]])
