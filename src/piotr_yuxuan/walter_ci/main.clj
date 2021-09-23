(ns piotr-yuxuan.walter-ci.main
  "Hint: caesium appears to run on Java SDK 14, but not 17."
  (:require [camel-snake-kebab.core :as csk]
            [leiningen.change]
            [caesium.crypto.box]
            [clojure.data]
            [clojure.string :as str]
            [clj-http.client :as http]
            [jsonista.core :as json]
            [medley.core :as medley]
            [caesium.crypto.secretbox :as crypto])
  (:gen-class)
  (:import (java.util Base64)
           (java.nio.charset StandardCharsets)))

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
  (println :target-repository target-repository)
  (println :secret-name secret-name)
  (println :sealed-public-key-box (pr-str sealed-public-key-box))
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
          secret-value "MY_SECRET_VALUE"]
      (println "Current secret value" (avoid-secret-redaction (System/getenv secret-name)))
      (println "Secret value" (avoid-secret-redaction secret-value))
      (println
        "Upsert response"
        (dissoc
          (->> secret-value
               (sealed-public-key-box public-key)
               (upsert-secret-value config target-repository secret-name))
          :headers
          :http-client)))))

;;; Trigger documentation build:
;;; curl --verbose 'https://cljdoc.org/api/request-build2' \
;;;   --header 'Content-Type: application/x-www-form-urlencoded' \
;;;   --header 'Origin: https://cljdoc.org' \
;;;   --data-raw 'project=com.github.piotr-yuxuan%2Fmalli-cli&version=0.0.6'
