package com.zileanstdio.chatapp.Exceptions;

import androidx.annotation.NonNull;

public class UserException extends Exception {
    public enum ErrorType {
        UNKNOWN_USER,
        UNKNOWN_TOKEN
    }

    private final ErrorType errorType;
    private final String message;

    public UserException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.message = message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @NonNull
    @Override
    public String toString() {
        return "Error Type: " + errorType + ". " + message;
    }
}