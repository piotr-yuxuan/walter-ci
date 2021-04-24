(ns piotr-yuxuan.walter-ci.install
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [malli.core :as m])
  (:import (java.io File)))

(defn git-commit-and-push
  [{{:keys [github-action-path github-workspace walter-git-email github-actor]} :env} commit-message ^File file-path]
  (shell/with-sh-dir (.getAbsolutePath (io/file github-workspace))
    (println "add" (.getAbsolutePath file-path))
    (shell/sh "git" "add" (.getAbsolutePath file-path))
    (let [diff (:out (shell/sh "git" "diff" "--staged"))]
      (println :empty-diff? (empty? diff))
      (println :git/config (shell/sh "git" "config" "--list"))
      (when-not (empty? diff)
        (let [commit-output (shell/sh "git" "commit" "-m" commit-message
                                      :env {"GIT_COMMITTER_NAME" github-actor
                                            "GIT_COMMITTER_EMAIL" walter-git-email
                                            "GIT_AUTHOR_NAME" github-actor
                                            "GIT_AUTHOR_EMAIL" walter-git-email})]
          (println (pr-str :commit-output commit-output)))
        (let [push-output (shell/sh "git" "push"
                                    :env {"GIT_ASKPASS" (.getAbsolutePath (io/file github-action-path "resources" "git-askpass.sh"))
                                          "GIT_TRACE" "1"})]
          (println (pr-str :push-output push-output)))))))

(def Config
  [:map
   [:env [:map
          [:github-action-path string?]
          [:walter-github-password string?]
          [:github-workspace string?]
          [:walter-git-email string?]
          [:github-actor string?]]]])

(defn step
  [{{:keys [github-workspace github-action-path]} :env :as config}]
  (when-not (m/validate Config config)
    (println (pr-str (m/explain Config config)))
    (throw (ex-info "Config invalid" {})))
  (let [source-yml (io/file github-action-path "resources" "walter-ci.standard.yml")
        target-yml (io/file github-workspace ".github" "workflows" "walter-ci.yml")]
    (println :copy)
    (io/copy source-yml target-yml)
    (println :git-commit-and-push)
    (git-commit-and-push config "Update walter-ci.yml" target-yml)))
