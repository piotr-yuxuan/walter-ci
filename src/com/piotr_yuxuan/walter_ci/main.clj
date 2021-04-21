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
  [{:keys [exit err] :as ret}]
  (when-not (zero? exit)
    (throw (ex-info (str (first (str/split-lines err)))
                    ret)))
  (pprint/pprint ret))

(defn -main
  [& args]
  (println "Environment")
  (pprint/pprint (System/getenv))

  (println "Identify user")
  (pprint-or-sh-exit
    (shell/sh
      "who"
      :env {"HOME" "/home/walter-ci"}))

  (println "Access rights")
  (pprint-or-sh-exit
    (shell/sh
      "ls" "-hal" (str (System/getenv "GITHUB_WORKSPACE"))
      :dir (System/getenv "GITHUB_WORKSPACE")
      :env {"HOME" "/home/walter-ci"}))

  ;(println "Retrieve dependencies")
  ;(pprint-or-sh-exit
  ;  (shell/sh
  ;    "lein" "deps"
  ;    :dir "/home/walter-ci"
  ;    :env {"HOME" "/home/walter-ci"}))
  ;
  ;(println ::test)
  ;(println
  ;  (pr-str
  ;    (shell/sh
  ;      "lein" "test"
  ;      :dir "/home/walter-ci"
  ;      :env {"HOME" "/home/walter-ci"})))
  ;
  ;(println ::uberjar)
  ;(println
  ;  (pr-str
  ;    (shell/sh
  ;      "lein" "uberjar"
  ;      :dir "/home/walter-ci"
  ;      :env {"HOME" "/home/walter-ci"})))
  ;
  ;(println ::deploy :clojars)
  ;(println
  ;  (pr-str
  ;    (shell/sh
  ;      "lein" "deploy" "clojars"
  ;      :dir (System/getenv "GITHUB_WORKSPACE")
  ;      :env {"HOME" "/home/walter-ci"
  ;            "WALTER_CLOJARS_USERNAME" (System/getenv "WALTER_CLOJARS_USERNAME")
  ;            "WALTER_CLOJARS_PASSWORD" (System/getenv "WALTER_CLOJARS_PASSWORD")})))
  )
