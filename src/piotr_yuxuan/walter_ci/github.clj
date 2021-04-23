(ns piotr-yuxuan.walter-ci.github
  "GitHub repository"
  (:require [leiningen.core.project :as leiningen]
            [clojure.java.io :as io]
            [clojure.data]
            [clojure.set]
            [clj-http.client :as http]
            [jsonista.core :as json]
            [clojure.string :as str]
            [malli.core :as m]
            [malli.transform :as mt]
            [malli.util :as mu]))

(def Defaults
  (m/schema
    [:map {:description "It is about GitHub repository, not the way Walter behaves."}
     [:github/description [string? {:request-key "description" :default nil :description "Description of the GitHub repository. Fallback on :description from the project definition."}]]
     [:github/homepage [string? {:request-key "homepage" :default nil :description "URL of the webpage for this project. Fallback on :url then on [:scm :url] from the project definition."}]]
     [:github/topics [:vector {:request-key "topics" :default [] :description "An array of topics to add to the repository."} string?]]
     [:github/private? [boolean? {:request-key "private" :default false :description "Either `true` to make the repository private or `false` to make it public."}]]
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

(defn map-val-schemas
  [schema]
  (map (comp first
             m/-children
             second)
       (m/-entries schema)))

(def request-key-transformer
  (-> {}
      (assoc-in [:encoders :map :compile]
        (fn [schema _]
          (partial medley.core/map-keys
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

(defn expected-settings
  [{:keys [project-root]}]
  (let [project (leiningen/read (.getAbsolutePath (io/file project-root "project.clj")) [:github])]
    (m/encode Defaults (-> project
                           (assoc :github/homepage (or (:github/homepage project) (:url project) (:scm (:url project))))
                           (assoc :github/description (or (:github/description project) (:description project))))
              (mt/transformer
                mt/default-value-transformer
                mt/strip-extra-keys-transformer))))

(defn apply-settings
  [{:keys [owner repo]} {:github/keys [topics] :as settings}]
  ;; Setting topics through the API is still in preview.
  (http/request {:request-method :put
                 :url (str/join "/" ["https://api.github.com/repos" owner repo "topics"])
                 :body (json/write-value-as-string {:names topics})
                 :basic-auth ["piotr-yuxuan" (System/getenv "GH_PACKAGES_PSW")]
                 :headers {"Content-Type" "application/json"
                           "Accept" "application/vnd.github.mercy-preview+json"}})
  (let [json-settings (m/encode Defaults (dissoc settings :github/topics)
                                (mt/transformer
                                  mt/strip-extra-keys-transformer
                                  request-key-transformer))]
    (http/request {:request-method :patch
                   :url (str/join "/" ["https://api.github.com/repos" owner repo])
                   :body (json/write-value-as-string json-settings)
                   :basic-auth ["piotr-yuxuan" (System/getenv "GH_PACKAGES_PSW")]
                   :headers {"Content-Type" "application/json"
                             "Accept" "application/vnd.github.v3+json"}}))
  :ok)

(comment
  (let [project {:owner "piotr-yuxuan"
                 :repo "walter-ci"
                 :project-root "/Users/p2b/src/github.com/piotr-yuxuan/walter-ci"}]
    (->> (expected-settings project)
         (apply-settings project))))
