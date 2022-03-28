✔ (ns piotr-yuxuan.walter-ci.core
?   (:require [piotr-yuxuan.malli-cli.utils :refer [deep-merge]]
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
  
✔ (defn deploy-walter-ci
?   [{:keys [github-action-path managed-repositories] :as config}]
✘   (doseq [github-repository managed-repositories]
✘     (let [config+github-repository (assoc config :github-repository github-repository)]
✘       (doseq [secret-name [:walter-author-name
?                            :walter-github-password
?                            :walter-git-email]]
✘         (secret/upsert-value config+github-repository
✘                              (csk/->SCREAMING_SNAKE_CASE_STRING secret-name)
✘                              (get config secret-name)))
✘       (doseq [workflow-file [(->file github-action-path "resources" "workflows" "walter-cd.yml")
✘                              (->file github-action-path "resources" "workflows" "walter-ci.yml")]]
✘         (update-workflow config+github-repository workflow-file)))))
  
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
  
✔ (defn wrap-escaped-around
?   [^String s & qs]
✘   {:pre [(every? #{:single-quote :double-quote} qs)]}
✘   (let [quotes {:single-quote \'
?                 :double-quote \"}]
✘     (reduce (fn [s q] (str q s q)) s (map quotes qs))))
  
✔ (defn install-nvd
?   [{:keys [^File github-workspace]}]
✘   (assert (zero? (:exit @(process/process "clojure -Ttools install nvd-clojure/nvd-clojure '{:mvn/version \"RELEASE\"}' :as nvd"
✘                                           {:out :inherit
?                                            :err :inherit
✘                                            :dir (.getPath github-workspace)})))
?           "Failed to install nvd-clojure"))
  
✔ (defn nvd-leiningen-classpath
?   [{:keys [^File github-workspace]}]
?   ;; For deps.edn it'd be `clojure -Spath -A:any:aliases`.
✘   (-> (process/process "lein with-profile -user,-dev classpath"
✘                        {:out :string
?                         :err :string
✘                         :dir (.getPath github-workspace)})
✘       ^babashka.process.Process deref
?       .out
✘       (wrap-escaped-around :double-quote
?                            :single-quote
?                            :double-quote)
✘       pr-str))
  
✔ (defn list-vulnerabilities
?   [{:keys [^File github-workspace] :as config}]
✘   (let [^File txt-report (doto (io/file github-workspace "doc/Known vulnerabilities.txt") io/make-parents)]
✘     (with-open [txt-report-writer (io/writer txt-report)]
✘       @(process/process ["clojure" "-J-Dclojure.main.report=stderr" "-Tnvd" "nvd.task/check" :classpath (nvd-leiningen-classpath config)]
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
✘           (str/join \n missing-entries)
?           :append true)
✘     (git/stage-all github-workspace options)
✘     (when (git/need-commit? github-workspace options)
✘       (git/commit github-workspace options (format "Update .gitignore"))
✘       (git/push github-workspace options))))
  
~ (defn code-coverage
?   [{:keys [^File github-workspace] :as options}]
✘   @(process/process ["lein" "cloverage" "--output" (->file github-workspace "doc" "code-coverage") "--text" "--no-html"]
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
✘   (let [{:keys [exit]} @(process/process "lein ancient upgrade :all :recursive :check-clojure :allow-qualified"
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
  
✔ (def commands
✔   {:clojure-git-ignore clojure-git-ignore
✔    :code-coverage code-coverage
✔    :conform-repository github/conform-repository
✔    :list-licences list-licenses
✔    :list-vulnerabilities (juxt install-nvd list-vulnerabilities)
✔    :package-deploy-artifacts package-deploy-artifacts
✔    :deploy-walter-ci deploy-walter-ci
✔    :rewrite-idiomatic-simple rewrite-idiomatic-simple
✔    :run-tests run-tests
✔    :sort-ns sort-ns
✔    :update-dependencies-run-tests update-dependencies-run-tests})
  
✔ (defn start
?   [{:keys [input-command] :as config}]
✘   ((get commands input-command) config))
