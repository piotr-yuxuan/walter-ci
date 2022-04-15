{:user {:global-vars {*warn-on-reflection* true}
        :plugins [[lein-ancient "LATEST"]
                  [lein-nomis-ns-graph "LATEST"] ; must stay the first, see https://github.com/simon-katz/lein-nomis-ns-graph#troubleshooting
                  [jonase/eastwood "LATEST"]
                  [lein-bikeshed "LATEST"]
                  [lein-cloverage "LATEST"]
                  [lein-kibit "LATEST"]
                  [lein-licenses "0.2.2"]
                  [ns-sort "LATEST"]
                  [lein-mutate "LATEST"] ; source: https://github.com/pithyless/mutant
                  [venantius/yagni "LATEST"]]
        :aliases {"test" ["with-profile" "+test,+kaocha" "run" "-m" "kaocha.runner" "--skip-meta" ":perf"]}}
 :walter/kaocha {:dependencies [[lambdaisland/kaocha "LATEST"]
                                [lambdaisland/kaocha-cloverage "LATEST"]]}}
