package com.example.pharmaaggregatorserver.exception;

import org.springframework.http.HttpStatus;

public class ApplicationException extends BaseException{

    public ApplicationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
    public ApplicationException(HttpStatus status, String message) {
        super(status, message);
    }
}