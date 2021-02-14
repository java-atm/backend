package com.utils.exceptions.db_exceptions;

public class NoEnoughMoneyException extends BaseDatabaseClientException{
    public NoEnoughMoneyException(String errorMessage) {
        super(errorMessage);
    }
}
