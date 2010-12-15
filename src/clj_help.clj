;; Copyright (c) 2010 Nicolas Buduroi. All rights reserved.
;; Permission is hereby granted, free of charge, to any person obtaining a copy
;; of this software and associated documentation files (the "Software"), to deal
;; in the Software without restriction, including without limitation the rights
;; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
;; copies of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:
;; The above copyright notice and this permission notice shall be included in
;; all copies or substantial portions of the Software.

(ns ^{:author "Nicolas Buduroi"}
  clj-help
  "Simple help macro to regroup contrib functions useful for interactive
  development."
  (:require (clojure.contrib [ns-utils :as ns-utils]
                             [repl-utils :as repl-utils]
                             [pprint :as pprint]
                             [classpath :as cp])))

;;;; Utilities

(defn- print-files [files]
  (doseq [f files]
    (println (.getAbsolutePath f))))

(defmacro ^{:private true} defquery
  [name usage args & body]
  `(def ~(with-meta name {:private true :doc usage ::query true})
     (fn [& ~args] ~@body)))

;;;; Queries

(defquery cp
  "Prints all files in the classpath, accept 'jars' or 'dirs' as optional argument."
  [only]
  (print-files
   (condp = only
     'jars (map #(java.io.File. (.getName %)) (cp/classpath-jarfiles))
     'dirs (cp/classpath-directories)
     (cp/classpath))))

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
  (when sym (println (repl-utils/get-source sym))))

(defquery macro
  "Pretty prints the macro expansion of the given form."
  [form]
  (when form (pprint/pprint (macroexpand form))))

(defquery pp
  "Evaluates the given form and pretty prints its result."
  [form]
  (when form (pprint/pprint (eval form))))

(defquery clean
  "Remove a namespace (and used vars) from the given namespace, or *ns* if none."
  [ns-to-reload from-ns]
  (let [sym #(symbol (.name %))
        orig-ns (sym *ns*)
        from-ns (or from-ns orig-ns)
        vars (ns-utils/ns-vars ns-to-reload)]
    (in-ns from-ns)
    (remove-ns ns-to-reload)
    (doseq [v vars]
      (ns-unmap from-ns v))
    (in-ns orig-ns)))

;;;; Help macro

(def ^{:private true} queries
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
     (if-let [query (ns-resolve 'clj-help query)]
       (apply query args)
       (print-usage))))

(defmacro help
  "Regroup contrib functions useful for interactive development. Call
  without arguments for usage."
  [& [query & args]]
  `(help* ~@(when query
              (map #(list 'quote %)
                   (conj args query)))))
