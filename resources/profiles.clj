{:user {:global-vars {*warn-on-reflection* true}
        :plugins [[lein-ancient "LATEST"]
                  [lein-nomis-ns-graph "LATEST"] ; must stay the first, see https://github.com/simon-katz/lein-nomis-ns-graph#troubleshooting
                  [jonase/eastwood "LATEST"]
                  [lein-bikeshed "LATEST"]
                  [lein-cloverage "LATEST"]
                  [lein-kibit "LATEST"]
                  [lein-licenses "0.2.2"]
                  [nvd-clojure/nvd-clojure "LATEST"]
                  [ns-sort "LATEST"]
                  [mutant "LATEST"] ; source: https://github.com/pithyless/mutant
                  [venantius/yagni "LATEST"]]}
 :kaocha {:dependencies [[lambdaisland/kaocha "LATEST"]]}}
