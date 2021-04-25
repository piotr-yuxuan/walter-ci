(ns piotr-yuxuan.walter-ci.main
  (:require [piotr-yuxuan.walter-ci.git-workspace :as git-workspace]
            [piotr-yuxuan.walter-ci.github :as github]
            [babashka.process :as process]
            [camel-snake-kebab.core :as csk]
            [clojure.java.io :as io]
            [leiningen.core.project :as leiningen]
            [malli.core :as m]
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
    (assert (zero? exit) "Failed to sort namespaces.")
    (if (git-workspace/stage!-and-need-commit? config)
      (do (assert (zero? (:exit (git-workspace/commit config "Sort namespaces")))
                  "Failed to commit sorted namespaces.")
          :just-installed)
      :already-installed)))

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
    (github/step config)
    (lein-test config)
    (lein-ns-sort config)
    (git-workspace/push config)
    (lein-deploy config)
    (println :all-done)))
