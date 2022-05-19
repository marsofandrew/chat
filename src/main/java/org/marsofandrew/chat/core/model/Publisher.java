package org.marsofandrew.chat.core.model;

public interface Publisher<M> {
    void sendMessage(M message);
    String getPublisherName();
}
