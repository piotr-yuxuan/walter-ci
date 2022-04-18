(defproject com.github.piotr-yuxuan/walter-ci (-> "./resources/walter-ci.version" slurp .trim)
  :description "A Clojure CICD atop community tools and GitHub Actions, with conventions and no configuration"
  :url "https://github.com/piotr-yuxuan/walter-ci"
  :license {:name "European Union Public License 1.2 or later"
            :url "https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12"
            :distribution :repo}
  :scm {:name "git"
        :url "https://github.com/piotr-yuxuan/walter-ci"}
  :pom-addition [:developers [:developer
                              [:name "胡雨軒 Петр"]
                              [:url "https://github.com/piotr-yuxuan"]]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [leiningen "2.9.6" :upgrade false :exclusions [org.apache.httpcomponents/httpcore]]
                 [leiningen-core "2.9.6" :upgrade false]
                 [com.brunobonacci/safely "0.7.0-alpha3"]
                 [clj-http "3.12.3" :exclusions [riddley]]
                 [com.github.piotr-yuxuan/malli-cli "2.0.0"] ; Configuration value from the command-line
                 [babashka/process "0.1.1"]
                 [camel-snake-kebab "0.4.2"]
                 [de.ubercode.clostache/clostache "1.4.0"] ; Templating engine Clojure wrapper.
                 [metosin/malli "0.8.4"]
                 [io.forward/yaml "1.0.11"] ; No sane person would wilfully accept to write YAML by hand.
                 [caesium "0.14.0"]
                 [metosin/jsonista "0.3.5"]]
  :main piotr-yuxuan.walter-ci.main
  :profiles {:github {:github/topics ["github" "actions" "automation" "clojure"]
                      :github/private? false}
             :provided {:dependencies []}
             :dev {:global-vars {*warn-on-reflection* true}}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.compiler.disable-locals-clearing=false"
                                  "-Dclojure.compiler.elide-meta=[:doc :file :line :added]"]}}
  :deploy-repositories [["clojars" {:sign-releases false
                                    :url "https://clojars.org/repo"
                                    :username :env/WALTER_CLOJARS_USERNAME
                                    :password :env/WALTER_CLOJARS_PASSWORD}]
                        ["github" {:sign-releases false
                                   :url "https://maven.pkg.github.com/piotr-yuxuan/walter-ci"
                                   :username :env/WALTER_ACTOR
                                   :password :env/WALTER_GITHUB_PASSWORD}]])
