package com.utils.exceptions.readers_exceptions;

public class RequestDataReadingFailedException extends RuntimeException{
    public RequestDataReadingFailedException(String errorMessage) {
        super(errorMessage);
    }
}
