
(ns clj-help
  "Simple help macro to regroup contrib functions useful for interactive
  development."
  (:require [clojure.contrib.ns-utils :as ns-utils])
  (:use clojure.contrib.classpath))

(defn- print-files [files]
  (doseq [f files]
    (println (.getAbsolutePath f))))

(defn- cp
  "Prints all files in the classpath, accept 'jars' or 'dirs' as optional argument."
  [& [only]]
  (print-files
   (condp = only
     'jars (map #(java.io.File. (.getName %)) (classpath-jarfiles))
     'dirs (classpath-directories)
     (classpath))))

(defn- dir
  "Prints a sorted directory of public vars in the given namespace, or *ns* if none."
  [& [namespace]]
  (ns-utils/print-dir (or namespace *ns*)))

(def #^{:private true} queries
  ['cp 'dir])

(defn- print-usage []
  (println "Usage: (help <query> ...)")
  (doseq [q queries]
    (let [f (ns-resolve 'clj-help q)
          m (meta f)]
      (println " " (:name m) "-" (:doc m)))))

(defn help*
  "Driver function for the help macro."
  ([] (print-usage))
  ([query & args]
     (apply (ns-resolve 'clj-help query) args)))

(defmacro help
  "Regroup contrib functions useful for interactive development. Call
  without arguments for usage."
  [& [query & args]]
  `(help* ~@(when query
              (map #(list 'quote %)
                   (conj args query)))))
