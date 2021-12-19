✔ (ns piotr-yuxuan.walter-ci.core
?   (:require [piotr-yuxuan.utils :refer [deep-merge]]
?             [piotr-yuxuan.walter-ci.files :refer [->file ->tmp-dir ->tmp-file with-delete! delete! copy!]]
?             [piotr-yuxuan.walter-ci.git :as git]
?             [piotr-yuxuan.walter-ci.github :as github]
?             [piotr-yuxuan.walter-ci.secrets :as secret]
?             [babashka.process :as process]
?             [camel-snake-kebab.core :as csk]
?             [clojure.java.io :as io]
?             [clojure.set :as set]
?             [clojure.string :as str]
?             [leiningen.core.project :as leiningen])
?   (:import (java.io File)))
  
✔ (defn update-workflow
?   [options ^File workflow-file]
✘   (with-delete! [working-directory (->tmp-dir "update-workflow")]
✘     (git/clone working-directory options)
✘     (io/copy workflow-file (doto (->file working-directory ".github" "workflows" (.getName workflow-file))
✘                              (io/make-parents)))
✘     (git/stage-all working-directory options)
✘     (when (git/need-commit? working-directory options)
✘       (git/commit working-directory options (format "Update %s" (.getName workflow-file)))
✘       (git/push working-directory options))))
  
✔ (defn replicate-walter-ci
?   [{:keys [github-action-path managed-repositories] :as config}]
✘   (doseq [github-repository managed-repositories]
✘     (let [config+github-repository (assoc config :github-repository github-repository)]
✘       (doseq [secret-name [:walter-author-name
?                            :walter-github-password
?                            :walter-git-email]]
✘         (secret/upsert-value config+github-repository
✘                              (csk/->SCREAMING_SNAKE_CASE_STRING secret-name)
✘                              (get config secret-name)))
✘       (update-workflow config+github-repository (->file github-action-path "resources" "workflows" "walter-ci.yml")))))
  
✔ (defn list-licenses
?   [{:keys [^File github-workspace] :as config}]
✘   (with-open [licenses (io/writer (doto (io/file "./doc/Licenses.csv")
✘                                     io/make-parents))]
✘     (let [{:keys [exit]} @(process/process "lein licenses :csv"
✘                                            {:out licenses
?                                             :err :inherit
✘                                             :dir (.getPath github-workspace)})]
✘       (assert (zero? exit) "Failed to list licenses.")))
✘   (git/stage-all github-workspace config)
✘   (when (git/need-commit? github-workspace config)
✘     (assert (zero? (:exit (git/commit github-workspace config "List licenses")))
?             "Failed to commit license list.")
✘     (git/push github-workspace config)))
  
✔ (defn list-vulnerabilities
?   [{:keys [^File github-workspace] :as config}]
✘   (let [^File github-workspace (io/file ".")
✘         ^File txt-report (doto (io/file "./doc/Known vulnerabilities.txt") io/make-parents)]
✘     (delete! (io/file "./doc/Known vulnerabilities.md") :quiet)
✘     (delete! (io/file "./doc/Known vulnerabilities.csv") :quiet)
✘     (delete! (io/file "./doc/KNOWN_VULNERABILITIES.md") :quiet)
✘     (with-open [txt-report-writer (io/writer txt-report)]
✘       @(process/process ["clojure" "-Sdeps" (pr-str {:aliases {:nvd {:extra-deps {'nvd-clojure/nvd-clojure {:mvn/version "LATEST"}}}}})
?                          "-M:nvd"
?                          "-m" "nvd.task.check"]
✘                         {:out txt-report-writer
?                          :err :inherit
✘                          :dir (.getPath github-workspace)}))
?     ;; Remove ANSI colour codes.
✘     (spit txt-report (str/replace (slurp txt-report) #"\x1b\[[0-9;]*m" "")))
✘   (git/stage-all github-workspace config)
✘   (when (git/need-commit? github-workspace config)
✘     (assert (zero? (:exit (git/commit github-workspace config "Report vulnerabilities")))
?             "Failed to commit vulnerability report.")
✘     (git/push github-workspace config)))
  
✔ (defn clojure-git-ignore
?   [{:keys [github-action-path github-workspace] :as options}]
✘   (let [required-entries (set (line-seq (io/reader (->file github-action-path "resources" ".template-gitignore"))))
✘         current-entries (set (line-seq (io/reader (->file github-workspace ".gitignore"))))
✘         missing-entries (sort (set/difference required-entries current-entries))]
✘     (spit (->file github-workspace ".gitignore")
✘           (str/join (System/lineSeparator) missing-entries)
?           :append true)
✘     (git/stage-all github-workspace options)
✘     (when (git/need-commit? github-workspace options)
✘       (git/commit github-workspace options (format "Update .gitignore"))
✘       (git/push github-workspace options))))
  
~ (defn code-coverage
?   [{:keys [^File github-workspace] :as options}]
✘   @(process/process ["lein" "cloverage" "--output" (->file github-workspace "doc" "code-coverage") "--text"]
✘                     {:out :inherit
?                      :err :inherit
✘                      :dir (.getPath github-workspace)})
✘   (git/stage-all github-workspace options)
✘   (when (git/need-commit? github-workspace options)
✘     (git/commit github-workspace options (format "Update code coverage"))
✘     (git/push github-workspace options)))
  
✔ (defn rewrite-idiomatic-simple
?   [{:keys [^File github-workspace] :as options}]
✘   (let [{:keys [exit]} @(process/process "lein kibit --replace"
✘                                          {:out :inherit
?                                           :err :inherit
✘                                           :dir (.getPath github-workspace)})]
✘     (assert (zero? exit) "Failed to apply kibit advices"))
✘   (git/stage-all github-workspace options)
✘   (when (git/need-commit? github-workspace options)
✘     (git/commit github-workspace options (format "More idiomatic code"))
✘     (git/push github-workspace options)))
  
✔ (defn sort-ns
?   [{:keys [^File github-workspace] :as config}]
✘   (let [{:keys [exit]} @(process/process "lein ns-sort"
✘                                          {:out :inherit
?                                           :err :inherit
✘                                           :dir (.getPath github-workspace)})]
✘     (assert (zero? exit) "Failed to sort namespaces."))
✘   (git/stage-all github-workspace config)
✘   (when (git/need-commit? github-workspace config)
✘     (assert (zero? (:exit (git/commit github-workspace config "Sort namespaces")))
?             "Failed to commit sorted namespaces.")
✘     (git/push github-workspace config)))
  
✔ (defn update-dependencies-run-tests
?   [{:keys [^File github-workspace] :as config}]
✘   (let [{:keys [exit]} @(process/process "lein ancient upgrade :all :check-clojure"
✘                                          {:out :inherit
?                                           :err :inherit
✘                                           :dir (.getPath github-workspace)})]
✘     (assert (zero? exit) "Failed to update versions."))
✘   (git/stage-all github-workspace config)
✘   (when (git/need-commit? github-workspace config)
✘     (assert (zero? (:exit (git/commit github-workspace config "Update versions")))
?             "Failed to commit updated dependencies.")
✘     (git/push github-workspace config)))
  
✔ (defn run-tests
?   [{:keys [^File github-workspace]}]
✘   (let [{:keys [exit]} @(process/process "lein test"
✘                                          {:out :inherit
?                                           :err :inherit
✘                                           :dir (.getPath github-workspace)})]
✘     (assert (zero? exit) "Tests failed.")))
  
✔ (defn package-deploy-artifacts
?   [{:keys [github-workspace]}]
✘   (let [leiningen-project (-> (io/file github-workspace "project.clj")
✘                               (.getAbsolutePath)
✘                               (leiningen/read [:deploy]))
✘         deploy-repositories (->> leiningen-project
?                                  :deploy-repositories
✘                                  (map first)
✘                                  seq)]
✘     (if deploy-repositories
✘       (println "Deploying to repositories:" deploy-repositories)
✘       (println "No deploy repositories found, not deploying."))
✘     (doseq [deploy-repository deploy-repositories]
✘       (println :dry-run ["lein" "deploy" deploy-repository])
?       #_(let [{:keys [exit]} @(process/process ["lein" "deploy" deploy-repository]
?                                                {:out :inherit
?                                                 :err :inherit
?                                                 :dir (.getPath github-workspace)})]
?           (when-not (zero? exit)
?             (println "Deployment failed to" deploy-repository))))))
  
✔ (defn start
?   [{:keys [input-command] :as config}]
✘   (cond (= :clojure-git-ignore input-command) (clojure-git-ignore config)
✘         (= :code-coverage input-command) (code-coverage config)
✘         (= :conform-repository input-command) (github/conform-repository config)
✘         (= :list-licences input-command) (list-licenses config)
✘         (= :list-vulnerabilities input-command) (list-vulnerabilities config)
✘         (= :replicate-walter-ci input-command) (replicate-walter-ci config)
✘         (= :rewrite-idiomatic-simple input-command) (rewrite-idiomatic-simple config)
✘         (= :run-tests input-command) (run-tests config)
✘         (= :sort-ns input-command) (sort-ns config)
✘         (= :package-deploy-artifacts input-command) (package-deploy-artifacts config)
✘         (= :update-dependencies-run-tests input-command) (update-dependencies-run-tests config)))
