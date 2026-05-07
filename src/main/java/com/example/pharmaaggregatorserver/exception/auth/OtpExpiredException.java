package com.example.pharmaaggregatorserver.exception.auth;

public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException(String message) {
        super(message);
    }
}
