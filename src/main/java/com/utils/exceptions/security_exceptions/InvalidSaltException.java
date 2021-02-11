package com.utils.exceptions.security_exceptions;

public class InvalidSaltException extends PasswordHashingFailedException{
    public InvalidSaltException(String errorMessage) {
        super(errorMessage);
    }
}
