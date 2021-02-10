package com.utils.exceptions.db_exceptions;

public class AccountNotFoundException extends BaseDatabaseClientException{
    public AccountNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
