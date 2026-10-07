package com.example.pharmaaggregatorserver.exception.auth;

public class OtpLockedException extends RuntimeException {
    public OtpLockedException(String message) {
        super(message);
    }
}
