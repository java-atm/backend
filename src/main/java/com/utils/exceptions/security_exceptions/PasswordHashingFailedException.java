package com.utils.exceptions.security_exceptions;

public class PasswordHashingFailedException extends Exception{
    public PasswordHashingFailedException(String errorMessage) {
        super(errorMessage);
    }
}
