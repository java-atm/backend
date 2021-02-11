package com.utils.exceptions.db_exceptions;

public abstract class BaseDatabaseClientException extends Exception{
    public BaseDatabaseClientException(String errorMessage) {
        super(errorMessage);
    }
}
