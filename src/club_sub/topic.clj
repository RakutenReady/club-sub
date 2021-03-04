(ns club-sub.topic
  (:require
   [clojure.string :as string]
   [club-sub.client :as client])
  (:import
   (com.google.cloud.pubsub.v1 TopicAdminClient)
   (com.google.pubsub.v1 ProjectTopicName ProjectName)
   (java.util.concurrent TimeUnit)))

(def ^:private module *ns*)

(defn get-client
  "Create a TopicAdminClient, which is used to call the other functions
   in this namespace. You must call shutdown on it when you are done using it."
  [config]
  (client/get-client module config))

(defn shutdown
  "Shuts down the topic client"
  [^TopicAdminClient client]
  (.shutdown client)
  (.awaitTermination client 1 TimeUnit/MINUTES))

(defn create
  "Creates a new topic for the configured project"
  [^TopicAdminClient client config topic-name]
  (let [topic (ProjectTopicName/of (:project-id config) topic-name)]
    (.createTopic client topic)))

(defn get
  "Returns an existing topic for the configured project"
  [^TopicAdminClient client config topic-name]
  (let [topic (ProjectTopicName/of (:project-id config) topic-name)]
    (.getTopic client topic)))

(defn delete
  "Deletes an existing topic for the configured project"
  [^TopicAdminClient client config topic-name]
  (let [topic (ProjectTopicName/of (:project-id config) topic-name)]
    (.deleteTopic client topic)))

(defn get-list
  "Returns a list containing all the topics for the configured project"
  [^TopicAdminClient client config]
  (let [project (ProjectName/of (:project-id config))
        topics (.listTopics client project)]
    (map #(last (string/split (.getName %) #"/")) (.iterateAll topics))))

(defn get-sub-list
  "Returns a list containing all the subcriptions for the specified project & topic"
  [^TopicAdminClient client config topic-name]
  (let [project (ProjectTopicName/of (:project-id config) topic-name)
        subs (.listTopicSubscriptions client project)]
    (map #(last (string/split % #"/")) (.iterateAll subs))))
