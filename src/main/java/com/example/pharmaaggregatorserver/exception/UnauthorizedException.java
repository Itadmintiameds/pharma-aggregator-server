package com.example.pharmaaggregatorserver.exception;

// UnauthorizedException.java
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}