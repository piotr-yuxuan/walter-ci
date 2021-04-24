(ns piotr-yuxuan.walter-ci.install
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as io])
  (:import (java.io File)))

(def Config
  [:map
   [:env [:map
          [:github-workspace string?]
          [:github-action-path string?]]]])

(def env
  {:github-workspace "/Users/p2b/src/github.com/piotr-yuxuan/walter-ci"
   :github-action-path "/Users/p2b/src/github.com/piotr-yuxuan/walter-ci"
   :walter-git-email "piotr-yuxuan@users.noreply.github.com"})

(defn git-commit-and-push
  [{{:keys [github-action-path github-workspace walter-git-email github-actor]} :env} commit-message ^File file-path]
  (shell/with-sh-dir (.getAbsolutePath (io/file github-workspace))
    (shell/sh "git" "add" (.getAbsolutePath file-path))
    (println :empty-diff? (seq (:out (shell/sh "git" "diff" "--porcelain"))))
    (when (seq (:out (shell/sh "git" "diff" "--porcelain")))
      (println
        (pr-str
          (shell/sh "git" "commit" "-m" commit-message
                    :env {"GIT_COMMITTER_NAME" github-actor
                          "GIT_COMMITTER_EMAIL" walter-git-email
                          "GIT_AUTHOR_NAME" github-actor
                          "GIT_AUTHOR_EMAIL" walter-git-email})))
      (println
        (pr-str
          (shell/sh "git" "push" "HEAD"
                    :env {"GIT_ASKPASS" (.getAbsolutePath (io/file github-action-path "resources" "git-askpass.sh"))}))))))

(defn step
  [{{:keys [github-workspace github-action-path]} :env :as config}]
  (let [source (.getAbsolutePath (io/file github-action-path "resources" "walter-ci.standard.yml"))
        target (.getAbsolutePath (io/file github-workspace ".github" "workflows" "walter-ci.yml"))]
    (println :copy)
    (io/copy (io/file source) (io/file target))
    (git-commit-and-push config "Update walter-ci.yml" target)))

