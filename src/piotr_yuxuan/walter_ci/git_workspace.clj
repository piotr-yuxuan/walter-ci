(ns piotr-yuxuan.walter-ci.git-workspace
  (:require [babashka.process :as process]
            [clojure.java.io :as io]))

(defn stage!-and-need-commit?
  [{{:keys [github-workspace]} :env}]
  (assert (zero? (:exit @(process/process "git add --all"
                                          {:out :inherit
                                           :err :inherit
                                           :dir github-workspace})))
          "Failed ot add updated files to index.")
  (let [{:keys [out exit]} @(process/process "git diff --staged"
                                             {:out :slurp
                                              :err :inherit
                                              :dir github-workspace})]
    (assert (zero? exit) "Failed to get staged changes in git.")
    (not (seq out))))

(defn commit
  "Simple `git commit` and nothing else."
  [{{:keys [walter-git-email github-actor github-workspace]} :env} commit-message]
  @(process/process ["git" "commit" "-m" commit-message]
                    {:out :inherit
                     :err :inherit
                     :dir github-workspace
                     :env {"GIT_COMMITTER_NAME" github-actor
                           "GIT_COMMITTER_EMAIL" walter-git-email
                           "GIT_AUTHOR_NAME" "Walter CI"
                           "GIT_AUTHOR_EMAIL" walter-git-email}}))

(defn push
  "Simple `git push` and nothing else."
  [{{:keys [walter-github-password github-action-path github-workspace]} :env}]
  @(process/process ["git"
                     ;; For this specific line, see https://github.com/actions/checkout/issues/162#issuecomment-590821598
                     "-c" "http.https://github.com/.extraheader="
                     "push"]
                    {:out :inherit
                     :err :inherit
                     :dir github-workspace
                     :env {"WALTER_GITHUB_PASSWORD" walter-github-password
                           "GIT_ASKPASS" (.getAbsolutePath (io/file github-action-path "resources" "git-askpass.sh"))}}))
