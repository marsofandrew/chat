package org.marsofandrew.chat.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.marsofandrew.chat.core.model.Message;
import org.marsofandrew.chat.core.model.Publisher;
import org.marsofandrew.chat.core.model.Subscriber;
import org.marsofandrew.chat.core.utils.FramedQueue;
import org.marsofandrew.chat.core.utils.LimitedLinkedList;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to handle different topics
 *
 * @param <M> Message type.
 */
@Slf4j
@RequiredArgsConstructor
public final class TopicService<M> {

    private final static int QUEUE_K = 3;
    private final Map<String, Topic<M>> topics = new ConcurrentHashMap<>();

    private final int messageLimit;
    private final int publishersLimit;
    private final int subscribersLimit;


    public Topic<M> getTopic(String topic) {
        return topics.computeIfAbsent(topic,
                ign -> new TopicService.Topic<>(topic, messageLimit,
                        subscribersLimit, publishersLimit));
    }

    public static class Topic<M> {
        @Getter
        private final String topic;

        private final int messageLimit;

        private final List<Message<M>> messages;
        private final List<Publisher<M>> publishers;
        private final List<Subscriber<M>> subscribers;

        protected Topic(String topic, int messageLimit, Integer subscriberLimit, Integer publisherLimit) {
            this.topic = topic;
            this.messages = Collections.synchronizedList(new FramedQueue<>(messageLimit));
            this.publishers = Collections.synchronizedList(new LimitedLinkedList<>(publisherLimit));
            this.subscribers = Collections.synchronizedList(new LimitedLinkedList<>(subscriberLimit));
            this.messageLimit = messageLimit;
        }

        public void publish(String sender, M message) {
            Instant now = Instant.now();
            messages.add(new Message<>(now, message, sender));
            subscribers.parallelStream().forEach(subscriber -> subscriber.handleMessage(topic, sender, message, now));
        }

        public synchronized void registerPublisher(@NonNull Publisher<M> publisher) {
            publishers.add(publisher);
        }

        public synchronized void registerSubscriber(@NonNull Subscriber<M> subscriber) {
            messages.forEach(message ->
                    subscriber.handleMessage(topic, message.sender(), message.message(), message.timestamp()));
            subscribers.add(subscriber);
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
    }
}


