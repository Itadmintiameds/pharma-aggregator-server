package com.example.pharmaaggregatorserver.dto.product;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResultDto {

    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<RowErrorDto> errors;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowErrorDto {
        private int rowNumber;
        private String productName;
        private String errorMessage;
    }
}