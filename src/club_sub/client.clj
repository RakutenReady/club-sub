(ns club-sub.client
  (:import
   (com.google.api.gax.batching BatchingSettings FlowControlSettings)
   (com.google.api.gax.core NoCredentialsProvider)
   (com.google.api.gax.grpc GrpcTransportChannel)
   (com.google.api.gax.rpc FixedTransportChannelProvider)
   (com.google.cloud.pubsub.v1 TopicAdminClient SubscriptionAdminClient Subscriber Publisher TopicAdminSettings Publisher$Builder TopicAdminSettings$Builder SubscriptionAdminSettings SubscriptionAdminSettings$Builder MessageReceiver Subscriber$Builder)
   (com.google.pubsub.v1 ProjectTopicName ProjectSubscriptionName PubsubMessage)
   (io.grpc ManagedChannelBuilder)
   (java.util.concurrent TimeUnit)))

;; NOTE: This is an internal helper namespace which
;; should not be used directly in applications.

(defn- set-emulator-conf
  [builder {:keys [emulator-host] :as _config}]
  (let [host (or emulator-host
                 (System/getenv "PUBSUB_EMULATOR_HOST")
                 (throw (ex-info "Can't determine emulator host. Please run $(gcloud beta emulators pubsub env-init) or provide :emulator-host \"localhost:8538\" in your config map." {})))
        channel (-> (ManagedChannelBuilder/forTarget host)
                    (.usePlaintext)
                    (.build))
        channel-provider (-> (GrpcTransportChannel/create channel)
                             (FixedTransportChannelProvider/create))
        credentials-provider (NoCredentialsProvider/create)]
    (cond-> builder
      (= TopicAdminSettings$Builder (type builder))
      (.setTransportChannelProvider channel-provider)

      (= SubscriptionAdminSettings$Builder (type builder))
      (.setTransportChannelProvider channel-provider)

      (= Publisher$Builder (type builder))
      (.setChannelProvider channel-provider)

      (= Subscriber$Builder (type builder))
      (.setChannelProvider channel-provider)

      :always (.setCredentialsProvider credentials-provider))))

(defn- set-batching-conf
  [builder config]
  (let [flow-settings (cond-> (FlowControlSettings/newBuilder)
                        (:max-elements-limit config)
                        (.setMaxOutstandingElementCount (:max-elements-limit config))

                        (:max-bytes-limit config)
                        (.setMaxOutstandingRequestBytes (:max-bytes-limit config))

                        :always (.build))
        batch-settings (cond-> (BatchingSettings/newBuilder)
                         (:batching config)
                         (.setIsEnabled (:batching config))

                         (:delay-threshold config)
                         (.setDelayThreshold (:delay-threshold config))

                         (:max-elements-threshold config)
                         (.setElementCountThreshold (:max-elements-threshold config))

                         (:max-bytes-threshold config)
                         (.setRequestByteThreshold (:max-bytes-threshold config))

                         (nil? flow-settings)
                         (.setFlowControlSettings flow-settings)

                         :always (.build))]
    (cond-> builder
      (= Publisher$Builder (type builder))
      (.setBatchingSettings batch-settings)

      (= Subscriber$Builder (type builder))
      (.setFlowControlSettings flow-settings))))

(defmulti get-client
  (fn [module config]
    [(str module) (:emulator config)]))

(defmethod get-client ["club-sub.topic" true] [_ config]
  (let [^TopicAdminSettings$Builder builder (TopicAdminSettings/newBuilder)]
    (-> builder
        ^TopicAdminSettings$Builder (set-emulator-conf config)
        (.build)
        (TopicAdminClient/create))))

(defmethod get-client ["club-sub.topic" false] [_ _]
  (let [^TopicAdminSettings$Builder builder (TopicAdminSettings/newBuilder)]
    (-> builder
        (.build)
        (TopicAdminClient/create))))

(defmethod get-client ["club-sub.subscription" true] [_ config]
  (let [^SubscriptionAdminSettings$Builder builder (SubscriptionAdminSettings/newBuilder)]
    (-> builder
        ^SubscriptionAdminSettings$Builder (set-emulator-conf config)
        (.build)
        (SubscriptionAdminClient/create))))

(defmethod get-client ["club-sub.subscription" false] [_ _]
  (let [^SubscriptionAdminSettings$Builder builder (SubscriptionAdminSettings/newBuilder)]
    (-> builder
        (.build)
        (SubscriptionAdminClient/create))))

(defmethod get-client ["club-sub.subscriber" true] [_ config]
  (let [msg-receiver (reify MessageReceiver
                       (receiveMessage [_ msg consumer]
                         (let [msg-map {:msg (.toByteArray (.getData msg))
                                        :id (.getMessageId msg)}]
                           ((:msg-handler config) msg-map consumer))))
        sub (ProjectSubscriptionName/of (:project-id config) (:subscription-id config))
        ^Subscriber$Builder builder (Subscriber/newBuilder sub msg-receiver)]
    (-> builder
        (set-emulator-conf config)
        (set-batching-conf config)
        (.build))))

(defmethod get-client ["club-sub.subscriber" false] [_ config]
  (let [msg-receiver (reify MessageReceiver
                       (receiveMessage [_ msg consumer]
                         (let [msg-map {:msg (.toByteArray (.getData msg))
                                        :id (.getMessageId msg)}]
                           ((:msg-handler config) msg-map consumer))))
        sub (ProjectSubscriptionName/of (:project-id config) (:subscription-id config))
        ^Subscriber$Builder builder (Subscriber/newBuilder sub msg-receiver)]
    (-> builder
        (set-batching-conf config)
        (.build))))

(defmethod get-client ["club-sub.publisher" true] [_ config]
  (let [topic (ProjectTopicName/of (:project-id config) (:topic-name config))
        ^Publisher$Builder builder (Publisher/newBuilder topic)]
    (-> builder
        (set-emulator-conf config)
        (set-batching-conf config)
        (.build))))

(defmethod get-client ["club-sub.publisher" false] [_ config]
  (let [topic (ProjectTopicName/of (:project-id config) (:topic-name config))
        ^Publisher$Builder builder (Publisher/newBuilder topic)]
    (-> builder
        (set-batching-conf config)
        (.build))))
