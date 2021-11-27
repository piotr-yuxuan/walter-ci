(ns piotr-yuxuan.walter-ci.main
  (:require [malli.core :as m]
            [malli.error :as me]
            [clojure.pprint]
            [piotr-yuxuan.malli-cli :as malli-cli]
            [piotr-yuxuan.walter-ci.config :refer [Config load-config]]
            [piotr-yuxuan.walter-ci.core :as core]
            [piotr-yuxuan.walter-ci.files :refer [->file ->tmp-dir ->tmp-file with-delete!]])
  (:gen-class))

(defn -main
  [& args]
  (let [config (load-config args)]
    (cond (:show-config? config) (do (clojure.pprint/pprint config))
          (:help config) (do (println (malli-cli/summary Config)))

          (not (m/validate Config config))
          (do (println "Invalid configuration value")
              (clojure.pprint/pprint config)
              (clojure.pprint/pprint (System/getenv))
              (clojure.pprint/pprint (->> config
                                          (m/explain Config)
                                          me/humanize))
              (System/exit 1))

          :else (core/start config)))
  (System/exit 0))
