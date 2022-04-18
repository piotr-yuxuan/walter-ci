(ns piotr-yuxuan.walter-ci.config
  (:require [piotr-yuxuan.malli-cli :as malli-cli]
            [piotr-yuxuan.malli-cli.utils :refer [deep-merge]]
            [piotr-yuxuan.walter-ci.files :refer [->dir]]
            [clojure.string :as str]
            [malli.core :as m]
            [malli.transform :as mt])
  (:import (java.time ZonedDateTime)))

(def EnvironmentVariables
  "These options include all the possible environment variables from GitHub ([link](https://docs.github.com/en/actions/learn-github-actions/environment-variables)). The ones we don't need now are commented out."
  [:map {:decode/args-transformer malli-cli/args-transformer}
   #_[:ci [boolean? {:description "Always set to true."
                     :env-var "CI"
                     :arg-number 0}]]
   #_[:github-workflow [any? {:description "The name of the workflow."
                              :env-var "GITHUB_WORKFLOW"}]]
   #_[:github-run-id [any? {:description "A unique number for each run within a repository. This number does not change if you re-run the workflow run."
                            :env-var "GITHUB_RUN_ID"}]]
   #_[:github-run-number [any? {:description "A unique number for each run of a particular workflow in a repository. This number begins at 1 for the workflow's first run, and increments with each new run. This number does not change if you re-run the workflow run."
                                :env-var "GITHUB_RUN_NUMBER"}]]
   #_[:github-job [any? {:description "The job-id of the current job."
                         :env-var "GITHUB_JOB"}]]
   #_[:github-action [any? {:description "The unique identifier (id) of the action."
                            :env-var "GITHUB_ACTION"}]]
   #_[:github-head-ref [any? {:description "Only set for pull request events. The name of the head branch."
                              :env-var "GITHUB_HEAD_REF"}]]
   #_[:github-base-ref [any? {:description "Only set for pull request events. The name of the base branch."
                              :env-var "GITHUB_BASE_REF"}]]
   #_[:github-graphql-url [any? {:description "Returns the GraphQL API URL. For example: https://api.github.com/graphql."
                                 :env-var "GITHUB_GRAPHQL_URL"}]]
   #_[:runner-name [any? {:description "The name of the runner executing the job."
                          :env-var "RUNNER_NAME"}]]
   #_[:runner-os [any? {:description "The operating system of the runner executing the job. Possible values are Linux, Windows, or macOS."
                        :env-var "RUNNER_OS"}]]
   #_[:runner-temp [:fn {:description "The path to a temporary directory on the runner. This directory is emptied at the beginning and end of each job. Note that files will not be removed if the runner's user account does not have permission to delete them."
                         :env-var "RUNNER_TEMP"
                         :decode/string ->dir}
                    ->dir]]
   #_[:runner-tool-cache [:fn {:description "The path to the directory containing preinstalled tools for GitHub-hosted runners. For more information, see \"Specifications for GitHub-hosted runners\"."
                               :env-var "RUNNER_TOOL_CACHE"
                               :decode/string ->dir}
                          ->dir]]
   #_[:github-actions [boolean? {:description "Always set to true when GitHub Actions is running the workflow. You can use this variable to differentiate when tests are being run locally or by GitHub Actions."
                                 :env-var "GITHUB_ACTIONS"
                                 :arg-number 0}]]
   #_[:github-event-name [any? {:description "The name of the webhook event that triggered the workflow."
                                :env-var "GITHUB_EVENT_NAME"}]]
   #_[:github-event-path [:fn {:description "The path of the file with the complete webhook event payload. For example, /github/workflow/event.json."
                               :env-var "GITHUB_EVENT_PATH"
                               :decode/string ->dir}
                          ->dir]]
   #_[:github-sha [any? {:description "The commit SHA that triggered the workflow. For example, ffac537e6cbbf934b08745a378932722df287a53."
                         :env-var "GITHUB_SHA"}]]
   #_[:github-ref [any? {:description "The branch or tag ref that triggered the workflow. For example, refs/heads/feature-branch-1. If neither a branch or tag is available for the event type, the variable will not exist."
                         :env-var "GITHUB_REF"}]]
   #_[:github-ref-name [any? {:description "The branch or tag name that triggered the workflow run."
                              :env-var "GITHUB_REF_NAME"}]]
   #_[:github-ref-protected [boolean? {:description "true if branch protections are configured for the ref that triggered the workflow run."
                                       :env-var "GITHUB_REF_PROTECTED"
                                       :arg-number 0}]]
   #_[:github-ref-type [:enum {:decode/string keyword
                               :description "The type of ref that triggered the workflow run. Valid values are branch or tag."
                               :env-var "GITHUB_REF_TYPE"}
                        :branch :tag]]
   [:github-action-path [:fn {:description "The path where your action is located. You can use this path to access files located in the same repository as your action. This variable is only supported in composite actions."
                              :env-var "GITHUB_ACTION_PATH"
                              :decode/string ->dir}
                         ->dir]]
   [:github-actor [any? {:description "The name of the person or app that initiated the workflow. For example, octocat."
                         :env-var "GITHUB_ACTOR"}]]
   [:github-repository [:string {:description "The owner and repository name. For example, octocat/Hello-World."
                                 :env-var "GITHUB_REPOSITORY"}]]
   [:github-workspace [:fn {:description "The GitHub workspace directory path, initially empty. For example, /home/runner/work/my-repo-name/my-repo-name. The actions/checkout action will check out files, by default a copy of your repository, within this directory."
                            :env-var "GITHUB_WORKSPACE"
                            :decode/string ->dir}
                       ->dir]]
   [:github-server-url [any? {:description "Returns the URL of the GitHub server. For example: https://github.com."
                              :env-var "GITHUB_SERVER_URL"}]]
   [:github-api-url [any? {:description "Returns the API URL. For example: https://api.github.com."
                           :env-var "GITHUB_API_URL"}]]
   [:walter-git-email [any? {:description "Returns the API URL. For example: https://api.github.com."
                             :env-var "WALTER_GIT_EMAIL"}]]
   [:walter-actor [any? {:description "Returns the API URL. For example: https://api.github.com."
                         :env-var "WALTER_ACTOR"}]]
   [:walter-github-password [any? {:description "Token as GITHUB_TOKEN, but you may give it more power like overriding workflow files."
                                   :env-var "WALTER_GITHUB_PASSWORD"}]]
   [:walter-clojars-password [string? {:description "Token as GITHUB_TOKEN, but you may give it more power like overriding workflow files."
                                       :env-var "WALTER_CLOJARS_PASSWORD"}]]
   [:walter-clojars-username [string? {:description "Token as GITHUB_TOKEN, but you may give it more power like overriding workflow files."
                                       :env-var "WALTER_CLOJARS_USERNAME"}]]
   [:walter-author-name [any? {:description "Different from the GIT_COMMITTER_NAME who made the commit. Here is the GIT_AUTHOR_NAME of the changes."
                               :env-var "WALTER_AUTHOR_NAME"
                               :default "Walter CI"}]]
   [:walter-version {:optional true} [string? {:env-var "WALTER_VERSION"}]]
   ;; Non-GitHub config keys
   [:show-config? {:optional true} [boolean? {:description "Print actual configuration value and exit."
                                              :optional true
                                              :arg-number 0}]]
   [:help {:optional true} [boolean? {:description "Display usage summary and exit."
                                      :short-option "-h"
                                      :optional true
                                      :arg-number 0}]]
   [:now {:optional true} [:fn {:decode/string #(ZonedDateTime/parse %)
                                :description "Token as GITHUB_TOKEN, but you may give it more power like overriding workflow files."
                                :env-var "NOW"
                                :default-fn #(ZonedDateTime/now)}
                           #(instance? ZonedDateTime %)]]])

(def command-schemas
  {:forward-secret [:map {:decode/args-transformer malli-cli/args-transformer}
                    [:secret-names [:sequential {:long-option "--secret-name"
                                                 :update-fn (fn [options _ [secret-name]]
                                                              (update options :secret-names (fnil conj []) secret-name))}
                                    string?]]]
   :retry [:map {:decode/args-transformer malli-cli/args-transformer}
           [:walter-try [string? {:env-var "WALTER_TRY"}]]
           [:walter-before-retry [string? {:env-var "WALTER_BEFORE_RETRY"}]]]

   :install-workflow [:map {:decode/args-transformer malli-cli/args-transformer}
                      [:source+target-pairs [:sequential [:map
                                                          [:source-edn :string]
                                                          [:target-yml :string]]]]
                      [:source-edn [string? {:update-fn (fn [options _ [source-edn]]
                                                          (update options :source+target-pairs (fnil conj []) {:source-edn source-edn}))}]]
                      [:target-yml [string? {:update-fn (fn [options _ [target-workflow]]
                                                          (update options :source+target-pairs (fn update-last [pairs]
                                                                                                 (update pairs (dec (count pairs))
                                                                                                   assoc :target-yml target-workflow))))}]]]})

(def Command
  [:and keyword? [:enum
                  :conform-repository
                  :forward-secret
                  :install-workflow
                  :retry
                  :update-git-ignore]])

;; FIXME Validation is broken.
(defn load-config
  [[command & rest-args]]
  (let [command (m/decode Command command (mt/string-transformer))]
    (merge
      {:command command}
      (m/decode EnvironmentVariables rest-args (mt/transformer malli-cli/cli-transformer))
      (when-let [schema (get command-schemas command)]
        (m/decode schema rest-args (mt/transformer malli-cli/cli-transformer))))))

(comment
  (load-config (str/split "retry --walter-try \"git-push\" --walter-before-retry \"git-pull---rebase\"" #"\s"))
  (load-config (str/split "self-deploy --github-repository piotr-yuxuan/slava-record" #"\s"))
  (load-config (println ["forward-secret" "--github-repository" "piotr-yuxuan/slava-record" "--secret-name" "a" "--secret-name" "b"]))
  (load-config ["conform-repository" "--github-repository" "piotr-yuxuan/slava-record"])
  (load-config ["update-git-ignore" "--github-repository" "piotr-yuxuan/slava-record"])
  (load-config ["install-workflow"
                "--source-edn" "\"$HOME1/.walter-ci/edn-sources/walter-perf.edn\"" "--target-yml" "1walter-perf.yml"
                "--source-edn" "\"$HOME2/.walter-ci/edn-sources/walter-perf.edn\"" "--target-yml" "2walter-perf.yml"
                "--source-edn" "\"$HOME3/.walter-ci/edn-sources/walter-perf.edn\"" "--target-yml" "3walter-perf.yml"
                "--github-repository" "%s"]))
