package com.utils.exceptions.db_exceptions;

public class ConnectionFailedException extends BaseDatabaseClientException{
    public ConnectionFailedException(String errorMessage) {
        super(errorMessage);
    }
}
