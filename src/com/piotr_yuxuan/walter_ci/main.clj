(ns com.piotr-yuxuan.walter-ci.main
  "All the environment variable in the [Action
  reference](https://docs.github.com/en/actions/reference/environment-variables)
  may be used. In addition, the following environment variables may be
  used:

  - WALTER_CLOJARS_USERNAME
  - WALTER_CLOJARS_PASSWORD
  - WALTER_GITHUB_USERNAME
  - WALTER_GITHUB_PASSWORD"
  (:require [clojure.java.shell :as shell])
  (:gen-class))

(defn -main
  [& args]
  (println "I am the first command run completely from a bootstrapped Walter :-)")

  (println ::deps)
  (println
    (pr-str
      (shell/sh "lein" "deps" :dir (System/getenv "GITHUB_WORKSPACE"))))

  (println ::test)
  (println
    (pr-str
      (shell/sh "lein" "test" :dir (System/getenv "GITHUB_WORKSPACE"))))

  (println ::uberjar)
  (println
    (pr-str
      (shell/sh "lein" "uberjar" :dir (System/getenv "GITHUB_WORKSPACE"))))

  (println ::deploy :clojars)
  (println
    (pr-str
      (shell/sh "lein" "deploy" "clojars"
                :dir (System/getenv "GITHUB_WORKSPACE")
                :env {:WALTER_CLOJARS_USERNAME (System/getenv "WALTER_CLOJARS_USERNAME")
                      :WALTER_CLOJARS_PASSWORD (System/getenv "WALTER_CLOJARS_PASSWORD")}))))
