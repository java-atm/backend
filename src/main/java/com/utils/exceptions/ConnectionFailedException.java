package com.utils.exceptions;

public class ConnectionFailedException extends Exception{
    public ConnectionFailedException(String errorMessage) {
        super(errorMessage);
    }
}
