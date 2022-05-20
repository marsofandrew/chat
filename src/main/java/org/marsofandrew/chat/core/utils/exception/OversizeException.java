package org.marsofandrew.chat.core.utils.exception;

public class OversizeException extends RuntimeException{
    public OversizeException(){
        super();
    }
    public OversizeException(int limit){
        super(String.format("limit is %s", limit));
    }
}
