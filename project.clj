(defproject club-sub "6.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.google.cloud/google-cloud-pubsub "1.108.1"]]
  :plugins [[lein-pprint "1.2.0"]
            [lein-cljfmt "0.6.3"]
            [com.gfredericks/lein-how-to-ns "0.2.2"]
            [test2junit "1.4.2"]]
  :how-to-ns {:require-docstring?      false
              :sort-clauses?           true
              :allow-refer-all?        false
              :allow-extra-clauses?    false
              :align-clauses?          false
              :import-square-brackets? false}
  :aliases {"fix" ["do" ["cljfmt" "fix"] ["how-to-ns" "fix"]]}
  :repl-options {:init-ns club-sub.client}
  :deploy-repositories
  [["releases"
    {:url "https://maven.pkg.github.com/RakutenReady/club-sub"
     :username :env/github_actor
     :password :env/github_token
     :sign-releases false}]])
