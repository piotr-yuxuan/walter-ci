(ns piotr-yuxuan.walter-ci.lein-test
  (:require [babashka.process :as process]
            [clojure.java.io :as io]))

(defn step
  [{{:keys [github-workspace]} :env}]
  (let [{:keys [exit]} @(process/process "lein test"
                                         {:out :inherit
                                          :dir (io/file github-workspace)})]
    (assert (zero? exit) "Tests failed")))
