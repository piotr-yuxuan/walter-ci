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

(defn -main
  [& args]
  (println "Environment")
  (pprint/pprint (System/getenv))

  (println "Implicit environment")
  (pprint-or-sh-exit
    (shell/sh
      "id"))

  (println "Explicit environment")
  (pprint-or-sh-exit
    (shell/sh
      "id"
      :env (System/getenv)))

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
      :env {"HOME" "/home/walter-ci"}))

  (println ::test)
  (println
    (pr-str
      (shell/sh
        "lein" "test"
        :env {"HOME" "/home/walter-ci"})))

  (println ::uberjar)
  (println
    (pr-str
      (shell/sh
        "lein" "uberjar"
        :env {"HOME" "/home/walter-ci"})))

  (println ::deploy :clojars)
  (println
    (pr-str
      (shell/sh
        "lein" "deploy" "clojars"
        :env (merge (System/getenv)
                    {"HOME" "/home/walter-ci"
                     "WALTER_CLOJARS_USERNAME" (System/getenv "WALTER_CLOJARS_USERNAME")
                     "WALTER_CLOJARS_PASSWORD" (System/getenv "WALTER_CLOJARS_PASSWORD")})))))
