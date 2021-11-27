(ns piotr-yuxuan.walter-ci.git
  (:require [babashka.process :as process]
            [piotr-yuxuan.walter-ci.files :refer [->file ->tmp-dir ->tmp-file with-delete!]])
  (:import (java.io File)
           (java.nio.file.attribute PosixFilePermissions PosixFilePermission)))

(defn ^File askpass
  "This is kind of a useless indirection. Perhaps ssh would be better?"
  [secret-name]
  (doto (->tmp-file "askpass" "sh" (PosixFilePermissions/asFileAttribute
                                     #{PosixFilePermission/OWNER_READ
                                       PosixFilePermission/OWNER_WRITE
                                       PosixFilePermission/OWNER_EXECUTE}))
    (spit (format "#!/bin/sh\necho \"${%s}\"\n" secret-name))))

(defn clone
  [working-directory {:keys [github-repository walter-github-password]}]
  (println ::clone)
  (with-delete! [askpass (askpass "GIT_PASSWORD")]
    (assert (zero? (:exit @(process/process ["git"
                                             ;; For this specific line, see https://github.com/actions/checkout/issues/162#issuecomment-590821598
                                             "-c" "http.https://github.com/.extraheader="
                                             "clone" "--depth" "1" (str "https://github.com/" github-repository)
                                             "."]
                                            {:out :inherit
                                             :err :inherit
                                             :dir working-directory
                                             :env {"GIT_ASKPASS" (.getAbsolutePath askpass)
                                                   "GIT_PASSWORD" walter-github-password}})))
            "Failed to clone GitHub repository")))

(defn stage-all
  [working-directory _]
  (println ::stage-all)
  (assert (zero? (:exit @(process/process "git add --all"
                                          {:out :inherit
                                           :err :inherit
                                           :dir working-directory})))
          "Failed ot add updated files to index."))

(defn need-commit?
  [working-directory _]
  (println ::need-commit?)
  (let [{:keys [out exit]} @(process/process "git diff --staged"
                                             {:out :slurp
                                              :err :inherit
                                              :dir working-directory})]
    (assert (zero? exit) "Failed to get staged changes in git.")
    (boolean (seq out))))

(defn commit
  "Simple `git commit` and nothing else."
  ;; FIXME https://git-scm.com/book/en/v2/Git-Tools-Signing-Your-Work
  [working-directory {:keys [walter-git-email walter-author-name github-actor]} commit-message]
  (println ::commit)
  @(process/process ["git" "commit" "-m" commit-message]
                    {:out :inherit
                     :err :inherit
                     :dir working-directory
                     :env {"GIT_COMMITTER_NAME" github-actor
                           "GIT_COMMITTER_EMAIL" walter-git-email
                           "GIT_AUTHOR_NAME" walter-author-name
                           "GIT_AUTHOR_EMAIL" walter-git-email}}))

(defn push
  "Simple `git push` and nothing else."
  [working-directory {:keys [walter-github-password]}]
  (println ::push)
  (with-delete! [askpass (askpass "GIT_PASSWORD")]
    @(process/process ["git"
                       ;; For this specific line, see https://github.com/actions/checkout/issues/162#issuecomment-590821598
                       "-c" "http.https://github.com/.extraheader="
                       "push"]
                      {:out :inherit
                       :err :inherit
                       :dir working-directory
                       :env {"GIT_PASSWORD" walter-github-password
                             "GIT_ASKPASS" (.getAbsolutePath askpass)}})))
