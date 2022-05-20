package org.marsofandrew.chat.core.model;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

public interface Subscriber<M> {
    Callable<Boolean> handleMessage(String topic, String sender, M message, Instant instant);


}
