# kemplittle

generated using Luminus version "3.54"

FIXME

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

## License

To start the frontend developlement, run: 
    
    clj -A:build-dev


Copyright © 2019 FIXME
