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
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [leiningen "2.9.6" :upgrade false :exclusions [org.apache.httpcomponents/httpcore]]
                 [leiningen-core "2.9.6" :upgrade false]
                 [com.brunobonacci/safely "0.7.0-alpha3"]
                 [clj-http "3.12.3" :exclusions [riddley]]
                 [com.github.piotr-yuxuan/malli-cli "1.0.2"] ; Command-line processing
                 [babashka/process "0.0.2"]
                 [camel-snake-kebab "0.4.2"]
                 [metosin/malli "0.7.4"]
                 [caesium "0.14.0"]
                 [metosin/jsonista "0.3.5"]
                 ;[clj-commons/clj-yaml "0.7.107"]
                 ;[com.arohner/uri "0.1.2"]
                 ;[io.forward/semver "0.1.0"]
                 ;[metosin/muuntaja "0.6.8"]
                 ;[vvvvalvalval/supdate "0.2.3"]
                 ]
  :main piotr-yuxuan.walter-ci.main
  :profiles {:github {:github/topics ["github" "actions" "automation" "clojure"]}
             :provided {:dependencies [[org.clojure/clojure "1.10.3"]]}
             :dev {:global-vars {*warn-on-reflection* true}}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.compiler.disable-locals-clearing=false"
                                  "-Dclojure.compiler.elide-meta=[:doc :file :line :added]"]}})
