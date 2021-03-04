(ns club-sub.subscriber
  (:require
   [club-sub.client :as client])
  (:import
   (com.google.cloud.pubsub.v1 Subscriber AckReplyConsumer)))

(def ^:private module *ns*)

(defn create
  "Creates and returns a subscriber for the specified config"
  [{:keys [project-id emulator] :as config} subscription-id msg-handler error-handler]
  {:pre [project-id]}
  (client/get-client module (merge config {:subscription-id subscription-id
                                           :msg-handler msg-handler
                                           :error-handler error-handler})))

(defn subscribe
  "Given a subscriber, start receiving messages on the message handler"
  [^Subscriber subscriber]
  (-> subscriber
      (.startAsync)
      (.awaitRunning))
  (.awaitTerminated subscriber))

(defn shutdown [^Subscriber subscriber]
  "Shuts down the subscriber"
  (.stopAsync subscriber))

(defn ack [^AckReplyConsumer consumer]
  (.ack consumer))
