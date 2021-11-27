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
    (io/copy workflow-file (doto (->file working-directory ".github" "workflows" (.getName workflow-file))
                             (io/make-parents)))
    (git-workspace/stage-all working-directory options)
    (git-workspace/commit working-directory options (format "Copy workflow %s" (.getName workflow-file)))
    (git-workspace/push working-directory options)))

(defn start
  [{:keys [input-command github-action-path managed-repositories] :as config}]
  (cond (= :copy-workflows input-command)
        (doseq [github-repository managed-repositories]
          (copy-workflow (assoc config :github-repository github-repository)
                         (->file github-action-path "resources" "workflows" "dummy-workflow.yml")))))
