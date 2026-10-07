package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.ExcelImportResultDto;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelProductImportService {
    ExcelImportResultDto importFromExcel(MultipartFile file);
}