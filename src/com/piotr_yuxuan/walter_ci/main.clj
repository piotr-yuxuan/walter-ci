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
  (println ::deps) (shell/sh "lein" "deps" (System/getenv "GITHUB_WORKSPACE"))
  (println ::test) (shell/sh "lein" "test" (System/getenv "GITHUB_WORKSPACE"))
  (println ::uberjar) (shell/sh "lein" "uberjar" (System/getenv "GITHUB_WORKSPACE"))
  (println :deploy/clojars) (shell/sh "lein" "deploy" "clojars" (System/getenv "GITHUB_WORKSPACE")))
