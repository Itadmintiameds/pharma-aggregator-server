package com.example.pharmaaggregatorserver.response;


import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // apply to all controllers
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // Avoid wrapping if already wrapped. Two different ApiResponse classes are
        // in use across the codebase (this one, and dto.seller.SellerLogIn.ApiResponse
        // used by GlobalExceptionHandler for error bodies) - check both, otherwise
        // error bodies get silently re-wrapped into a fake "SUCCESS" envelope here.
        if (body instanceof ApiResponse
                || body instanceof com.example.pharmaaggregatorserver.dto.seller.SellerLogIn.ApiResponse) {
            return body;
        }
        Long count = (body instanceof java.util.Collection<?> collection)
                ? (long) collection.size()
                : 0;
        // Wrap normal response
        return new ApiResponse<>(
                "SUCCESS",
                "Request processed successfully",
                body,
                count
        );
    }
}



