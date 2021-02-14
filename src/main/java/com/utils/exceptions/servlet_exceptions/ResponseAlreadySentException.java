package com.utils.exceptions.servlet_exceptions;

public class ResponseAlreadySentException extends Exception{
    public ResponseAlreadySentException(String errorMessage) {
        super(errorMessage);
    }
}
