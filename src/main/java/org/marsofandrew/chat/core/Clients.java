package org.marsofandrew.chat.core;

import io.netty.channel.Channel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.marsofandrew.chat.core.exception.ClientNotJoinedToChannelException;
import org.marsofandrew.chat.core.exception.InvalidPasswordException;
import org.marsofandrew.chat.core.model.Publisher;
import org.marsofandrew.chat.core.model.Subscriber;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class Clients {

    private static final ConcurrentHashMap<String, Client> CLIENTS = new ConcurrentHashMap<>();

    public static Client login(@NonNull TopicService<String> topicService, @NonNull String username, @NonNull String password) {
        var client = CLIENTS.computeIfAbsent(username, ign -> new Client(topicService, username, password));
        if (!client.password.equals(password)) {
            throw new InvalidPasswordException();
        }
        return client;
    }

    /**
     * Class to represent single client
     */
    @RequiredArgsConstructor
    public static class Client implements Subscriber<String>, Publisher<String> {

        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private static final String RESPONSE_FORMAT = "FROM %s at %s: %s\n";

        private final TopicService<String> topicService;
        private final String user;
        private final String password;
        private Channel channel;
        private TopicService.Topic<String> currentTopic;

        @Override
        public void sendMessage(String message) {
            if (currentTopic == null) {
                throw new ClientNotJoinedToChannelException();
            }
            currentTopic.publish(user, message);
        }

        @Override
        public String getPublisherName() {
            return user;
        }

        @Override
        public Callable<Boolean> handleMessage(String topic, String sender, String message, Instant instant) {
            if (channel == null || !channel.isActive()) {
                log.error("[{}] Couldn't handle message", user);
                return null;
            }
            return () -> {
                try {
                    channel.writeAndFlush(String.format(RESPONSE_FORMAT, sender, DATE_TIME_FORMATTER.format(instant),
                            message)).sync().get();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            };

        }

        public void joinChannel(String topic) {
            if (currentTopic != null && currentTopic.getTopic().equals(topic)) {
                return;
            }
            if (currentTopic != null) {
                currentTopic.unregisterClient(this, this);
            }

            var topicChannel = topicService.getTopic(topic);
            topicChannel.registerClient(this, this);
            currentTopic = topicChannel;
        }

        public List<String> getUsers() {
            if (currentTopic == null) {
                throw new ClientNotJoinedToChannelException();
            }
            return currentTopic.getPublisherIds();
        }

        public void leave() {
            channel.flush().close();
        }

        public Client setChannel(@NonNull Channel channel) {
            this.channel = channel;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Client client = (Client) o;
            return user.equals(client.user) && password.equals(client.password);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, password);
        }
    }
}
