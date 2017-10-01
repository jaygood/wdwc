# wdwc

## tests

lein test-refresh

## stuff
lein run

lein repl :connect 7000

(require '[wdwc.db.core :refer :all])
(mount/start #'wdwc.db.core/*db*)

OR

(use '[myapp.db.core])
(mount/start #'*db*)
(get-messages)


lein test-refresh





## package

lein uberjar

export DATABASE_URL="jdbc:h2:./wdwc_dev.db"
java -jar target/uberjar/wdwc.jar
