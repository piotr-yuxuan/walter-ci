(ns piotr-yuxuan.walter-ci.core
  (:require [clojure.java.io :as io]
            [piotr-yuxuan.utils :refer [deep-merge]]
            [piotr-yuxuan.walter-ci.files :refer [->file ->tmp-dir ->tmp-file with-delete! delete!]]
            [piotr-yuxuan.walter-ci.git :as git-workspace]
            [piotr-yuxuan.walter-ci.secrets :as secret])
  (:import (java.io File)
           (java.time ZonedDateTime)))

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
  (println ::replicate)
  (doseq [github-repository managed-repositories]
    (doto (assoc config :github-repository github-repository)
      (secret/upsert-value "MY_SECRET" (format "Secret value generated at %s." (ZonedDateTime/now)))
      (update-workflow (->file github-action-path "resources" "workflows" "walter-ci.yml")))))

(defn start
  [{:keys [input-command] :as config}]
  (cond (= :replicate input-command) (replicate-workflow config)))
