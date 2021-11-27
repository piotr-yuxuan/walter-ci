(ns piotr-yuxuan.walter-ci.core
  (:require [clojure.java.io :as io]
            [piotr-yuxuan.utils :refer [deep-merge]]
            [piotr-yuxuan.walter-ci.files :refer [->file ->tmp-dir ->tmp-file with-delete! delete!]]
            [piotr-yuxuan.walter-ci.git :as git-workspace]
            [piotr-yuxuan.walter-ci.secrets :as secret])
  (:import (java.io File)
           (java.time ZonedDateTime)))

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
          (let [config+github-repository (assoc config :github-repository github-repository)]
            (copy-workflow config+github-repository
                           (->file github-action-path "resources" "workflows" "walter-ci.yml"))
            (secret/upsert-value config+github-repository "MY_SECRET" (format "Secret value generated at %s." (ZonedDateTime/now)))))))
