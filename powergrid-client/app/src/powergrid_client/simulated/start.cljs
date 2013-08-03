(ns powergrid-client.simulated.start
  (:require [io.pedestal.app :as app]
            [io.pedestal.app.render.push.handlers.automatic :as d]
            [io.pedestal.app.protocols :as p]
            [powergrid.domain]
            [powergrid-client.start :as start]
            [powergrid-client.simulated.services :as services]
            ;; This needs to be included somewhere in order for the
            ;; tools to work.
            [io.pedestal.app-tools.tooling :as tooling]))

(defn ^:export main []
  ;; Create an application which uses the data renderer. The :data-ui
  ;; aspect is configured to run this main function. See
  ;;
  ;; config/config.clj
  ;;
  (let [app (start/create-app d/data-renderer-config)
        services (services/->WebsocketService (:app app))]
    (powergrid.domain/register-tag-parsers!)
    (app/consume-effects (:app app) services/services-fn)
    (p/start services)
    app))
