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
  :dependencies [[leiningen "2.9.6" :upgrade false :exclusions [org.apache.httpcomponents/httpcore]]
                 [leiningen-core "2.9.6" :upgrade false]
                 [com.brunobonacci/safely "0.7.0-alpha3"]
                 [clj-http "3.12.3" :exclusions [riddley]]
                 [com.github.piotr-yuxuan/malli-cli "2.0.0"] ; Configuration value from the command-line
                 [babashka/process "0.1.1"]
                 [camel-snake-kebab "0.4.2"]
                 [metosin/malli "0.8.4"]
                 [io.forward/yaml "1.0.11"] ; No sane person would wilfully accept to write YAML by hand.
                 [caesium "0.14.0"]
                 [metosin/jsonista "0.3.5"]]
  :main piotr-yuxuan.walter-ci.main
  :profiles {:github {:github/topics ["github" "actions" "automation" "clojure"]
                      :github/private? false}
             :provided {:dependencies [[org.clojure/clojure "1.11.1"]]}
             :dev {:global-vars {*warn-on-reflection* true}}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.compiler.disable-locals-clearing=false"
                                  "-Dclojure.compiler.elide-meta=[:doc :file :line :added]"]}})
