(ns piotr-yuxuan.walter-ci.main
  "Hint: caesium appears to run on Java SDK 14, but not 17."
  (:require [camel-snake-kebab.core :as csk]
            [leiningen.change]
            [clojure.data]
            [clojure.string :as str]
            [clj-http.client :as http]
            [jsonista.core :as json]
            [medley.core :as medley]
            [caesium.crypto.secretbox :as crypto])
  (:gen-class)
  (:import (java.util Base64)))

(defn load-config
  []
  (->> (System/getenv)
       (into {})
       (medley/map-keys csk/->kebab-case-keyword)))

(defn public-key
  [{:keys [github-api-url github-actor walter-github-password]} github-repository]
  (let [payload (->> {:request-method :get
                      :url (str/join "/" [github-api-url "repos" github-repository "actions/secrets/public-key"])
                      :basic-auth [github-actor walter-github-password]
                      :headers {"Content-Type" "application/json"
                                "Accept" "application/vnd.github.v3+json"}}
                     http/request
                     :body
                     json/read-value)
        ^String encoded-key (get payload "key")]
    {:encoded-key encoded-key
     :decoded-key (.decode (Base64/getDecoder) encoded-key)
     :key-id (get payload "key_id")}))

(defn sealed-public-key-box
  [{:keys [decoded-key key-id]} ^String secret-value]
  {:encrypted_value (->> (.getBytes secret-value)
                         ^"[B" (crypto/encrypt decoded-key (crypto/int->nonce 0))
                         (.encode (Base64/getEncoder))
                         slurp)
   :key_id key-id})

(defn upsert-secret-value
  [{:keys [github-api-url github-actor walter-github-password]} target-repository secret-name sealed-public-key-box]
  (try
    (http/request
      {:request-method :put
       :url (str/join "/" [github-api-url "repos" target-repository "actions" "secrets" (csk/->SCREAMING_SNAKE_CASE_STRING secret-name)])
       :body (json/write-value-as-string sealed-public-key-box)
       :basic-auth [github-actor walter-github-password]
       :headers {"Content-Type" "application/json"
                 "Accept" "application/vnd.github.v3+json"}})
    (catch Exception ex
      (println (pr-str (ex-data ex)))
      (throw ex))))

(defn avoid-secret-redaction
  "You must not use it. Display a string as a collection of characters."
  [^String secret-value]
  (map identity secret-value))

(defn -main
  [& _]
  (let [config (load-config)]
    (println :config config)
    (let [target-repository "piotr-yuxuan/walter-ci"
          public-key (public-key config target-repository)
          secret-name "MY_SECRET"
          secret-value "MY_OTHER_VALUE"]
      (println
        (->> (clojure.data/diff secret-value
                                (System/getenv secret-name))
             (map avoid-secret-redaction)
             pr-str))
      (->> secret-value
           (sealed-public-key-box public-key)
           (upsert-secret-value config target-repository secret-name)))
    (println :all-done)))

;;; Trigger documentation build:
;;; curl --verbose 'https://cljdoc.org/api/request-build2' \
;;;   --header 'Content-Type: application/x-www-form-urlencoded' \
;;;   --header 'Origin: https://cljdoc.org' \
;;;   --data-raw 'project=com.github.piotr-yuxuan%2Fmalli-cli&version=0.0.6'
