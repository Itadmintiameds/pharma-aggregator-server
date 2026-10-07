package com.example.pharmaaggregatorserver.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalLogInExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLocked(AccountLockedException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleAccountInactive(AccountInactiveException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "An unexpected error occurred");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
