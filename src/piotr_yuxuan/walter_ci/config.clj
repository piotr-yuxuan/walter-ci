(ns piotr-yuxuan.walter-ci.config
  (:require [piotr-yuxuan.malli-cli :as malli-cli]
            [piotr-yuxuan.malli-cli.utils :refer [deep-merge]]
            [piotr-yuxuan.walter-ci.files :refer [->dir]]
            [malli.core :as m]
            [malli.transform :as mt]
            [clojure.string :as str]
            [malli.util :as mu]
            [malli.error :as me])
  (:import (java.time ZonedDateTime)))

(def config-env-vars
  [:map
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
   [:walter-github-password [any? {:description "Token as GITHUB_TOKEN, but you may give it more power like overriding workflow files."
                                   :env-var "WALTER_GITHUB_PASSWORD"}]]
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
                           #(instance? ZonedDateTime %)]]
   [:walter-author-name [any? {:description "Different from the GIT_COMMITTER_NAME who made the commit. Here is the GIT_AUTHOR_NAME of the changes."
                               :env-var "WALTER_AUTHOR_NAME"
                               :default "Walter CI"}]]
   [:walter-try [string? {:env-var "WALTER_TRY"}]]
   [:walter-before-retry [string? {:env-var "WALTER_BEFORE_RETRY"}]]])

(def Config
  "These options include all the possible environment variables from GitHub ([link](https://docs.github.com/en/actions/learn-github-actions/environment-variables)). The ones we don't need now are commented out."
  (m/schema
    (-> [:map {:closed true, :decode/args-transformer malli-cli/args-transformer}]
        (mu/merge config-env-vars)
        (mu/merge [:schema {:registry {::command [:and keyword? [:enum
                                                                 :conform-repository
                                                                 :retry
                                                                 :self-deploy
                                                                 :update-git-ignore]]
                                       ::sub-command [:string]}}
                   [:and
                    [:map [:command ::command]]
                    [:multi {:dispatch :command}
                     [:retry [:map
                              [:walter-try ::sub-command]
                              [:walter-before-retry ::sub-command]]]
                     [:self-deploy [:map [:github-repository string?]]]
                     [::m/default :any]]]]))))

;; FIXME Validation is broken.
(defn load-config
  [args & [repl-overrides]]
  (let [{::malli-cli/keys [operands after-option-operands] :as decoded} (m/decode Config args (mt/transformer malli-cli/args-transformer))]
    (println :decoded decoded)
    (as-> decoded $
      (assoc $
        :command (first operands)
        :sub-command (str/join \space after-option-operands))
      (m/decode
        Config
        $
        (mt/transformer
          (mt/string-transformer)
          (mt/default-value-transformer {:key :env-var
                                         :default-fn #(get (malli-cli/*system-get-env*) %2)})
          (mt/default-value-transformer {:key :default})
          ;; Doesn't work on multi schema.
          ;(mt/strip-extra-keys-transformer)
          ))
      (deep-merge $ repl-overrides))))

(comment
  (load-config (str/split "retry -- git -c http.https://github.com/.extraheader= push" #"\s"))
  (load-config (str/split "self-deploy --github-repository piotr-yuxuan/slava-record" #"\s")))
