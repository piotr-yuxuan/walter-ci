(ns com.piotr-yuxuan.walter-ci.main
  "All the environment variable in the [Action
  reference](https://docs.github.com/en/actions/reference/environment-variables)
  may be used. In addition, the following environment variables may be
  used:

  - WALTER_CLOJARS_USERNAME
  - WALTER_CLOJARS_PASSWORD
  - WALTER_GITHUB_USERNAME
  - WALTER_GITHUB_PASSWORD"
  (:require [clojure.java.shell :as shell]
            [clojure.pprint :as pprint]
            [clojure.string :as str])
  (:gen-class))

(defn pprint-or-sh-exit
  [{:keys [exit out err] :as ret}]
  (when-not (zero? exit)
    (throw (ex-info (str (first (str/split-lines err)))
                    ret)))
  (pprint/pprint out))

(println "I'm in the code, not the jar trololo.")

(defn -main
  [& args]
  (println "Environment")
  (pprint/pprint (System/getenv))

  (println "Implicit environment")
  (pprint-or-sh-exit
    (shell/sh
      "env"))

  (println "Explicit environment")
  (pprint-or-sh-exit
    (shell/sh
      "env"
      :env (into {} (System/getenv))))

  (println "Controlled environment")
  (pprint-or-sh-exit
    (shell/sh
      "env"
      :env {"HOME" "/home/walter-ci"}))

  (println "Identify user")
  (pprint-or-sh-exit
    (shell/sh
      "id"))

  (println "Access rights")
  (pprint-or-sh-exit
    (shell/sh
      "ls" "-hal"
      :env {"HOME" "/home/walter-ci"}))

  (println "Retrieve dependencies")
  (pprint-or-sh-exit
    (shell/sh
      "lein" "deps"
      :dir "/home/walter-ci/workspace"
      :env {"HOME" "/home/walter-ci"}))

  (println ::test)
  (pprint-or-sh-exit
    (shell/sh
      "lein" "test"))

  (println ::uberjar)
  (pprint-or-sh-exit
    (shell/sh
      "lein" "uberjar"))

  (println ::deploy :clojars)
  (pprint-or-sh-exit
    (shell/sh
      "lein" "deploy" "clojars"
      :env (merge (into {} (System/getenv))
                  {"DEBUG" "true"
                   "WALTER_CLOJARS_USERNAME" (System/getenv "WALTER_CLOJARS_USERNAME")
                   "WALTER_CLOJARS_PASSWORD" (System/getenv "WALTER_CLOJARS_PASSWORD")}))))
