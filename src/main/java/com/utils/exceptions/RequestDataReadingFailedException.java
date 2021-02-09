package com.utils.exceptions;

public class RequestDataReadingFailedException extends RuntimeException{
    public RequestDataReadingFailedException(String errorMessage) {
        super(errorMessage);
    }
}
