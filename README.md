# club-sub ![Alt text](resources/images/club-sub.png?raw=true "Club Sub")

Google PubSub in Clojure

### Usage

```clojure

;; Define a config map

(def cfg {:project-id "test-poc" :emulator true})
=> #'club-sub.core/cfg

;; Create a new topic

(topic/create cfg "test-topic")
=> #object[com.google.pubsub.v1.Topic 0x35b75e48 "name: \"projects/test-poc/topics/test-topic\"\n"]

;; List topics

(topic/get-list cfg)
=> ("test-topic")

;; Create a new subscription

(subscription/create cfg "test-sub" "test-topic")
=>
#object[com.google.pubsub.v1.Subscription
        0x4fc8b692
        "name: \"projects/test-poc/subscriptions/test-sub\"
         topic: \"projects/test-poc/topics/test-topic\"
         push_config {
         }
         ack_deadline_seconds: 10
         message_retention_duration {
           seconds: 604800
         }
         "]

;; List subscriptions

(subscription/get-list cfg)
=> ("test-sub")

;; Publishing messages
;; Messages must be byte arrays

(def pub (publisher/create cfg "test-topic"))
=> #'club-sub.core/pub

(-> pub
    (publisher/publish (byte-array [1 2 3]))
    (publisher/shutdown))
=> true

;; Subscribing messages

(def msg-handler (fn [msg consumer]
                   (prn (update msg :msg vec))
                   (subscriber/ack consumer)))
=> #'club-sub.core/msg-handler

(def sub (subscriber/create cfg subscription msg-handler identity))
=> #'club-sub.core/sub

(subscriber/subscribe sub)
{:msg [1 2 3], :id "4"}

```

