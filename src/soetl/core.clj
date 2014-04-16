(ns soetl.core)

(def mysql-db {:subprotocol "h2"
               :subname "//127.0.0.1:3306/clojure_test"
               :user "sa"
               :password ""})

(defmacro defjob [job-name {} & body]
  `(fn [ctx#] ~@body))


(defmulti load-to (fn [ctx & {:table}]))

(defmulti load-to :table (fn [ctx & {table :table mode :mode}])
  (if-let [sql (:sql ctx)]
    ()))

(defjob load-yubin {:trigger {:cycle "1 * * * *"}}
  (extract "resources/test-1.csv")
  (transform "test-1")
  (load-to :table "kankore" :mode insert))
