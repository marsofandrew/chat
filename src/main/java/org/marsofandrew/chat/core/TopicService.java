package org.marsofandrew.chat.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.marsofandrew.chat.core.exception.NotSingleElementException;
import org.marsofandrew.chat.core.model.Message;
import org.marsofandrew.chat.core.model.Publisher;
import org.marsofandrew.chat.core.model.Subscriber;
import org.marsofandrew.chat.core.utils.FramedQueue;
import org.marsofandrew.chat.core.utils.LimitedLinkedList;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Service to handle different topics
 *
 * @param <M> Message type.
 */
@Slf4j
@RequiredArgsConstructor
public final class TopicService<M> implements AutoCloseable {

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

    @Override
    public void close() throws Exception {
        for (var topic : topics.values()) {
            topic.close();
        }
    }

    public static class Topic<M> implements AutoCloseable {
        @Getter
        private final String topic;

        private final int messageLimit;

        private final List<Message<M>> messages;
        private final List<Publisher<M>> publishers;
        private final List<SubscriberThread> subscribers;

        protected Topic(String topic, int messageLimit, Integer subscriberLimit, Integer publisherLimit) {
            this.topic = topic;
            this.messages = Collections.synchronizedList(new FramedQueue<>(messageLimit));
            this.publishers = Collections.synchronizedList(new LimitedLinkedList<>(publisherLimit));
            this.subscribers = Collections.synchronizedList(new LimitedLinkedList<>(subscriberLimit));
            this.messageLimit = messageLimit;
        }

        public void publish(String sender, M message) {
            Instant now = Instant.now();
            var m = new Message<>(now, message, sender);
            messages.add(m);
            subscribers.parallelStream().forEach(subscriber -> subscriber.send(m));
        }

        public synchronized void registerPublisher(@NonNull Publisher<M> publisher) {
            publishers.add(publisher);
        }

        public synchronized void registerSubscriber(@NonNull Subscriber<M> subscriber) {
            var subscriberThread = new SubscriberThread(QUEUE_K * messageLimit, subscriber);
            messages.forEach(subscriberThread::send);
            subscriberThread.start();
            subscribers.add(subscriberThread);
        }

        public synchronized void unregisterPublisher(@NonNull Publisher<M> publisher) {
            publishers.remove(publisher);
        }

        public synchronized void unregisterSubscriber(@NonNull Subscriber<M> subscriber) {

            var subscriberThread = getSubscriberThread(subscriber);
            subscriberThread.finish();
            try {
                subscriberThread.join();
            } catch (InterruptedException e) {
                log.error("Error", e);
            }
            subscribers.remove(subscriberThread);
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

        private SubscriberThread getSubscriberThread(Subscriber<M> subscriber) {
            var list = subscribers.parallelStream()
                    .filter(subscriberThread -> subscriberThread.subscriber.equals(subscriber))
                    .toList();
            if (list.size() != 1) {
                throw new NotSingleElementException("" + list.size());
            }
            return list.get(0);
        }

        @Override
        public void close() throws Exception {
            for (SubscriberThread thread : subscribers) {
                thread.finish();
                thread.join();
                System.out.println("thread is finished");
            }
        }

        private class SubscriberThread extends Thread {

            private final LimitedLinkedList<Callable<Boolean>> queue;
            private final CountDownLatch latch;

            private final Subscriber<M> subscriber;

            private final Object pause = new Object();

            private boolean isRun;

            private SubscriberThread(Integer queueSize, Subscriber<M> subscriber) {
                this.queue = new LimitedLinkedList<>(queueSize);
                this.latch = new CountDownLatch(1);
                this.subscriber = subscriber;
                this.isRun = true;
            }


            private void addAction(Callable<Boolean> action) throws InterruptedException {
                synchronized (pause) {
                    if (queue.size() >= queue.getLimit()) {
                        latch.await();
                    }
                    queue.add(action);
                    pause.notifyAll();
                }
            }

            void send(Message<M> message) {
                var action = subscriber.handleMessage(topic, message.sender(),
                        message.message(), message.timestamp());
                try {
                    if (action != null) {
                        addAction(action);
                    }
                } catch (InterruptedException e) {
                    log.error("Thread is interrupted", e);
                }
            }

            @Override
            public void run() {
                while (isRun) {
                    synchronized (pause) {
                        var action = queue.poll();
                        if (action == null) {
                            try {
                                pause.wait();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            continue;
                        }

                        Boolean result = null;
                        try {
                            result = action.call();
                        } catch (Exception e) {
                            log.error("Error", e);
                        }
                        latch.countDown();
                        if (result == null || !result) {
                            queue.clear();
                        }
                    }

                }
            }

            void finish() {
                isRun = false;
            }
        }
    }
}


