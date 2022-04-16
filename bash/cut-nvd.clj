#!/usr/bin/env bb

(def csv-file-path (first *command-line-args*))

(let [rows (slurp csv-file-path)]
  (with-open [writer (io/writer csv-file-path)]
    ;; First column always contains stdin, second column is the scan
    ;; timestamp and makes it not reproducible.
    (csv/write-csv writer (map #(drop 2 %) (csv/read-csv rows)))))

:ok
