(require
 '[clojure.string :as s]
 '[clojure.java.io :as io])

(load-file "development/cli.clj")
(load-file "development/util.clj")

(def git-ignore-cache {})

(defn ->file [file-or-string]
  (if (string? file-or-string) (io/file file-or-string) file-or-string))

(defn file? [file]
  (-> file ->file (#(.isFile %))))

(defn md? [file]
  (when (s/ends-with? (.toString file) ".md") file))

(defn line-to-v [file]
  (with-open [rdr (io/reader (.getAbsolutePath file))]
    (mapv identity (line-seq rdr))))

(defn root? [dir]
  (member? ".git" (.list dir)))

(defn dir? [dir]
  (let [dir (->file dir)]
    (if (.isDirectory dir)
      dir
      (exit 1 (str "Invalid directory " dir)))))

(defn find-root-dir [file]
  (let [file (->file file)]
    (if (.isDirectory file)
      (if (root? file) file (find-root-dir (.getParentFile file)))
      (find-root-dir (.getParentFile file)))))

#_(defn git-ignore? [file]
  (let [file-name (.getName file)
        root      (find-root-dir file)
        ignore-re (or (get git-ignore-cache (.getAbsolutePath root))
                      (filter #(not (s/starts-with? % "#"))
                              (line-to-v (io/file root ".gitignore"))))]
    (member? true (map #(re-find (re-pattern %) file-name) ignore-re))))

(defn travers-dir [dir f]
  (if (.isDirectory dir)
    (mapv #(travers-dir % f) (.listFiles dir))
    (f dir)))