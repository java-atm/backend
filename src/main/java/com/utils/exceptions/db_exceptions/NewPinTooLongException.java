package com.utils.exceptions.db_exceptions;

public class NewPinTooLongException extends BaseDatabaseClientException{
    public NewPinTooLongException(String errorMessage) {
        super(errorMessage);
    }
}
