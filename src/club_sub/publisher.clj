(ns club-sub.publisher
  (:require
   [club-sub.client :as client])
  (:import
   (com.google.api.core ApiFuture ApiFutureCallback ApiFutures)
   (com.google.cloud.pubsub.v1 Publisher)
   (com.google.protobuf ByteString)
   (com.google.pubsub.v1 PubsubMessage)
   (java.util.concurrent TimeUnit Executors)))

(def ^:private module *ns*)

(defn create
  "Creates and returns a publisher for the specified config"
  [{:keys [project-id emulator] :as config} topic-name]
  {:pre [project-id topic-name]}
  (client/get-client module (merge config {:topic-name topic-name})))

(defn publish
  "Given a client and a message as a byte array, publishes the message.
   Returns a com.google.api.core.ApiFuture."
  ([^Publisher client ^bytes message] (publish client message identity identity))
  ([^Publisher client ^bytes message success-fn failure-fn]
   (let [bytes (ByteString/copyFrom message)
         message (-> (PubsubMessage/newBuilder)
                     (.setData bytes)
                     (.build))]
     (let [^ApiFuture future (.publish client message)
           callbacks (reify ApiFutureCallback
                       (onSuccess [_ message-id]
                         (success-fn message-id))
                       (onFailure [_ throwable]
                         (failure-fn throwable)))]
       (ApiFutures/addCallback future callbacks)
       future))))

(defn shutdown [^Publisher client]
  "Shuts down the publisher."
  (.shutdown client)
  (.awaitTermination client 1 TimeUnit/MINUTES))
