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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class Clients {

    private static final ConcurrentHashMap<String, Client> CLIENTS = new ConcurrentHashMap<>();

    public static Client login(@NonNull String username, @NonNull String password) {
        var client = CLIENTS.computeIfAbsent(username, ign -> new Client(username, password));
        if (!client.password.equals(password)) {
            throw new InvalidPasswordException();
        }
        return client;
    }

    @RequiredArgsConstructor
    public static class Client implements Subscriber<String>, Publisher<String> {
        private static final String RESPONSE_FORMAT = "FROM %s at %s: %s\n";
        private final String user;
        private final String password;
        private Channel channel;
        private Topics.Topic<String> currentTopic;

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
        public void handleMessage(String topic, String sender, String message, Instant instant) {
            if (channel == null || !channel.isActive()) {
                log.error("[{}] Couldn't handle message", user);
            }
            channel.writeAndFlush(String.format(RESPONSE_FORMAT, sender, instant.toString(), message));
        }

        public void joinChannel(String topic) {
            if (currentTopic != null && currentTopic.getTopic().equals(topic)) {
                return;
            }
            if (currentTopic != null) {
                currentTopic.unregisterClient(this, this);
            }

            var topicChannel = Topics.getStringTopic(topic);
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

        public synchronized void updateChat() {
            if (currentTopic != null) {
                currentTopic.getMessages().forEach(message ->
                        handleMessage(currentTopic.getTopic(), message.sender(), message.message(), message.timestamp()));
            }
        }

        public Client setChannel(@NonNull Channel channel) {
            this.channel = channel;
            return this;
        }
    }
}
