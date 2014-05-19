(ns soetl.core-test
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as j])
  (:use [midje.sweet]
        [soetl.core])
  (:import [java.sql SQLException]))

(fact ""
  (j/db-do-commands db (j/drop-table-ddl :test1))
  (with-open [rdr (io/reader (io/resource "test.csv"))]
    (extract-from :csv rdr "test1"))
  (try
    (j/db-do-commands db
      (j/drop-table-ddl :test2))
    (catch SQLException e)
    (finally
      (j/db-do-commands db
        (j/create-table-ddl :test2 [:id :integer "PRIMARY KEY"] [:name "varchar(255)"]))))
  (load-to-table :update-insert "test2"
    "SELECT id, name FROM test1")
  (j/query db "SELECT id FROM test2 ORDER BY id") => [{:id 1} {:id 2}]
  (j/db-do-commands db (j/drop-table-ddl :test1)))



(defjob load-entries {:trigger {:cycle "1 * * * *"}}
  (extract (io/resource "test1.csv"))
  (transform "SELECT NAME, NUM FROM test1")
  (load-to :table "kankore" :mode :delete-insert))

(defjob out-entries {:trigger {:cycle "1 * * * *"}}
  (transform "SELECT NAME, NUM FROM kankore")
  (load-to :file "target/kankore.csv" :format :csv))

(fact "defjob"
  (try
    (j/db-do-commands db
      (j/drop-table-ddl :kankore))
    (catch SQLException e)
    (finally
      (j/db-do-commands db
        (j/create-table-ddl :kankore [:name "varchar(255)"] [:num "varchar(255)"]))))
  
  (load-entries {})
  (out-entries {}))

