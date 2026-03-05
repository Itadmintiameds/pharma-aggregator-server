package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.ExcelImportResultDto;
import com.example.pharmaaggregatorserver.service.product.ExcelProductImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ExcelProductImportController {

    private final ExcelProductImportService excelProductImportService;

    /**
     * Import products in bulk from the official Excel upload template.
     *
     * <pre>
     * POST /api/v1/products/import
     * Content-Type: multipart/form-data
     * Parameter   : file  (.xlsx)
     * </pre>
     *
     * Response contains a summary with per-row success / failure detail.
     */
    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ExcelImportResultDto> importProducts(
            @RequestParam("file") MultipartFile file) {

        ExcelImportResultDto result = excelProductImportService.importFromExcel(file);
        return ResponseEntity.ok(result);
    }
}