(ns piotr-yuxuan.walter-ci.core
  (:require [clojure.java.io :as io]
            [camel-snake-kebab.core :as csk]
            [piotr-yuxuan.utils :refer [deep-merge]]
            [piotr-yuxuan.walter-ci.files :refer [->file ->tmp-dir ->tmp-file with-delete! delete!]]
            [piotr-yuxuan.walter-ci.git :as git-workspace]
            [piotr-yuxuan.walter-ci.secrets :as secret])
  (:import (java.io File)))

(defn update-workflow
  [options ^File workflow-file]
  (println ::update-workflow)
  (with-delete! [working-directory (->tmp-dir "update-workflow")]
    (git-workspace/clone working-directory options)
    (println :source (str workflow-file))
    (println :target (str (->file working-directory ".github" "workflows" (.getName workflow-file))))
    (io/copy workflow-file (doto (->file working-directory ".github" "workflows" (.getName workflow-file))
                             (io/make-parents)))
    (git-workspace/stage-all working-directory options)
    (when (git-workspace/need-commit? working-directory options)
      (git-workspace/commit working-directory options (format "Update %s" (.getName workflow-file))))
    (git-workspace/push working-directory options)))

(defn replicate-workflow
  [{:keys [github-action-path managed-repositories] :as config}]
  (doseq [github-repository managed-repositories]
    (println ::replicate github-repository)
    (let [config+github-repository (assoc config :github-repository github-repository)]
      (doseq [secret-name [:walter-author-name :walter-github-password :github-api-url]]
        (println :secret-name (csk/->SCREAMING_SNAKE_CASE_STRING secret-name))
        (secret/upsert-value config+github-repository
                             (csk/->SCREAMING_SNAKE_CASE_STRING secret-name)
                             (get config secret-name)))
      (update-workflow config+github-repository (->file github-action-path "resources" "workflows" "walter-ci.yml")))))

(defn start
  [{:keys [input-command] :as config}]
  (cond (= :replicate input-command) (replicate-workflow config)))
