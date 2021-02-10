package com.utils.exceptions.readers_exceptions;

public class JSONParsingFailedException extends Exception{
    public JSONParsingFailedException(String errorMessage) {
        super(errorMessage);
    }
}
