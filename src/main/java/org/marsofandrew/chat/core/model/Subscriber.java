package org.marsofandrew.chat.core.model;

import java.time.Instant;

public interface Subscriber<M> {
    void handleMessage(String topic, String sender, M message, Instant instant);


}
