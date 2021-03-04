(ns club-sub.subscription
  (:require
   [clojure.string :as string]
   [club-sub.client :as client])
  (:import
   (com.google.cloud.pubsub.v1 SubscriptionAdminClient)
   (com.google.pubsub.v1 ProjectTopicName ProjectSubscriptionName PushConfig ProjectName)
   (java.util.concurrent TimeUnit)))

(def ^:private module *ns*)

(defn get-client
  "Create a client for the other functions in this namespace. You must call
   shutdown on it when you are done using it."
  [config]
  (client/get-client module config))

(defn shutdown
  "Shuts down the subscription client"
  [^SubscriptionAdminClient client]
  (.shutdown client)
  (.awaitTermination client 1 TimeUnit/MINUTES))

(defn create
  "Creates a new subscription for the configured project and specified topic"
  [^SubscriptionAdminClient client subscription-id subscription-project-id topic-name topic-project-id]
  (let [topic (ProjectTopicName/of topic-project-id topic-name)
        sub (ProjectSubscriptionName/of subscription-project-id subscription-id)
        push-cfg (PushConfig/getDefaultInstance)]
    (.createSubscription client sub topic push-cfg 0)))

(defn get
  "Returns an existing subscription for the configured project"
  [^SubscriptionAdminClient client config sub-name]
  (let [sub (ProjectSubscriptionName/of (:project-id config) sub-name)]
    (.getSubscription client sub)))

(defn delete
  "Deletes an existing subscription for the configured project"
  [^SubscriptionAdminClient client config sub-name]
  (let [sub (ProjectSubscriptionName/of (:project-id config) sub-name)]
    (.deleteSubscription client sub)))

(defn get-list
  "Returns a list containing all the subscription for the configured project"
  [^SubscriptionAdminClient client config]
  (let [project (ProjectName/of (:project-id config))
        subs (.listSubscriptions client project)]
    (map #(last (string/split (.getName %) #"/")) (.iterateAll subs))))
