package com.example.pharmaaggregatorserver.exception;

import com.example.pharmaaggregatorserver.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {

        ErrorResponse response = new ErrorResponse(
                ex.getStatus().value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception ex) {

        ErrorResponse response = new ErrorResponse(
                500,
                "Internal server error"
        );

        return ResponseEntity.status(500).body(response);
    }
}