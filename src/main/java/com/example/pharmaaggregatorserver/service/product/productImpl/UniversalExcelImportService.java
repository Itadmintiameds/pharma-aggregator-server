package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.ExcelImportResultDto;
import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import com.example.pharmaaggregatorserver.entity.product.Category;
import com.example.pharmaaggregatorserver.repository.product.CategoryRepository;
import com.example.pharmaaggregatorserver.service.product.ProductDetailsService;
import com.example.pharmaaggregatorserver.service.product.util.ProductImportStrategy;
import com.example.pharmaaggregatorserver.service.product.util.ProductImportStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UniversalExcelImportService {

    private final ProductImportStrategyFactory strategyFactory;
    private final ProductDetailsService productService;
    private final CategoryRepository categoryRepository;

    public ExcelImportResultDto importFile(MultipartFile file, Long userId) {
        String name = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        return name.endsWith(".csv") ? importCsv(file, userId) : importExcel(file, userId);
    }

    private ExcelImportResultDto importExcel(MultipartFile file, Long userId) {

        List<ExcelImportResultDto.RowErrorDto> errors = new ArrayList<>();
        int success = 0, total = 0;

        try (Workbook wb = getWorkbook(file)) {

            for (int s = 0; s < wb.getNumberOfSheets(); s++) {

                Sheet sheet = wb.getSheetAt(s);
                String sheetName = sheet.getSheetName();

                // Skip sheets that have no registered strategy (e.g. "Master", "Masters")
                ProductImportStrategy strategy;
                try {
                    strategy = strategyFactory.getStrategy(sheetName);
                } catch (RuntimeException ex) {
                    log.info("Skipping sheet '{}': {}", sheetName, ex.getMessage());
                    continue;
                }

                Category category = categoryRepository
                        .findByCategoryNameIgnoreCase(sheetName)
                        .orElseThrow(() -> new RuntimeException("Category not found: " + sheetName));

                // Row 0 = main header, Row 1 = sub-header → data starts at row 2
                for (int i = 2; i <= sheet.getLastRowNum(); i++) {

                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String productName = getString(row, 2);
                    if (productName == null || productName.isBlank()) continue;

                    total++;

                    try {
                        ProductDetailsDto dto = strategy.mapRow(row);
                        dto.setCategoryId(Long.valueOf(category.getCategoryId()));
                        productService.createProduct(dto, userId);
                        success++;

                    } catch (Exception ex) {
                        log.error("Row {} failed [{}]: {}", i + 1, productName, ex.getMessage());
                        errors.add(ExcelImportResultDto.RowErrorDto.builder()
                                .rowNumber(i + 1)
                                .productName(productName)
                                .errorMessage(ex.getMessage())
                                .build());
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Excel processing failed: " + e.getMessage(), e);
        }

        return buildResult(total, success, errors);
    }

    private ExcelImportResultDto importCsv(MultipartFile file, Long userId) {

        List<ExcelImportResultDto.RowErrorDto> errors = new ArrayList<>();
        int success = 0, total = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()   // consumes row 0 (main header)
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .withAllowMissingColumnNames() // row 0 has empty cols (nan) — don't throw
                     .parse(reader)) {

            // Category is fixed to DRUGS for this template.
            // Extend here when new category templates are introduced.
            String categoryName = "DRUGS";

            ProductImportStrategy strategy = strategyFactory.getStrategy(categoryName);
            Category category = categoryRepository
                    .findByCategoryNameIgnoreCase(categoryName)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

            for (CSVRecord record : parser) {

                // Row 1 (sub-header) becomes first record — blank product name skips it.
                // Fully blank rows in the template body are also skipped here.
                String productName = getCsvString(record, "Product Name*");
                if (productName == null || productName.isBlank()) continue;

                total++;
                try {
                    ProductDetailsDto dto = strategy.mapCsv(record);
                    dto.setCategoryId(Long.valueOf(category.getCategoryId()));
                    productService.createProduct(dto, userId);
                    success++;
                } catch (Exception ex) {
                    log.error("CSV row {} failed [{}]: {}", record.getRecordNumber(), productName, ex.getMessage());
                    errors.add(ExcelImportResultDto.RowErrorDto.builder()
                            .rowNumber((int) record.getRecordNumber())
                            .productName(productName)
                            .errorMessage(ex.getMessage())
                            .build());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("CSV processing failed: " + e.getMessage(), e);
        }

        return buildResult(total, success, errors);
    }

    // Add this small helper used above — keeps the service self-contained
    private String getCsvString(CSVRecord record, String header) {
        try {
            String v = record.get(header);
            return (v != null && !v.isBlank()) ? v.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Workbook getWorkbook(MultipartFile file) throws Exception {
        String name = file.getOriginalFilename().toLowerCase();
        if (name.endsWith(".xlsx")) return new XSSFWorkbook(file.getInputStream());
        if (name.endsWith(".xls")) return new HSSFWorkbook(file.getInputStream());
        throw new RuntimeException("Unsupported file type — use .xlsx, .xls, or .csv");
    }

    private ExcelImportResultDto buildResult(int total, int success,
                                             List<ExcelImportResultDto.RowErrorDto> errors) {
        return ExcelImportResultDto.builder()
                .totalRows(total)
                .successCount(success)
                .failureCount(errors.size())
                .errors(errors)
                .build();
    }

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell != null ? cell.toString().trim() : null;
    }
}