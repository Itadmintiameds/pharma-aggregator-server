package com.example.pharmaaggregatorserver.exception.auth;

public class RefreshTokenException extends RuntimeException {
    public RefreshTokenException(String message) {
        super(message);
    }
}

