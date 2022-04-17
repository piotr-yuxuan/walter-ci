(ns piotr-yuxuan.walter-ci.secrets
  (:require [byte-streams :as byte-streams]
            [caesium.crypto.box :as crypto]
            [clj-http.client :as http]
            [clojure.string :as str]
            [jsonista.core :as json]
            [leiningen.change]
            [safely.core :refer [safely]])
  (:import (java.util Base64)))

(defn repository-public-key
  [{:keys [github-repository github-api-url walter-actor walter-github-password]}]
  (let [response (safely
                   (http/request
                     {:request-method :get
                      :url (str/join "/" [github-api-url "repos" github-repository "actions/secrets/public-key"])
                      :basic-auth [walter-actor walter-github-password]
                      :headers {"Content-Type" "application/json"
                                "Accept" "application/vnd.github.v3+json"}})
                   :on-error
                   :max-retries 5)
        payload (->> response
                     :body
                     json/read-value)
        ^String encoded-key (get payload "key")]
    {:encoded-key encoded-key
     :decoded-key (.decode (Base64/getDecoder) (.getBytes encoded-key))
     :key-id (get payload "key_id")}))

(defn public-key-sealed-box
  [{:keys [^String decoded-key key-id]} ^String secret-value]
  {:encrypted_value (->> (byte-streams/to-byte-array secret-value)
                         (crypto/anonymous-encrypt decoded-key)
                         (.encodeToString (Base64/getEncoder)))
   :key_id key-id})

(defn upsert-value
  [{:keys [github-repository github-api-url walter-actor walter-github-password] :as config} ^String secret-name ^String secret-value]
  (println :secret-name secret-name)
  (println :secret-value (type secret-value))
  (assert secret-name (format "Secret %s not found, looked up as %s."))
  (let [public-key (repository-public-key config)
        sealed-box (public-key-sealed-box public-key secret-value)]
    (safely
      (http/request
        {:request-method :put
         :url (str/join "/" [github-api-url "repos" github-repository "actions" "secrets" secret-name])
         :body (json/write-value-as-string sealed-box)
         :basic-auth [walter-actor walter-github-password]
         :headers {"Content-Type" "application/json"
                   "Accept" "application/vnd.github.v3+json"}})
      :on-error
      :max-retries 5)))
