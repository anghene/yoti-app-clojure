# Yoti App integration in Clojure Server/Client

App uses Clojure on backend, and CLJS with UIX library on frontend. It's an example of implementing Yoti App verification (Age verification and Identity Verification flows) in Clojure. I developed this for a client who no longer wanted it and they were acquired by a different firm.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

You will also need to make a file in your root folder called dev-config.edn.

Add following contents:

```
{:port 3000
 :nrepl-port 7000
 :database-url "jdbc:sqlite:kemplittle_dev.db"}
```


## Running

To start a web server for the application, run:

    lein run

## Developing

To start the frontend developlement using autoreload and figwheel, run:

    clj -A:build-dev

You can also hook in to the nrepl on port you configured

    lein repl :connect [PORT]

Or just launch a new repl and start the services from there, then navigate in namespaces

    lein repl
    (start)

## For creating artifacts:

First clean dev-out folder if you developed using figwheel

    rm resources/public/cljs-out -rf

Then before your build the jar pack the cljs with

    lein cljsbuild once

At last, run this command to build the jar file:

    clj -A:uberdeps --target [filename.jar]

## Env vars

For deploying, you need the following env vars:

YOTIAPP:

YOTIAPP_PRIV_KEY_PATH
YOTIAPP_SDK_ID

DOCSCAN:

    DOCSCAN_PRIV_KEY_PATH
    SUCCESS_URL
    ERROR_URL
    DOCSCAN_SDK_ID
    WEBHOOK_USR
    WEBHOOK_PW
    WEBHOOK_URL
    KL_REQUEST_FILE

SMTP:

    KL_SMTP_HOST
    KL_SMTP_USER
    KL_SMTP_PORT
    KL_SMTP_PASS
    KL_SMTP_SSL
    KL_FROM_EMAIL

DEBUGGING:

    KL_DEBUG_DOCSCAN
    KL_DEBUG_YOTIAPP
    KL_DEBUG_UUID
    KL_DEBUG_EMAIL

When developing and you want to control emails sending:

    SEND_EMAILS


## Deploying

When you have env vars setup, use the following to run the jar

    java -Dconf="dev-config.edn"  -cp [filename.jar] clojure.main -m kemplittle.core
