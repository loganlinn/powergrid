(ns powergrid-client.util.macros)

(defmacro go-loop [& body]
  `(cljs.core.async.macros/go
     (while true
       ~@body)))
