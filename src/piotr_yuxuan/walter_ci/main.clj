(ns piotr-yuxuan.walter-ci.main
  (:require [piotr-yuxuan.walter-ci.github :as github]
            [piotr-yuxuan.walter-ci.install :as install]
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
    (github/step config)
    (install/step config)
    (println :all-done)))
