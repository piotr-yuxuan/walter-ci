(ns com.piotr-yuxuan.walter-ci.main
  (:gen-class))

(defn -main
  [& args]
  (println "I am bootstrapped :-)")
  (shell/sh "lein" "deps" (System/getenv "GITHUB_WORKSPACE"))
  (shell/sh "lein" "test" (System/getenv "GITHUB_WORKSPACE"))
  (shell/sh "lein" "uberjar" (System/getenv "GITHUB_WORKSPACE")))
