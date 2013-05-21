(ns powergrid.util.log)

(defn trace [& args] (.trace js/console (pr-str args)))
(defn debug [& args] (.debug js/console (pr-str args)))
(defn info [& args] (.info js/console (pr-str args)))
(defn warn [& args] (.warn js/console (pr-str args)))
(defn error [& args] (.error js/console (pr-str args)))
(defn fatal [& args] (.error js/console (pr-str args)))
(defn report [& args] (.info js/console (pr-str args)))
(defn spy [expr] (info expr) expr)

