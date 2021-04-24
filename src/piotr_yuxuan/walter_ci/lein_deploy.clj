(ns piotr-yuxuan.walter-ci.lein-deploy
  (:require [babashka.process :as process]
            [leiningen.deploy]
            [clojure.java.io :as io]
            [leiningen.core.project :as leiningen]))

(defn step
  [{{:keys [github-workspace]} :env}]
  (let [leiningen-project (-> (io/file github-workspace "project.clj")
                              (.getAbsolutePath)
                              (leiningen/read [:deploy]))
        deploy-repositories (->> leiningen-project
                                 :deploy-repositories
                                 (map first)
                                 seq)]
    (if deploy-repositories
      (println "Deploying to repositories:" deploy-repositories)
      (println "No deploy repository found, not deploying."))
    (doseq [deploy-repository deploy-repositories]
      (let [{:keys [exit]} @(process/process ["lein" "deploy" deploy-repository]
                                             {:out :inherit
                                              :dir (io/file github-workspace)})]
        (when-not (zero? exit)
          (println "Deployment failed to" deploy-repository))))))
