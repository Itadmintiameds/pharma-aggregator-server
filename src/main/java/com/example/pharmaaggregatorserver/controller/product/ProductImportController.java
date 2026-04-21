package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.ExcelImportResultDto;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.product.productImpl.UniversalExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products/import")
@RequiredArgsConstructor
public class ProductImportController {

    private final UniversalExcelImportService importService;

    @PostMapping
    public ResponseEntity<ExcelImportResultDto> importProducts(
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") Long categoryId,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        ExcelImportResultDto result =
                importService.importFile(file, user.getId(), categoryId);

        return ResponseEntity.ok(result);
    }
}