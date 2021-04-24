(ns piotr-yuxuan.walter-ci.install
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [malli.core :as m])
  (:import (java.io File)))

(defn git-commit-and-push
  [{{:keys [walter-github-password github-action-path github-workspace walter-git-email github-actor]} :env} commit-message ^File file-path]
  (let [commit-exit (shell/sh "git" "commit" "-m" commit-message
                              :dir (io/file github-workspace)
                              :env {"GIT_COMMITTER_NAME" github-actor
                                    "GIT_COMMITTER_EMAIL" walter-git-email
                                    "GIT_AUTHOR_NAME" github-actor
                                    "GIT_AUTHOR_EMAIL" walter-git-email})
        push-exit (shell/sh "git"
                            ;; For this specific line, see https://github.com/actions/checkout/issues/162#issuecomment-590821598
                            "-c" "http.https://github.com/.extraheader="
                            "push"
                            :dir (io/file github-workspace)
                            :env {"WALTER_GITHUB_PASSWORD" walter-github-password
                                  "GIT_ASKPASS" (.getAbsolutePath (io/file github-action-path "resources" "git-askpass.sh"))
                                  "GIT_TRACE" "1"})]
    (assert (zero? (:exit commit-exit)) "Install commit failed")
    (assert (zero? (:exit push-exit)) "Install push failed")
    true))

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
    (io/copy source-yml target-yml)
    (shell/sh "git" "add" (.getAbsolutePath target-yml))
    (let [diff (:out (shell/sh "git" "diff" "--staged"))]
      (println "(empty? diff)" (empty? diff))
      (cond (empty? diff) :already-installed
            (git-commit-and-push config "Update walter-ci.yml" target-yml) :just-installed))))
