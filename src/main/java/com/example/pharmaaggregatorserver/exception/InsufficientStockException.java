package com.example.pharmaaggregatorserver.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException {

    public InsufficientStockException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
