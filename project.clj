(defproject soetl "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/tools.logging "0.2.6"]
                 [com.h2database/h2 "1.4.177"]]
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
