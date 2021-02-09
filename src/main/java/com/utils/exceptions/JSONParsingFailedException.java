package com.utils.exceptions;

public class JSONParsingFailedException extends Exception{
    public JSONParsingFailedException(String errorMessage) {
        super(errorMessage);
    }
}
