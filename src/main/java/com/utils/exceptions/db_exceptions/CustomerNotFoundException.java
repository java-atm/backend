package com.utils.exceptions.db_exceptions;

public class CustomerNotFoundException extends BaseDatabaseClientException{
    public CustomerNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
