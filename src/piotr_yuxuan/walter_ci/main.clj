(ns piotr-yuxuan.walter-ci.main
  "Hint: caesium appears to run on Java SDK 14, but not 17."
  (:require [camel-snake-kebab.core :as csk]
            [clj-yaml.core :as yaml]
            [leiningen.change]
            [caesium.crypto.box]
            [clojure.data]
            [clojure.string :as str]
            [clj-http.client :as http]
            [jsonista.core :as json]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import (java.util Base64))
  (:gen-class))

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
     :decoded-key (.decode (Base64/getDecoder) (.getBytes encoded-key))
     :key-id (get payload "key_id")}))

(defn sealed-public-key-box
  [{:keys [^String decoded-key key-id]} ^String secret-value]
  {:encrypted_value (->> (byte-streams/to-byte-array secret-value)
                         (caesium.crypto.box/anonymous-encrypt decoded-key)
                         (.encodeToString (Base64/getEncoder)))
   :key_id key-id})

(defn upsert-secret-value
  [{:keys [github-api-url github-actor walter-github-password]} target-repository secret-name sealed-public-key-box]
  (http/request
    {:request-method :put
     :url (str/join "/" [github-api-url "repos" target-repository "actions" "secrets" (csk/->SCREAMING_SNAKE_CASE_STRING secret-name)])
     :body (json/write-value-as-string sealed-public-key-box)
     :basic-auth [github-actor walter-github-password]
     :headers {"Content-Type" "application/json"
               "Accept" "application/vnd.github.v3+json"}}))

(defn spit-workflow-yaml
  "Did you know that the alpha2 country code of Norway is `false`, and
  `on` is `true`? I just refuse to touch a file format so corrupted by
  syntactic sugar. Read edn data in `input-file` and write yaml
  equivalent in `output-file`. Prepend a header."
  [form output-filename]
  (as-> form $
    (yaml/generate-string $ :dumper-options {:flow-style :block})
    (str (slurp (io/resource "header.yaml")) $)
    (spit output-filename $ :append false)))

(defn -main
  [& _]
  (println :ok))

;;; Trigger documentation build:
;;; curl --verbose 'https://cljdoc.org/api/request-build2' \
;;;   --header 'Content-Type: application/x-www-form-urlencoded' \
;;;   --header 'Origin: https://cljdoc.org' \
;;;   --data-raw 'project=com.github.piotr-yuxuan%2Fmalli-cli&version=0.0.6'
