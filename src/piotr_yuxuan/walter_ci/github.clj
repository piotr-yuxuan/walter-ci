(ns piotr-yuxuan.walter-ci.github
  "GitHub repository"
  (:require [piotr-yuxuan.walter-ci.files :refer [->file]]
            [piotr-yuxuan.walter-ci.git :as git]
            [clj-http.client :as http]
            [clojure.data]
            [clojure.java.io :as io]
            [clojure.set]
            [clojure.string :as str]
            [jsonista.core :as json]
            [leiningen.core.project :as leiningen]
            [malli.core :as m]
            [malli.transform :as mt]
            [malli.util :as mu]
            [medley.core :as medley]
            [safely.core :refer [safely]])
  (:import (clojure.lang DynamicClassLoader)))

(def Defaults
  (m/schema
    [:map {:description "It is about GitHub repository, not the way Walter behaves."}
     [:github/description [string? {:request-key "description" :default nil :description "Description of the GitHub repository. Fallback on :description from the project definition."}]]
     [:github/homepage [string? {:request-key "homepage" :default nil :description "URL of the webpage for this project. Fallback on :url then on [:scm :url] from the project definition."}]]
     [:github/topics [:vector {:request-key "topics" :default [] :description "An array of topics to add to the repository."} string?]]
     ;; This is so dangerous when you forget that unless specified otherwise it will close any new repository and make it private.
     ;; It is the decision of the repo owner, as described in `project.clj`, to make this repo public.
     [:github/private? [boolean? {:request-key "private" :default true :description "Either `true` (default) to make the repository private or `false` to make it public."}]]
     [:github/issues? [boolean? {:request-key "has_issues" :default true :description "Enable GitHub issues for this repository."}]]
     [:github/projects? [boolean? {:request-key "has_projects" :default false :description "Enable GitHub projects for this repository."}]]
     [:github/wiki? [boolean? {:request-key "has_wiki" :default false :description "Enable GitHub wiki for this repository."}]]
     [:github/template? [boolean? {:request-key "is_template" :default false :description "Make this repo available as a template repository"}]]
     [:github/default-branch [string? {:request-key "default_branch" :default "main" :description "Name of the default git branch."}]]
     [:github/allow-squash-merge? [boolean? {:request-key "allow_squash_merge" :default false :description "Allow squash-merging pull requests on GitHub."}]]
     [:github/allow-merge-commit? [boolean? {:request-key "allow_merge_commit" :default false :description "Allow merging pull requests with a merge commit."}]]
     [:github/allow-rebase-merge? [boolean? {:request-key "allow_rebase_merge" :default true :description "Allow rebase-merging pull requests."}]]
     [:github/delete-branch-on-merge? [boolean? {:request-key "delete_branch_on_merge" :default true :description "Allow automatically deleting head branches when pull requests are merged."}]]
     [:github/archived? [boolean? {:request-key "archived" :default false :description "Mark the GitHub repository as archived. Note: You cannot unarchive repositories through the API."}]]]))

(def request-key-transformer
  (-> {}
      (assoc-in [:encoders :map :compile]
        (fn [schema _]
          (partial medley/map-keys
                   (comp :request-key
                         m/properties
                         (partial mu/get schema)))))
      (assoc-in [:decoders :map :compile]
        (fn [schema _]
          (let [request-keys (reduce (fn [acc [schema-key val-schema]]
                                       (assoc acc
                                         (->> val-schema
                                              m/-children
                                              first
                                              m/properties
                                              :request-key)
                                         schema-key))
                                     {}
                                     (m/-entries schema))]
            (fn [m]
              (dissoc
                (medley.core/map-keys (fn [k] (get request-keys k ::not-found)) m)
                ::not-found)))))
      mt/transformer))

(defn ensure-dynamic-classloader!
  ;; https://github.com/riemann/riemann/pull/892/files#diff-691f7c44e67c3e00a8d2eda28e4b4422535c693efaf6779ce29aa537fef26ceeR109-R115
  [^Thread current-thread]
  (let [cl (.getContextClassLoader current-thread)]
    (when-not (instance? DynamicClassLoader cl)
      (.setContextClassLoader current-thread (DynamicClassLoader. cl)))))

(defn expected-settings
  [{:keys [github-workspace]}]
  (ensure-dynamic-classloader! (Thread/currentThread))
  (let [project (leiningen/read (.getAbsolutePath (io/file github-workspace "project.clj")) [:github])]
    (m/encode Defaults (-> project
                           (assoc :github/homepage (or (:github/homepage project) (:url project) (:scm (:url project))))
                           (assoc :github/description (or (:github/description project) (:description project))))
              (mt/transformer
                mt/default-value-transformer
                mt/strip-extra-keys-transformer))))

(defn apply-settings
  [{:keys [github-api-url github-actor github-repository walter-github-password]} {:github/keys [topics] :as settings}]
  ;; Setting topics through the API is still in preview.
  (safely
    (http/request {:request-method :put
                   :url (str/join "/" [github-api-url "repos" github-repository "topics"])
                   :body (json/write-value-as-string {:names topics})
                   :basic-auth [github-actor walter-github-password]
                   :headers {"Content-Type" "application/json"
                             "Accept" "application/vnd.github.mercy-preview+json"}})
    :on-error
    :max-retries 5)
  (let [json-settings (m/encode Defaults (dissoc settings :github/topics)
                                (mt/transformer
                                  mt/strip-extra-keys-transformer
                                  request-key-transformer))]
    (safely
      (http/request {:request-method :patch
                     :url (str/join "/" [github-api-url "repos" github-repository])
                     :body (json/write-value-as-string json-settings)
                     :basic-auth [github-actor walter-github-password]
                     :headers {"Content-Type" "application/json"
                               "Accept" "application/vnd.github.v3+json"}})
      :on-error
      :max-retries 5)))

(defn conform-repository
  [{:keys [github-workspace] :as config}]
  (doseq [github-file ["FUNDING.yml"
                       "CODEOWNERS.yml"]]
    (spit (doto (->file github-workspace ".github" github-file) io/make-parents)
          (slurp (io/resource github-file)))
    (git/stage-all github-workspace config)
    (when (git/need-commit? github-workspace config)
      (git/commit github-workspace config (format "Update %s" github-file))))
  (git/push github-workspace config)
  (apply-settings config (expected-settings config)))
