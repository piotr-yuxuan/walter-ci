(ns piotr-yuxuan.walter-ci.core
  (:require [piotr-yuxuan.utils :refer [deep-merge]]
            [piotr-yuxuan.walter-ci.files :refer [->file ->tmp-dir ->tmp-file with-delete! delete!]]
            [piotr-yuxuan.walter-ci.git :as git]
            [piotr-yuxuan.walter-ci.github :as github]
            [piotr-yuxuan.walter-ci.secrets :as secret]
            [babashka.process :as process]
            [camel-snake-kebab.core :as csk]
            [clojure.java.io :as io])
  (:import (java.io File)))

(defn update-workflow
  [options ^File workflow-file]
  (with-delete! [working-directory (->tmp-dir "update-workflow")]
    (git/clone working-directory options)
    (io/copy workflow-file (doto (->file working-directory ".github" "workflows" (.getName workflow-file))
                             (io/make-parents)))
    (git/stage-all working-directory options)
    (when (git/need-commit? working-directory options)
      (git/commit working-directory options (format "Update %s" (.getName workflow-file)))
      (git/push working-directory options))))

(defn replicate-walter-ci
  [{:keys [github-action-path managed-repositories] :as config}]
  (doseq [github-repository managed-repositories]
    (let [config+github-repository (assoc config :github-repository github-repository)]
      (doseq [secret-name [:walter-author-name
                           :walter-github-password
                           :walter-git-email]]
        (secret/upsert-value config+github-repository
                             (csk/->SCREAMING_SNAKE_CASE_STRING secret-name)
                             (get config secret-name)))
      (update-workflow config+github-repository (->file github-action-path "resources" "workflows" "walter-ci.yml")))))

(defn list-licenses
  [{{:keys [github-workspace]} :env :as config}]
  (with-open [licenses (io/writer (doto (io/file "./doc/Licenses.csv")
                                    io/make-parents))]
    (let [{:keys [exit]} @(process/process "lein licenses :csv"
                                           {:out licenses
                                            :err :inherit
                                            :dir github-workspace})]
      (assert (zero? exit) "Failed to list licenses.")))
  (git/stage-all github-workspace config)
  (when (git/need-commit? github-workspace config)
    (assert (zero? (:exit (git/commit github-workspace config "List licenses")))
            "Failed to commit license list.")
    (git/push github-workspace config)))

(defn list-vulnerabilities
  [{{:keys [github-workspace]} :env :as config}]
  (with-open [vulnerabilities (io/writer (doto (io/file "./doc/Known vulnerabilities.md")
                                           io/make-parents))]
    (let [{:keys [exit]} @(process/process "lein nvd check"
                                           {:out vulnerabilities
                                            :err :inherit
                                            :dir github-workspace})]
      (assert (zero? exit) "Failed to report vulnerabilities.")))
  (git/stage-all github-workspace config)
  (when (git/need-commit? github-workspace config)
    (assert (zero? (:exit (git/commit github-workspace config "Report vulnerabilities")))
            "Failed to commit vulnerability report.")
    (git/push github-workspace config)))

(defn sort-ns
  [{:keys [github-workspace] :as config}]
  (let [{:keys [exit]} @(process/process "lein ns-sort"
                                         {:out :inherit
                                          :err :inherit
                                          :dir github-workspace})]
    (assert (zero? exit) "Failed to sort namespaces."))
  (git/stage-all github-workspace config)
  (when (git/need-commit? github-workspace config)
    (assert (zero? (:exit (git/commit github-workspace config "Sort namespaces")))
            "Failed to commit sorted namespaces.")
    (git/push github-workspace config)))

(defn update-dependencies-run-tests
  [{:keys [github-workspace] :as config}]
  (let [{:keys [exit]} @(process/process "lein ancient upgrade :all :check-clojure"
                                         {:out :inherit
                                          :err :inherit
                                          :dir github-workspace})]
    (assert (zero? exit) "Failed to update versions."))
  (git/stage-all github-workspace config)
  (when (git/need-commit? github-workspace config)
    (assert (zero? (:exit (git/commit github-workspace config "Update versions")))
            "Failed to commit updated dependencies.")
    (git/push github-workspace config)))

(defn run-tests
  [{:keys [github-workspace]}]
  (let [{:keys [exit]} @(process/process "lein test"
                                         {:out :inherit
                                          :err :inherit
                                          :dir github-workspace})]
    (assert (zero? exit) "Tests failed.")))

#_(defn lein-deploy
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
                                                :err :inherit
                                                :dir github-workspace})]
          (when-not (zero? exit)
            (println "Deployment failed to" deploy-repository))))))

(defn start
  [{:keys [input-command] :as config}]
  (cond (= :conform-repository input-command) (github/conform-repository config)
        (= :list-licences input-command) (list-licenses config)
        (= :list-vulnerabilities input-command) (list-vulnerabilities config)
        (= :replicate-walter-ci input-command) (replicate-walter-ci config)
        (= :sort-ns input-command) (sort-ns config)
        (= :run-tests input-command) (run-tests config)
        (= :update-dependencies-run-tests input-command) (update-dependencies-run-tests config)))
