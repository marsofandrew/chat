package org.marsofandrew.chat.core.exception;

public class ClientNotJoinedToChannelException extends RuntimeException {

    public ClientNotJoinedToChannelException(){
        super();
    }
    public ClientNotJoinedToChannelException(String message) {
        super(message);
    }
}
