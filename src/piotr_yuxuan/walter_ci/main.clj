(ns piotr-yuxuan.walter-ci.main
  "Hint: caesium appears to run on Java SDK 14, but not 17."
  (:require [camel-snake-kebab.core :as csk]
            [leiningen.change]
            [caesium.crypto.box]
            [clojure.data]
            [clojure.string :as str]
            [clj-http.client :as http]
            [jsonista.core :as json]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:gen-class)
  (:import (java.util Base64)
           (java.nio.charset StandardCharsets)))

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
     :decoded-key (.decode (Base64/getDecoder) (.getBytes encoded-key StandardCharsets/UTF_8))
     :key-id (get payload "key_id")}))

(defn sealed-public-key-box
  [{:keys [^String decoded-key key-id]} ^String secret-value]
  {:encrypted_value (->> (byte-streams/to-byte-array secret-value)
                         (caesium.crypto.box/anonymous-encrypt decoded-key)
                         (.encodeToString (Base64/getEncoder)))
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

(defn -main
  [& _]
  (let [github-repository (System/getenv "GITHUB_REPOSITORY")
        config {:github-repository github-repository
                :github-api-url (System/getenv "GITHUB_API_URL")
                :github-actor (System/getenv "GITHUB_ACTOR")
                :walter-github-password (System/getenv "WALTER_GITHUB_PASSWORD")}]
    (doseq [github-repository (:github-repositories (edn/read-string (slurp (io/resource "state.edn"))))
            {:keys [secret-name secret-value]} [{:secret-name "MY_SECRET" :secret-value "Value set by an action."}]]
      (println :github-repository github-repository
               :secret-name secret-name)
      (let [public-key (public-key config github-repository)]
        (->> (sealed-public-key-box public-key secret-value)
             (upsert-secret-value config github-repository secret-name))))))

;;; Trigger documentation build:
;;; curl --verbose 'https://cljdoc.org/api/request-build2' \
;;;   --header 'Content-Type: application/x-www-form-urlencoded' \
;;;   --header 'Origin: https://cljdoc.org' \
;;;   --data-raw 'project=com.github.piotr-yuxuan%2Fmalli-cli&version=0.0.6'
