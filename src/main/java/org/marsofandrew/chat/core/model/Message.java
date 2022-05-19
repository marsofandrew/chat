package org.marsofandrew.chat.core.model;

import java.time.Instant;

public record Message<M>(Instant timestamp, M message, String sender) {
}
