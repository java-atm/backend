package com.utils.exceptions.db_exceptions;

public class PinChangeFailedException extends BaseDatabaseClientException{
    public PinChangeFailedException(String errorMessage) {
        super(errorMessage);
    }
}
