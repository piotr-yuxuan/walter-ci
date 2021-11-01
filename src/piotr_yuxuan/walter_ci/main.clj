(ns piotr-yuxuan.walter-ci.main
  (:import (java.time LocalDateTime))
  (:gen-class))

(defn -main
  [& _]
  (println "Hello world" (LocalDateTime/now)))
