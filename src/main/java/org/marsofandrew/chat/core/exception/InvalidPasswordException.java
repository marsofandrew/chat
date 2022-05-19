package org.marsofandrew.chat.core.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(){
        super();
    }

    public InvalidPasswordException(String message){
        super(message);
    }
}
