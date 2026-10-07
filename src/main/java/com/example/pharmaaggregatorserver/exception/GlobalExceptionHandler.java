package com.example.pharmaaggregatorserver.exception;

import org.springframework.web.server.ResponseStatusException;
import com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.ApiResponse;
import com.example.pharmaaggregatorserver.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Custom business exceptions
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getStatus().value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(response, ex.getStatus());
    }

    // Single handler for MethodArgumentNotValidException - KEEP ONLY ONE
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Option 1: Return detailed field errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("ERROR", "Validation failed", errors));
    }

    // Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>("ERROR", ex.getMessage(), null));
    }

    // Bad Request
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("ERROR", ex.getMessage(), null));
    }

    // Unauthorized
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("ERROR", ex.getMessage(), null));
    }

    // Single handler for generic exceptions - KEEP ONLY ONE
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        // Log the error for debugging
        log.error("Unexpected error occurred: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("ERROR", "An unexpected error occurred: " + ex.getMessage(), null));
    }

//    @ExceptionHandler(DuplicateRequestException.class)
//    @ResponseStatus(HttpStatus.OK)  // Return 200 OK instead of 500
//    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDuplicateRequestException(DuplicateRequestException ex) {
//        log.error("Duplicate request error: {}", ex.getMessage());
//
//        // Create the inner error data
//        Map<String, Object> errorData = new HashMap<>();
//        errorData.put("status", "ERROR");
//        errorData.put("message", ex.getMessage());
//        errorData.put("data", null);
//
//        // Create the outer response
//        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
//                "SUCCESS",
//                "Request processed successfully",
//                errorData
//        );
//
//        return ResponseEntity.ok(response);
//    }



    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatusException(ResponseStatusException ex) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ApiResponse<>(
                        "ERROR",
                        ex.getReason(),
                        null
                ));
    }



}