(ns piotr-yuxuan.walter-ci.main
  (:require [piotr-yuxuan.walter-ci.git-workspace :as git-workspace]
            [piotr-yuxuan.walter-ci.github :as github]
            [babashka.process :as process]
            [camel-snake-kebab.core :as csk]
            [clojure.java.io :as io]
            [leiningen.core.project :as leiningen]
            [leiningen.change]
            [malli.core :as m]
            [medley.core :as medley])
  (:gen-class))

(declare increment-version!
         reverse-domain-based-project-group!
         check-license!
         quality-scan
         lint-files!
         new-github-release)

(defn load-config
  []
  {:env (->> (System/getenv)
             (into {})
             (medley/map-keys csk/->kebab-case-keyword))})

(defn lein-test
  [{{:keys [github-workspace]} :env}]
  (let [{:keys [exit]} @(process/process "lein test"
                                         {:out :inherit
                                          :dir (io/file github-workspace)})]
    (assert (zero? exit) "Tests failed.")))

(defn lein-ns-sort
  [{{:keys [github-workspace]} :env :as config}]
  (let [{:keys [exit]} @(process/process "lein ns-sort"
                                         {:out :inherit
                                          :dir (io/file github-workspace)})]
    (assert (zero? exit) "Failed to sort namespaces."))
  (when (git-workspace/stage!-and-need-commit? config)
    (assert (zero? (:exit (git-workspace/commit config "Sort namespaces")))
            "Failed to commit sorted namespaces.")))

(defn lein-update-versions
  [{{:keys [github-workspace]} :env :as config}]
  (let [{:keys [exit]} @(process/process "lein ancient upgrade :all :check-clojure"
                                         {:out :inherit
                                          :dir (io/file github-workspace)})]
    (assert (zero? exit) "Failed to update versions."))
  (when (git-workspace/stage!-and-need-commit? config)
    (assert (zero? (:exit (git-workspace/commit config "Update versions")))
            "Failed to commit updated dependencies.")))

(defn lein-report-vulnerabilities
  [{{:keys [github-workspace]} :env :as config}]
  (with-open [vulnerabilities (io/writer (io/file "./doc/Known vulnerabilities.md"))]
    (let [{:keys [exit]} @(process/process "lein nvd check"
                                           {:out vulnerabilities
                                            :dir (io/file github-workspace)})]
      (assert (zero? exit) "Failed to report vulnerabilities.")))
  (when (git-workspace/stage!-and-need-commit? config)
    (assert (zero? (:exit (git-workspace/commit config "Report vulnerabilities")))
            "Failed to commit vulnerability report.")))

(defn lein-list-licenses
  [{{:keys [github-workspace]} :env :as config}]
  (with-open [licenses (io/writer (io/file "./doc/Licenses.csv"))]
    (let [{:keys [exit]} @(process/process "lein licenses :csv"
                                           {:out licenses
                                            :dir (io/file github-workspace)})]
      (assert (zero? exit) "Failed to list licenses.")))
  (when (git-workspace/stage!-and-need-commit? config)
    (assert (zero? (:exit (git-workspace/commit config "List licenses")))
            "Failed to commit license list.")))

(defn lein-deploy
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
                                              :dir (io/file github-workspace)})]
        (when-not (zero? exit)
          (println "Deployment failed to" deploy-repository))))))

(def InstallConfig
  [:map
   [:env [:map
          [:github-action-path string?]
          [:walter-github-password string?]
          [:github-workspace string?]
          [:walter-git-email string?]
          [:github-actor string?]]]])

(defn walter-install
  [{{:keys [github-workspace github-action-path]} :env :as config}]
  (when-not (m/validate InstallConfig config)
    (println (pr-str (m/explain InstallConfig config)))
    (throw (ex-info "Config invalid" {})))
  (let [source-yml (io/file github-action-path "resources" "walter-ci.standard.yml")
        target-yml (io/file github-workspace ".github" "workflows" "walter-ci.yml")]
    (io/copy source-yml target-yml)
    (if (git-workspace/stage!-and-need-commit? config)
      (do (assert (zero? (:exit (git-workspace/commit config "Update walter-ci.yml")))
                  "Install commit failed")
          (assert (zero? (:exit (git-workspace/push config)))
                  "Install push failed")
          :just-installed)
      :already-installed)))

(defn -main
  [& args]
  (let [config (load-config)]
    (when (= :just-installed (walter-install config))
      (println "Just installed, this has triggered another build.")
      (System/exit 0))
    (github/conform-repository config)
    (lein-test config)
    (lein-ns-sort config)
    (lein-update-versions config)
    (lein-report-vulnerabilities config)
    (lein-list-licenses config)
    (git-workspace/push config)
    (lein-deploy config)
    (println :all-done)))
