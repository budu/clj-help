
(ns clj-help
  "Simple help macro to regroup contrib functions useful for interactive
  development."
  (:require [clojure.contrib.ns-utils :as ns-utils]
            [clojure.contrib.repl-utils :as repl-utils])
  (:use clojure.contrib.classpath))

;;;; Utilities

(defn- print-files [files]
  (doseq [f files]
    (println (.getAbsolutePath f))))

(defmacro #^{:private true} defquery
  [name usage args body]
  `(def ~(with-meta name {:private true :doc usage ::query true})
     (fn [& ~args] ~body)))

;;;; Queries

(defquery cp
  "Prints all files in the classpath, accept 'jars' or 'dirs' as optional argument."
  [only]
  (print-files
   (condp = only
     'jars (map #(java.io.File. (.getName %)) (classpath-jarfiles))
     'dirs (classpath-directories)
     (classpath))))

(defquery dir
  "Prints a sorted directory of public vars in the given namespace, or *ns* if none."
  [namespace]
  (ns-utils/print-dir (or namespace *ns*)))

(defquery docs
  "Prints documentation for the public vars in the given namespace, or *ns* if none."
  [namespace]
  (ns-utils/print-docs (or namespace *ns*)))

(defquery info
  "Analyzes the given s-expr and prints the class of the value it returns."
  [expr]
  (when expr
    (let [{:keys [class primitive?]}
          (repl-utils/expression-info expr)]
      (println class))))

(defquery show
  "Prints all instance members of the given class with an optional int, string or regex selector."
  [klass selector]
  (when klass
    (apply repl-utils/show
           (conj [(resolve klass)] selector))))

(defquery source
  "Prints a string of the source code for the given symbol"
  [sym]
  (when sym (repl-utils/source sym)))

;;;; Help macro

(def #^{:private true} queries
  (filter (comp ::query meta)
          (map val (ns-interns 'clj-help))))

(defn- print-usage []
  (println "Usage: (help <query> ...)")
  (doseq [q queries]
    (let [m (meta q)]
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
