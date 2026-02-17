package com.example.pharmaaggregatorserver.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "status",
        "message",
        "count",
        "data"
})
public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;
    private Long count;

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(String status, String message, T data, Long count) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Long getCount() {
        return count;
    }
}