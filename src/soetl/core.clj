(ns soetl.core
  (:require [clojure.java.jdbc :as j]
            [clojure.string :as string]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:use [clojure.tools.logging :only [info warn error]]))

(def db {:subprotocol "h2"
            :subname "~/test"
            :user "sa"
            :password ""})

(defmacro defjob [job-name job-attr & body]
  `(def ~job-name (fn [ctx#] (-> ctx# ~@body))))

(defn prepare-table [table columns & {:keys [truncate?] :or {truncate? false}}]
  (j/with-db-metadata [md db]
    (if (empty? (j/metadata-result
                  (.getTables md nil nil (string/upper-case table) (into-array ["TABLE"])))) 
      (j/db-do-commands db (apply j/create-table-ddl table
                             (for [column columns] [(keyword column) "varchar(255)"])))
      (when truncate?
        (j/db-do-commands db (str "TRUNCATE TABLE " table))))))

(defmulti extract-from (fn [type reader table] type))

(defmethod extract-from :csv [_ reader table]
  (let [[columns & records] (csv/read-csv reader)]
    (prepare-table table columns :truncate? true)
    (j/with-db-transaction [conn db]
      (doseq [record records]
        (j/insert! conn table (zipmap columns record))))))
      
(defn extract [ctx url & {format :format}]
  (let [url (io/as-url url)
       [_ table extension] (re-find #"/([^/]*)\.(\w+)$" (.getPath url))]
    (with-open [reader (io/reader url)]
      (info "Extract" url "to" table)
      (extract-from (keyword extension) reader table))
    ctx))

(defn transform [ctx sql]
  (assoc ctx :select sql))

(defmulti load-to-table (fn [mode table select-stmt & bulk] mode))

(defmethod load-to-table :update-insert [_ table select-stmt & bulk]
  (j/execute! db [(str "MERGE INTO " table " " select-stmt) ]))

(defmethod load-to-table :delete-insert [_ table select-stmt & bulk]
  (j/delete!  db (keyword table) [])
  (j/execute! db [(str "INSERT INTO " table " " select-stmt) ]))

(defmulti load-to-file (fn [format path select-stmt] format))

(defmethod load-to-file :csv [_ path select-stmt]
  (with-open [wtr (io/writer path)]
    (csv/write-csv wtr (j/query db [select-stmt] :as-arrays? true))))

(defmulti load-to (fn [ctx & options]
                    (some (-> (apply hash-map options) keys set) [:table :file :url])))

(defmethod load-to :table [ctx & {:keys [table mode bulk] :or {mode :update-insert}}]
  (if-let [select-stmt (:select ctx)]
    (load-to-table mode table select-stmt)
    (warn "If you want to load, you must call transform first.")))

(defmethod load-to :file [ctx & {:keys [file format] :or {format :csv}}]
  (if-let [select-stmt (:select ctx)]
    (load-to-file format file select-stmt)))
