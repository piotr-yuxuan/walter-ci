(ns piotr-yuxuan.walter-ci.core
  (:require [piotr-yuxuan.malli-cli.utils :refer [deep-merge]]
            [piotr-yuxuan.walter-ci.files :refer [->file ->tmp-dir with-delete!]]
            [piotr-yuxuan.walter-ci.git :as git]
            [piotr-yuxuan.walter-ci.github :as github]
            [piotr-yuxuan.walter-ci.secrets :as secrets]
            [babashka.process :as process]
            [camel-snake-kebab.core :as csk]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [safely.core :refer [safely-fn]]
            [yaml.core :as yaml]))

(defn update-workflow
  [{:keys [working-directory] :as config} ^String target-yml ^String yml]
  (println "target-yml file" (->file working-directory ".github" "workflows" target-yml))
  (println "yml string length" (count yml))
  (println "target-yml before spit" (count (slurp (->file working-directory ".github" "workflows" target-yml))))
  (doto (->file working-directory ".github" "workflows" target-yml)
    (io/make-parents)
    (spit yml))
  (println "target-yml after spit" (count (slurp (->file working-directory ".github" "workflows" target-yml))))
  (git/stage-all working-directory config)
  (println "git/need-commit?" (git/need-commit? working-directory config))
  (when (git/need-commit? working-directory config)
    (git/commit working-directory config (format "Update %s" target-yml))
    (git/push working-directory config)))

(defn cmd-retry
  [{:keys [^String walter-try ^String walter-before-retry]}]
  (apply safely-fn
         #(let [proc (process/process walter-try
                                      {:out :inherit
                                       :err :inherit})]
            (when (and walter-before-retry
                       (not (-> @proc :exit zero?)))
              (process/process walter-before-retry
                               {:out :inherit
                                :err :inherit}))
            (process/check proc))
         (mapcat vec {:max-retries 5})))

(def unix-cli-line-breaker
  (str \space \\ \newline \space \space))

(def walter-ci-github-repository
  "piotr-yuxuan/walter-ci")

(defn deploy-job
  [github-repository]
  {:runs-on "ubuntu-latest"
   :environment {:name (if (= walter-ci-github-repository github-repository)
                         :self
                         :production)}
   :steps [{:uses "piotr-yuxuan/walter-ci@main"}
           {:run (reduce (fn [acc secret-name]
                           (str/join unix-cli-line-breaker [acc (format "--secret-name \"%s\"" secret-name)]))
                         (format "walter forward-secret --github-repository %s" github-repository)
                         ["WALTER_ACTOR"
                          "WALTER_AUTHOR_NAME"
                          "WALTER_CLOJARS_PASSWORD"
                          "WALTER_CLOJARS_USERNAME"
                          "WALTER_GITHUB_PASSWORD"
                          "WALTER_GIT_EMAIL"])}
           {:run (reduce (fn [acc [source-edn target-yml]]
                           (str/join unix-cli-line-breaker [acc (format "--source-edn \"%s\" --target-yml \"%s\"" source-edn target-yml)]))
                         (format "walter install-workflow --github-repository %s" github-repository)
                         [["$HOME/.walter-ci/edn-sources/workflows/walter-ci.edn" "walter-ci.yml"]
                          ["$HOME/.walter-ci/edn-sources/workflows/walter-cd.edn" "walter-cd.yml"]
                          ["$HOME/.walter-ci/edn-sources/workflows/walter-perf.edn" "walter-perf.yml"]])}]})

(defn walter-readers
  [steps managed-repositories]
  (letfn [(read-step [value]
            (cond (keyword? value)
                  (read-step [value {}])

                  (vector? value)
                  (let [[step-name step-opts] value]
                    (deep-merge (get steps step-name)
                                step-opts))))
          (wrap-in-job [steps]
            {:runs-on "ubuntu-latest"
             :steps steps})]
    {'step read-step
     'job/wrap wrap-in-job
     'cmd/retry cmd-retry
     'walter/env (fn [{:keys [git walter walter-version version-to-release deploy-identities]}]
                   (merge (sorted-map)
                          (when git
                            {:GIT_COMMITTER_NAME "${{ secrets.WALTER_AUTHOR_NAME }}"
                             :GIT_COMMITTER_EMAIL "${{ secrets.WALTER_GIT_EMAIL }}"
                             :GIT_AUTHOR_NAME "${{ secrets.WALTER_AUTHOR_NAME }}"
                             :GIT_AUTHOR_EMAIL "${{ secrets.WALTER_GIT_EMAIL }}"
                             :GIT_PASSWORD "${{ secrets.GITHUB_TOKEN }}"
                             :GIT_ASKPASS "${HOME}/.walter-ci/bin/askpass.sh"})
                          (when walter
                            {:WALTER_ACTOR "${{ secrets.WALTER_ACTOR }}"
                             :WALTER_AUTHOR_NAME "${{ secrets.WALTER_AUTHOR_NAME }}"
                             :WALTER_GITHUB_PASSWORD "${{ secrets.WALTER_GITHUB_PASSWORD }}"
                             :WALTER_GIT_EMAIL "${{ secrets.WALTER_GIT_EMAIL }}"})
                          (when walter-version
                            {:WALTER_VERSION "${{ github.event.inputs.walter-version }}"})
                          (when version-to-release
                            {:VERSION_TO_RELEASE "${{ github.event.inputs.version-to-release }}"})
                          (when deploy-identities
                            {:WALTER_ACTOR "${{ secrets.WALTER_ACTOR }}"
                             :WALTER_GITHUB_PASSWORD "${{ secrets.WALTER_GITHUB_PASSWORD }}"
                             :WALTER_CLOJARS_PASSWORD "${{ secrets.WALTER_CLOJARS_PASSWORD }}"
                             :WALTER_CLOJARS_USERNAME "${{ secrets.WALTER_CLOJARS_USERNAME }}"})))
     'line/join #(str/join \newline %)
     'line/break #(str/join unix-cli-line-breaker %)
     'str/join #(str/join \space %)
     'walter/install-jobs (fn [_]
                            (reduce #(assoc %1 (str/replace %2 "/" "-") (deploy-job %2))
                                    (sorted-map)
                                    (sort managed-repositories)))}))

(def yml-header
  "# This file is maintained by Walter CI, and may be rewritten.\n# https://github.com/piotr-yuxuan/walter-ci\n#\n# You are free to remove this project from Walter CI realm by opening\n# a PR. You may also create another workflow besides this one.\n\n")

(defn steps+edn->yml
  [steps managed-repositories source-edn]
  (->> source-edn
       (edn/read-string {:readers (walter-readers steps managed-repositories)})
       (#(yaml/generate-string % :dumper-options {:flow-style :block
                                                  :split-lines false
                                                  :width 1e3}))
       (str yml-header)))

(defn steps+edn->write-to-yml-file!
  [steps managed-repositories source-edn target-yml]
  (spit target-yml (steps+edn->yml steps managed-repositories (slurp source-edn))))

(defn forward-secret
  [{:keys [secret-names] :as config}]
  (doseq [s secret-names]
    (secrets/upsert-value config
                          s
                          (get config (csk/->kebab-case-keyword s)))))

(defn install-workflow
  [{:keys [source+target-pairs] :as config}]
  (with-delete! [working-directory (->tmp-dir "update-workflow")]
    (git/clone working-directory config)
    (doseq [{:keys [source-edn target-yml]} source+target-pairs]
      (let [steps (edn/read-string {:readers {'line/join #(str/join \newline %)
                                              'line/break #(str/join unix-cli-line-breaker %)
                                              'str/join #(str/join \space %)}}
                                   (slurp (io/resource "steps.edn")))
            config+working-directory (assoc config :working-directory working-directory)]
        (->> (slurp source-edn)
             (steps+edn->yml steps nil)
             (update-workflow config+working-directory target-yml))))))

(defn update-git-ignore
  [{:keys [github-workspace]}]
  (println "exists? template-gitignore" (boolean (io/resource "template-gitignore")))
  (println "exists? .gitignore" (.exists (->file github-workspace ".gitignore")))
  (let [required-entries (set (line-seq (io/reader (io/resource "template-gitignore"))))
        current-entries (set (line-seq (io/reader (->file github-workspace ".gitignore"))))
        missing-entries (sort (set/difference required-entries current-entries))
        gitignore (->file github-workspace ".gitignore")
        current-git-ignore (str/trim (slurp gitignore))]
    (spit gitignore
          (str current-git-ignore
               (System/lineSeparator) (System/lineSeparator)
               (str/join (System/lineSeparator) missing-entries))
          :append false)))

;; When install Walter, we should install clojure CLI, lein CLI, practicalli configs, and lein default profiles.
;; So Walter is just a bunch of helpers around basic Bash script:
;; - Lein and profiles
;; - Clojure CLI and clojure-deps-edn aliases and configuration
;; - Walter executable commands

(def source+targets
  [;["edn-sources/action.edn" "action.yml"]
   ["edn-sources/workflows/deploy.edn" ".github/workflows/deploy.yml"]
   ;["edn-sources/workflows/generate.edn" ".github/workflows/generate.yml"]
   ["edn-sources/workflows/walter-cd.edn" ".github/workflows/walter-cd.yml"]
   ["edn-sources/workflows/walter-ci.edn" ".github/workflows/walter-ci.yml"]
   ["edn-sources/workflows/walter-perf.edn" ".github/workflows/walter-perf.yml"]])

(comment
  (let [steps (edn/read-string {:readers {'line/join #(str/join \newline %)
                                          'line/break #(str/join unix-cli-line-breaker %)
                                          'str/join #(str/join \space %)}}
                               (slurp (io/resource "steps.edn")))
        managed-repositories (edn/read-string (slurp "managed-repositories.edn"))]
    (doseq [[source-edn target-yml] source+targets]
      (steps+edn->write-to-yml-file! steps managed-repositories source-edn target-yml))))

(defmulti start :command)
(defmethod start :conform-repository [config] (github/conform-repository config))
(defmethod start :retry [config] (cmd-retry config))
(defmethod start :update-git-ignore [config] (update-git-ignore config))
(defmethod start :forward-secret [config] (forward-secret config))
(defmethod start :install-workflow [config] (install-workflow config))
