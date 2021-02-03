package com.utils.exceptions;

public class NewPinTooLongException extends Exception{
    public NewPinTooLongException(String errorMessage) {
        super(errorMessage);
    }
}
