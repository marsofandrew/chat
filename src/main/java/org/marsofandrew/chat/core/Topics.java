package org.marsofandrew.chat.core;

import lombok.Getter;
import lombok.NonNull;
import org.marsofandrew.chat.core.model.Message;
import org.marsofandrew.chat.core.model.Publisher;
import org.marsofandrew.chat.core.model.Subscriber;
import org.marsofandrew.chat.core.utils.FramedQueue;
import org.marsofandrew.chat.core.utils.LimitedLinkedList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Topics {
    private static final Map<String, Topic<String>> STRING_CHANNELS = new ConcurrentHashMap<>();

    private static final int DEFAULT_MESSAGES_LIMIT = 10;
    private static final int DEFAULT_PUBLISHERS_LIMIT = 10;
    private static final int DEFAULT_SUBSCRIBERS_LIMIT = 10;

    public static Topic<String> getStringTopic(String topic) {
        return STRING_CHANNELS.computeIfAbsent(topic,
                ign -> new Topics.Topic<>(topic, DEFAULT_MESSAGES_LIMIT,
                        DEFAULT_SUBSCRIBERS_LIMIT, DEFAULT_PUBLISHERS_LIMIT));
    }

    private Topics() {
        throw new UnsupportedOperationException();
    }

    public static class Topic<M> {
        @Getter
        private final String topic;

        private final List<Message<M>> messages;
        private final List<Publisher<M>> publishers;
        private final List<Subscriber<M>> subscribers;

        protected Topic(String topic, int messageLimit, Integer subscriberLimit, Integer publisherLimit) {
            this.topic = topic;
            this.messages = Collections.synchronizedList(new FramedQueue<>(messageLimit));
            this.publishers = Collections.synchronizedList(new LimitedLinkedList<>(publisherLimit));
            this.subscribers = Collections.synchronizedList(new LimitedLinkedList<>(subscriberLimit));
        }

        public void publish(String sender, M message) {
            Instant now = Instant.now();
            messages.add(new Message<>(now, message, sender));
            subscribers.forEach(subscriber -> subscriber.handleMessage(topic, sender, message, now));
        }

        public synchronized void registerPublisher(@NonNull Publisher<M> publisher) {
            publishers.add(publisher);
        }

        public synchronized void registerSubscriber(@NonNull Subscriber<M> subscriber) {
            subscribers.add(subscriber);
            messages.forEach(message -> subscriber.handleMessage(topic, message.sender(), message.message(), message.timestamp()));
        }

        public synchronized void unregisterPublisher(@NonNull Publisher<M> publisher) {
            publishers.remove(publisher);
        }

        public synchronized void unregisterSubscriber(@NonNull Subscriber<M> subscriber) {
            subscribers.remove(subscriber);
        }

        public synchronized void registerClient(@NonNull Publisher<M> publisher, @NonNull Subscriber<M> subscriber) {
            registerPublisher(publisher);
            registerSubscriber(subscriber);
        }

        public synchronized void unregisterClient(@NonNull Publisher<M> publisher, @NonNull Subscriber<M> subscriber) {
            unregisterPublisher(publisher);
            unregisterSubscriber(subscriber);
        }

        public List<String> getPublisherIds() {
            return publishers.stream().map(Publisher::getPublisherName).toList();
        }

        public List<Message<M>> getMessages() {
            return new ArrayList<>(messages);
        }

    }
}


