(ns piotr-yuxuan.walter-ci.main
  (:require [piotr-yuxuan.walter-ci.github :as github]
            [piotr-yuxuan.walter-ci.install :as install])
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
  {:env (into {} (System/getenv))})

(defn -main
  [& args]
  (let [config (load-config)]
    (github/step config)
    (install/step config)
    (println :all-done)))
