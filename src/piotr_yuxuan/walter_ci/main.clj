(ns piotr-yuxuan.walter-ci.main
  (:require [leiningen.core.project :as leiningen]
            [medley.core :as medley]
            [jsonista.core :as json])
  (:gen-class))

(defn project-description
  [{:keys [project-path] :as overrides}]
  (merge {:version nil
          :main-function nil
          :docker-file nil
          :license {:file nil
                    :type nil}
          :walter-ci {:installed? nil
                      :standard? nil}}
         overrides))

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

(defn clojure-library-leiningen
  [project-path]
  (-> (project-description {:project-path project-path})
      (run-tests)
      (increment-version!)
      (upgrade-dependencies!)
      (report-vulnerabilities)
      (reverse-domain-based-project-group!)
      (check-license!)
      (list-licenses)
      (quality-scan)
      (lint-files!)
      (sort-namespaces!)
      (new-github-release)
      (deploy-to-clojars)))

(defn -main
  [& args]
  )
