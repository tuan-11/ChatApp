package com.zileanstdio.chatapp.Exceptions;

public class PhoneNumberException extends Exception {
    public enum ErrorType {
        HAS_ALREADY_EXISTED
    }

    private ErrorType errorType;
    private String message;

    public PhoneNumberException(ErrorType errorType, String message) {
        super(message);
        this.message = message;
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return "Error type: " + errorType + ". " + message;
    }
}
