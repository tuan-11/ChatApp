package com.zileanstdio.chatapp.Exceptions;

public class VerificationException extends Exception{
    public enum ErrorType {
        INVALID_CODE
    }

    private ErrorType errorType;
    private String message;

    public VerificationException(ErrorType errorType, String message) {
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
