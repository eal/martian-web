(ns martian-web.routes.home
  (:require [compojure.core :refer :all]
            [martian-web.layout :as layout]
            [martian-web.util :as util]
            [instaparse.core :as insta]
            [instaparse.print :as instaprint]
            [instaparse.failure :as instafail]
            [clojure.string :as str]
            [cheshire.core :as json]))

(defn home-page []
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")}))

(defn docs-page []
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")}))

(defn about-page []
  (layout/render "about.html"))

(defn martian-page []
  (layout/render "martian.html"))

(defn parse-log-entry [parser entry]
  (let [result (insta/parse parser entry)]
    (if (insta/failure? result)
      {:status "failure"
       :entry entry
       :result (with-out-str
             (instafail/pprint-failure result))}
      {:status "success"
       :entry entry
       :result result})))

(defn martian-parse [rules logs]
  (let [parser (insta/parser rules)
        log-lines (str/split-lines (str/trim logs))]
    (map (partial parse-log-entry parser) log-lines)))

(defn parser-string [rules]
  (instaprint/Parser->str (insta/parser rules)))

(defn martian-post-page [rules logs]
  (let [result (martian-parse rules logs)
        number (count result)]
   (layout/render "martian.html" {:rules rules
                                  :count number
                                  :logs logs
                                  :result result})))
(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/martian" [] (martian-page))
  (POST "/martian" [rules logs] (martian-post-page rules logs))
  (GET "/docs" [] (docs-page))
  (GET "/about" [] (about-page)))
