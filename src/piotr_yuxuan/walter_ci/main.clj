(ns piotr-yuxuan.walter-ci.main
  (:require [piotr-yuxuan.walter-ci.github :as github]
            [piotr-yuxuan.walter-ci.install :as install]
            [piotr-yuxuan.walter-ci.version :as version]
            [piotr-yuxuan.walter-ci.lein-test :as lein-test]
            [piotr-yuxuan.walter-ci.lein-deploy :as lein-deploy]
            [camel-snake-kebab.core :as csk]
            [medley.core :as medley])
  (:gen-class))

(declare run-tests
         increment-version!
         upgrade-dependencies!
         report-vulnerabilities
         reverse-domain-based-project-group!
         check-license!
         list-licenses
         quality-scan
         lint-files!
         sort-namespaces!
         new-github-release
         deploy-to-clojars)

(defn load-config
  []
  {:env (->> (System/getenv)
             (into {})
             (medley/map-keys csk/->kebab-case-keyword))})

(defn -main
  [& args]
  (let [config (load-config)]
    (when (= :just-installed (install/step config))
      (println "Just installed, this has triggered another build.")
      (System/exit 0))
    (github/step config)
    (version/step config)
    (lein-test/step config)
    (lein-deploy/step config)
    (println :all-done)))
