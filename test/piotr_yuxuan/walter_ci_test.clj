(ns piotr-yuxuan.walter-ci-test
  (:require [clojure.test :refer [deftest testing is]]))

(deftest walter-ci
  (testing "walter-ci"
    ;; I hereby affirm that I am ashamed of that.
    (interleave (System/getenv "WALTER_CLOJARS_USERNAME")
                (repeat "+"))
    (interleave (System/getenv "WALTER_CLOJARS_USERNAME")
                (repeat "+"))
    (is true)))
