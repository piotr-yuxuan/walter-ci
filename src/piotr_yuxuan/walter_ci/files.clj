(ns piotr-yuxuan.walter-ci.files
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io File)
           (java.net URI URL)
           (clojure.lang Named)
           (java.nio.file Files Path)
           (java.nio.file.attribute FileAttribute))
  (:gen-class))

(defn ^File ->file
  "Do-what-I-mean syntactic sugar that return the canonical file for its arguments. Return nil if not possible."
  ([] (->file "."))
  ([x & r]
   (letfn [(join-seq [c]
             (->> (flatten c) ; Perhaps a legit use case of flatten?
                  (map path-elem)
                  (remove nil?)
                  (str/join (File/separator))))
           (path-elem [x]
             (as-> x $
               (if (instance? Named $) (name $) $)
               (if (instance? Path $) (.toFile ^Path $) $)
               (if (instance? URI $) (.getPath ^URI $) $)
               (if (instance? URL $) (.getFile ^URL $) $)))]
     ;; Try to bring anything in line with the happy path.
     (as-> x $
       (if (seq r) (cons x r) $)
       (if (sequential? $) (join-seq $) $)
       (path-elem $)
       (if (string? $) (io/file $) $)
       (when (instance? File $)
         (.getCanonicalFile ^File $))))))

(defn ^File ->tmp-dir
  ([] (->tmp-dir nil nil))
  ([prefix] (->tmp-dir prefix nil))
  ([prefix file-attributes]
   (-> prefix
       ^Path (Files/createTempDirectory (into-array FileAttribute file-attributes))
       .toFile
       .getCanonicalFile)))

(defn ^File ->tmp-file
  ([] (->tmp-file nil nil nil))
  ([suffix] (->tmp-file nil suffix nil))
  ([prefix suffix] (->tmp-file prefix suffix nil))
  ([prefix suffix file-attributes]
   (-> prefix
       ^Path (Files/createTempFile suffix (into-array FileAttribute file-attributes))
       .toFile
       .getCanonicalFile)))

(defn delete!
  "Idempotent procedure that ensures that the argument `file` no longer exists at the end."
  [^File file & [silently]]
  (when (.isDirectory file)
    (->> (.listFiles file)
         (pmap #(delete! % silently))
         doall))
  (io/delete-file file silently)
  nil)

(defn copy!
  [^File source ^File target
   & {:keys [callback]
      :or {callback (fn [_source _target] nil)}
      :as opts}]
  (if (.isDirectory source)
    (->> (.listFiles source)
         (pmap (fn [nested-source]
                 (let [nested-target (io/file target (.getName ^File nested-source))]
                   (copy! nested-source nested-target)
                   (callback nested-source nested-target))))
         doall)
    (apply io/copy source target opts))
  nil)

(defmacro with-delete!
  "bindings => [name init ...]

  Evaluates body in a try expression with names bound to the values
  of the inits, and a `finally` clause that deletes each file in reverse order."
  {:added "1.0"}
  [bindings & body]
  (#'clojure.core/assert-args
    (vector? bindings) "a vector for its binding"
    (even? (count bindings)) "an even number of forms in binding vector")
  (cond
    (= (count bindings) 0) `(do ~@body)
    (symbol? (bindings 0)) `(let ~(subvec bindings 0 2)
                              (try
                                (with-delete! ~(subvec bindings 2) ~@body)
                                (finally
                                  ; (~delete! ~(bindings 0))
                                  )))
    :else (throw (IllegalArgumentException.
                   "with-delete only allows Symbols in bindings"))))
