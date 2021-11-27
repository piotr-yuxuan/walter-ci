(ns piotr-yuxuan.walter-ci.core
  (:require [clojure.java.io :as io]
            [piotr-yuxuan.utils :refer [deep-merge]]
            [piotr-yuxuan.walter-ci.files :refer [->file ->tmp-dir ->tmp-file with-delete!]]
            [piotr-yuxuan.walter-ci.git :as git-workspace])
  (:import (java.io File)))

(defn copy-workflow
  [options ^File workflow-file]
  (with-delete! [working-directory (->tmp-dir "copy-workflow")]
    (git-workspace/clone working-directory options)
    (io/copy workflow-file (->file working-directory ".github" "workflows" (.getName workflow-file)))
    (git-workspace/stage-all working-directory options)
    (git-workspace/commit working-directory options (format "Copy workflow %s" (.getName workflow-file)))
    (git-workspace/push working-directory options)))

(defn start
  [{:keys [input-command github-action-path managed-repositories] :as config}]
  (cond (= :copy-workflows input-command)
        (doseq [github-repository managed-repositories]

          (println ::start github-repository)
          (println ::workflow-file (.exists (->file github-action-path)) (str (->file github-action-path)))
          (println ::workflow-file (.exists (->file github-action-path "resources")) (str (->file github-action-path "resources")))
          (println ::workflow-file (.exists (->file github-action-path "resources" "workflows")) (str (->file github-action-path "resources" "workflows")))
          (println ::workflow-file (.exists (->file github-action-path "resources" "workflows" "dummy-workflow.yml")) (str (->file github-action-path "resources" "workflows" "dummy-workflow.yml")))

          (println ::github-repository github-repository)
          (copy-workflow (assoc config :github-repository github-repository)
                         (->file github-action-path "resources" "workflows" "dummy-workflow.yml")))))
