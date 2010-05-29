
(ns clj-help
  "Simple help macro to regroup contrib functions useful for interactive
  development."
  (:use clojure.contrib.classpath))

(defn- print-files [files]
  (doseq [f files]
    (println (.getAbsolutePath f))))

(defn- cp [& [only]]
  (print-files
   (condp = only
     'jars (map #(java.io.File. (.getName %)) (classpath-jarfiles))
     'dirs (classpath-directories)
     (classpath))))

(defn- print-usage []
  (println "Usage: ..."))

(defn help*
  "Driver function for the help macro."
  ([] (print-usage))
  ([query & args]
     (apply (resolve query) args)))

(defmacro help
  "Regroup contrib functions useful for interactive development. Call
  without arguments for usage."
  [& [query & args]]
  `(help* ~@(when query
              (map #(list 'quote %)
                   (conj args query)))))
