package com.example.pharmaaggregatorserver.exception;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {

    private final HttpStatus status;

    public BaseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

