✔ (ns piotr-yuxuan.walter-ci.git
?   (:require [piotr-yuxuan.walter-ci.files :refer [->tmp-file with-delete!]]
?             [babashka.process :as process]
?             [safely.core :refer [safely]])
?   (:import (java.io File)
?            (java.nio.file.attribute PosixFilePermissions PosixFilePermission)))
  
✔ (defn ^File askpass
?   "This is kind of a useless indirection. Perhaps ssh would be better?"
?   [secret-name]
✘   (doto (->tmp-file "askpass" "sh" (PosixFilePermissions/asFileAttribute
✘                                      #{PosixFilePermission/OWNER_READ
✘                                        PosixFilePermission/OWNER_WRITE
✘                                        PosixFilePermission/OWNER_EXECUTE}))
✘     (spit (format "#!/bin/sh\necho \"${%s}\"\n" secret-name))))
  
✔ (defn clone
?   [^File working-directory {:keys [github-repository walter-github-password]}]
✘   (with-delete! [askpass (askpass "GIT_PASSWORD")]
✘     (assert (zero? (:exit @(process/process ["git"
?                                              ;; For this specific line, see https://github.com/actions/checkout/issues/162#issuecomment-590821598
?                                              "-c" "http.https://github.com/.extraheader="
✘                                              "clone" "--depth" "1" (str "https://github.com/" github-repository)
?                                              "."]
✘                                             {:out :inherit
?                                              :err :inherit
✘                                              :dir (.getPath working-directory)
✘                                              :env {"GIT_ASKPASS" (.getAbsolutePath askpass)
✘                                                    "GIT_PASSWORD" walter-github-password}})))
?             "Failed to clone GitHub repository")))
  
✔ (defn stage-all
?   [^File working-directory _]
✘   (assert (zero? (:exit @(process/process "git add --all"
✘                                           {:out :inherit
?                                            :err :inherit
✘                                            :dir (.getPath working-directory)})))
?           "Failed ot add updated files to index."))
  
✔ (defn need-commit?
?   [^File working-directory _]
✘   (let [{:keys [out exit]} @(process/process "git diff --staged"
✘                                              {:out :string
?                                               :err :inherit
✘                                               :dir (.getPath working-directory)})]
✘     (assert (zero? exit) "Failed to get staged changes in git.")
✘     (boolean (seq out))))
  
~ (defn commit
?   "Simple `git commit` and nothing else."
?   ;; FIXME https://git-scm.com/book/en/v2/Git-Tools-Signing-Your-Work
?   [^File working-directory {:keys [walter-git-email walter-author-name github-actor]} commit-message]
✘   @(process/process ["git" "commit" "-m" commit-message]
✘                     {:out :inherit
?                      :err :inherit
✘                      :dir (.getPath working-directory)
✘                      :env {"GIT_COMMITTER_NAME" github-actor
✘                            "GIT_COMMITTER_EMAIL" walter-git-email
✘                            "GIT_AUTHOR_NAME" walter-author-name
✘                            "GIT_AUTHOR_EMAIL" walter-git-email}}))
  
✔ (defn push
?   "Simple `git push` and nothing else."
?   [^File working-directory {:keys [walter-github-password walter-git-email walter-author-name github-actor]}]
✘   (with-delete! [askpass (askpass "GIT_PASSWORD")]
✘     (safely
✘       (process/check
✘         @(process/process "git pull --rebase"
✘                           {:out :inherit
?                            :err :inherit
✘                            :dir (.getPath working-directory)
✘                            :env {"GIT_COMMITTER_NAME" github-actor
✘                                  "GIT_COMMITTER_EMAIL" walter-git-email
✘                                  "GIT_AUTHOR_NAME" walter-author-name
✘                                  "GIT_AUTHOR_EMAIL" walter-git-email
✘                                  "GIT_PASSWORD" walter-github-password
✘                                  "GIT_ASKPASS" (.getAbsolutePath askpass)}}))
✘       (process/check
✘         @(process/process ["git"
?                            ;; For this specific line, see https://github.com/actions/checkout/issues/162#issuecomment-590821598
?                            "-c" "http.https://github.com/.extraheader="
?                            "push"]
✘                           {:out :inherit
?                            :err :inherit
✘                            :dir (.getPath working-directory)
✘                            :env {"GIT_PASSWORD" walter-github-password
✘                                  "GIT_ASKPASS" (.getAbsolutePath askpass)}}))
?       :on-error
?       :max-retries 5)))
