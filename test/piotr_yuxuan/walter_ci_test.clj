(ns piotr-yuxuan.walter-ci-test
  (:require [clojure.test :refer [deftest testing is]]))

(deftest walter-ci
  (testing "walter-ci"
    ;; I hereby affirm that I am ashamed of that.
    (println
      :WALTER_GITHUB_USERNAME
      (interleave (System/getenv "WALTER_GITHUB_USERNAME")
                  (repeat "+")))
    (println
      :WALTER_CLOJARS_USERNAME
      (interleave (System/getenv "WALTER_CLOJARS_USERNAME")
                  (repeat "+")))
    (is true)))
