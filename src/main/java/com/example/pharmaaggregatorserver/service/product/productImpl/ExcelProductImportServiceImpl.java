package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.ExcelImportResultDto;
import com.example.pharmaaggregatorserver.dto.product.ExcelImportResultDto.RowErrorDto;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import com.example.pharmaaggregatorserver.entity.product.PackagingDetailsDrug;
import com.example.pharmaaggregatorserver.entity.product.PricingDetailsDrug;
import com.example.pharmaaggregatorserver.entity.product.ProductDetailsDrug;
import com.example.pharmaaggregatorserver.repository.master.ProductTypeMasterRepository;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import com.example.pharmaaggregatorserver.repository.product.PackagingDetailsDrugRepository;
import com.example.pharmaaggregatorserver.repository.product.PricingDetailsDrugRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductDetailsDrugRepository;
import com.example.pharmaaggregatorserver.service.product.ExcelProductImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Parses the "Drugs" sheet of the product upload template.
 * <p>
 * Column layout (0-indexed, matching template v0.1.1):
 * 0  Therapeutic Category
 * 1  Therapeutic Sub Category
 * 2  Product Name              ← row key / mandatory
 * 3  Molecule (comma-separated names)
 * 4  Mechanism of Action (comma-separated, informational – stored on Molecule)
 * 5  Primary Use
 * 6  Dosage Form
 * 7  Strength / Net Quantity
 * 8  Warnings / Precautions
 * 9  Product Description
 * 10  Product Image URL
 * 11  Product Marketing URL
 * 12  Packaging Unit
 * 13  Number of Units
 * 14  Pack Size (auto – ignored on import)
 * 15  Minimum Order Qty
 * 16  Max Order Qty
 * 17  Batch / Lot Number
 * 18  Manufacturing Date
 * 19  Expiry Date
 * 20  Storage Condition
 * 21  Stock Quantity
 * 22  Date of Entry (informational – ignored)
 * 23  Price per Unit
 * 24  MRP (INR)
 * 25  Discount %
 * 26  Additional Discount – Minimum Purchase Quantity (sub-header row 1)
 * 27  Additional Discount – Discount Percentage    (sub-header row 1)
 * 28  Final Price (auto – ignored on import)
 * 29  GST %
 * 30  HSN Code
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelProductImportServiceImpl implements ExcelProductImportService {

    // ── Supported file types ──────────────────────────────────────────────
    private enum FileType {XLSX, XLS, CSV}

    // ── Column indices ────────────────────────────────────────────────────
    private static final int COL_THERAPEUTIC_CAT = 0;
    private static final int COL_THERAPEUTIC_SUBCAT = 1;
    private static final int COL_PRODUCT_NAME = 2;
    private static final int COL_MOLECULES = 3;
    private static final int COL_DOSAGE_FORM = 6;
    private static final int COL_STRENGTH = 7;
    private static final int COL_WARNINGS = 8;
    private static final int COL_DESCRIPTION = 9;
    private static final int COL_IMAGE_URL = 10;
    private static final int COL_MARKETING_URL = 11;
    private static final int COL_PKG_UNIT = 12;
    private static final int COL_PKG_UNITS_COUNT = 13;
    private static final int COL_PKG_MIN_ORDER = 15;
    private static final int COL_PKG_MAX_ORDER = 16;
    private static final int COL_BATCH_NUMBER = 17;
    private static final int COL_MFG_DATE = 18;
    private static final int COL_EXPIRY_DATE = 19;
    private static final int COL_STORAGE = 20;
    private static final int COL_STOCK_QTY = 21;
    private static final int COL_PRICE_PER_UNIT = 23;
    private static final int COL_MRP = 24;
    private static final int COL_DISCOUNT_PCT = 25;
    private static final int COL_MIN_PURCHASE_QTY = 26;
    private static final int COL_ADD_DISCOUNT_PCT = 27;
    private static final int COL_FINAL_PRICE = 28;
    private static final int COL_GST_PCT = 29;
    private static final int COL_HSN_CODE = 30;

    private static final int DATA_START_ROW = 2; // rows 0 & 1 are headers

    // ── Repositories ──────────────────────────────────────────────────────
    private final ProductDetailsDrugRepository productRepository;
    private final MoleculeRepository moleculeRepository;
    private final PackagingDetailsDrugRepository packagingRepository;
    private final PricingDetailsDrugRepository pricingRepository;
    private final ProductTypeMasterRepository productTypeMasterRepository;

    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ExcelImportResultDto importFromExcel(MultipartFile file) {

        FileType fileType = validateFile(file);  // capture the return value

        if (fileType == FileType.CSV) {
            return importFromCsv(file);
        }

        List<RowErrorDto> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = fileType == FileType.XLSX
                     ? new XSSFWorkbook(is)
                     : new HSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheet("Drugs");
            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Sheet 'Drugs' not found. Please use the official upload template.");
            }

            for (int rowIdx = DATA_START_ROW; rowIdx <= sheet.getLastRowNum(); rowIdx++) {

                Row row = sheet.getRow(rowIdx);
                if (row == null || isRowEmpty(row)) continue;

                totalRows++;
                String productName = getString(row, COL_PRODUCT_NAME);

                try {
                    importRow(row, rowIdx + 1); // +1 for human-readable row number
                    successCount++;
                    log.info("Row {} – product '{}' imported successfully.", rowIdx + 1, productName);

                } catch (Exception ex) {
                    log.warn("Row {} – import failed for '{}': {}", rowIdx + 1, productName, ex.getMessage());
                    errors.add(RowErrorDto.builder()
                            .rowNumber(rowIdx + 1)
                            .productName(productName)
                            .errorMessage(ex.getMessage())
                            .build());
                }
            }

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse Excel file: " + ex.getMessage(), ex);
        }

        return ExcelImportResultDto.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failureCount(errors.size())
                .errors(errors)
                .build();
    }

    // ── Per-row import logic ──────────────────────────────────────────────

    private void importRow(Row row, int humanRowNum) {

        String productName = getString(row, COL_PRODUCT_NAME);
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product Name is mandatory.");
        }

        // ── 1. Always create a new ProductDetailsDrug ─────────────────────
        // Duplicate product names are intentional (multiple sellers).
        // Seller mapping will be handled separately.
        ProductDetailsDrug product = buildNewProduct(row, productName);
        product.setProductId(generateProductId(productName));

        // ── Resolve productCategoryId from master table ───────────────────
        ProductTypeMaster productType = productTypeMasterRepository
                .findByProductTypeNameIgnoreCase("Drugs")
                .orElseThrow(() -> new IllegalStateException(
                        "Product type 'Drugs' not found in master table."));
        product.setProductCategoryId(String.valueOf(productType.getProductTypeId()));

        // ── 2. Molecules (MANY-TO-MANY) ───────────────────────────────────
        String moleculeCell = getString(row, COL_MOLECULES);
        if (moleculeCell != null && !moleculeCell.isBlank()) {
            Set<Molecule> molecules = resolveMolecules(moleculeCell);
//            product.setMolecules(molecules);
        }

        // ── 3. Packaging (ONE-TO-ONE) ─────────────────────────────────────
        String packagingUnit = getString(row, COL_PKG_UNIT);
        if (packagingUnit != null && !packagingUnit.isBlank()) {
            PackagingDetailsDrug packaging = buildPackaging(row);
            packaging.setPackagingId(generatePackagingId());
            packaging.setProduct(product);
            product.setPackagingDetails(packaging);
        }

        // Save product (and cascade packaging) before adding pricing
        ProductDetailsDrug savedProduct = productRepository.save(product);

        // ── 4. Pricing / Batch (ONE-TO-MANY) ─────────────────────────────
        String batchNumber = getString(row, COL_BATCH_NUMBER);
        if (batchNumber != null && !batchNumber.isBlank()) {
            PricingDetailsDrug pricing = buildPricing(row);
            pricing.setPricingId(generatePricingId());
            pricing.setProduct(savedProduct);
            pricingRepository.save(pricing);
        }
    }

    // ── Builder helpers ───────────────────────────────────────────────────

    private ProductDetailsDrug buildNewProduct(Row row, String productName) {
        ProductDetailsDrug p = new ProductDetailsDrug();
        p.setProductName(productName);
        p.setTherapeuticCategory(getString(row, COL_THERAPEUTIC_CAT));
        p.setTherapeuticSubcategory(getString(row, COL_THERAPEUTIC_SUBCAT));
        p.setDosageForm(getString(row, COL_DOSAGE_FORM));
        p.setWarningsPrecautions(getString(row, COL_WARNINGS));
        p.setProductDescription(getString(row, COL_DESCRIPTION));
        p.setProductImage(getString(row, COL_IMAGE_URL));
        p.setProductMarketingUrl(getString(row, COL_MARKETING_URL));
        Long strength = getLong(row, COL_STRENGTH);
        if (strength != null) p.setStrength(strength);
        return p;
    }

    private PackagingDetailsDrug buildPackaging(Row row) {
        PackagingDetailsDrug pkg = new PackagingDetailsDrug();
        pkg.setPackagingUnit(getString(row, COL_PKG_UNIT));
        pkg.setNumberOfUnits(getLong(row, COL_PKG_UNITS_COUNT));
        pkg.setMinimumOrderQuantity(getLong(row, COL_PKG_MIN_ORDER));
        pkg.setMaximumOrderQuantity(getLong(row, COL_PKG_MAX_ORDER));
        // Pack size = numberOfUnits (template says "Auto")
        Long units = getLong(row, COL_PKG_UNITS_COUNT);
        if (units != null) pkg.setPackSize(units);
        pkg.setCreatedDate(LocalDateTime.now());
        pkg.setModifiedDate(LocalDateTime.now());
        return pkg;
    }

    private PricingDetailsDrug buildPricing(Row row) {
        PricingDetailsDrug p = new PricingDetailsDrug();
        p.setBatchLotNumber(getString(row, COL_BATCH_NUMBER));
        p.setManufacturingDate(getDateTime(row, COL_MFG_DATE));
        p.setExpiryDate(getDateTime(row, COL_EXPIRY_DATE));
        p.setStorageCondition(getString(row, COL_STORAGE));
        p.setStockQuantity(getLong(row, COL_STOCK_QTY));
        p.setPricePerUnit(getLong(row, COL_PRICE_PER_UNIT));
        p.setMrp(getLong(row, COL_MRP));
        p.setDiscountPercentage(getLong(row, COL_DISCOUNT_PCT));
        p.setMinimumPurchaseQuantity(getLong(row, COL_MIN_PURCHASE_QTY));
        p.setFinalPrice(getLong(row, COL_FINAL_PRICE));
        p.setGstPercentage(getLong(row, COL_GST_PCT));
        p.setHsnCode(getLong(row, COL_HSN_CODE));
        p.setCreatedDate(LocalDateTime.now());
        p.setModifiedDate(LocalDateTime.now());
        return p;
    }

    // ── Molecule resolution ───────────────────────────────────────────────

    /**
     * Accepts molecule names (comma-separated).
     * Looks up by name (case-insensitive); creates a new Molecule if not found.
     */
    private Set<Molecule> resolveMolecules(String raw) {
        String[] names = raw.split(",");
        Set<Molecule> result = new LinkedHashSet<>();
        for (String name : names) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) continue;
            Molecule molecule = moleculeRepository
                    .findByMoleculeNameIgnoreCase(trimmed)
                    .orElseGet(() -> {
                        Molecule m = new Molecule();
                        m.setMoleculeName(trimmed);
                        return moleculeRepository.save(m);
                    });
            result.add(molecule);
        }
        return result;
    }

    // ── ID generators (mirrors ProductDetailsDrugServiceImpl) ─────────────

    private synchronized String generateProductId(String productName) {
        String prefix = "SN";
        String namePart = productName.replaceAll("[^a-zA-Z]", "").toUpperCase();
        namePart = namePart.length() >= 3
                ? namePart.substring(0, 3)
                : String.format("%-3s", namePart).replace(' ', 'X');
        Integer last = productRepository.findMaxProductNumber();
        int next = (last == null) ? 1 : last + 1;
        return prefix + namePart + String.format("%05d", next);
    }

    private synchronized String generatePackagingId() {
        Integer last = packagingRepository.findMaxPackagingNumber();
        int next = (last == null) ? 1 : last + 1;
        return "SNPKG" + String.format("%05d", next);
    }

    private synchronized String generatePricingId() {
        Integer last = pricingRepository.findMaxPricingNumber();
        int next = (last == null) ? 1 : last + 1;
        return "SNBTCH" + String.format("%05d", next);
    }

    // ── Cell readers ──────────────────────────────────────────────────────

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private Long getLong(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case STRING:
                    String s = cell.getStringCellValue().trim();
                    return s.isEmpty() ? null : Long.parseLong(s.replaceAll("[^0-9]", ""));
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime getDateTime(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                if (!s.isEmpty()) {
                    // Support YYYY-MM-DD or DD/MM/YYYY
                    if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        return java.time.LocalDate.parse(s).atStartOfDay();
                    } else if (s.matches("\\d{2}/\\d{2}/\\d{4}")) {
                        String[] p = s.split("/");
                        return java.time.LocalDate.of(
                                Integer.parseInt(p[2]),
                                Integer.parseInt(p[1]),
                                Integer.parseInt(p[0])
                        ).atStartOfDay();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // ── Utility ───────────────────────────────────────────────────────────

    /**
     * Validates the uploaded file and returns its detected {@link FileType}.
     *
     * @throws IllegalArgumentException if the file is empty or has an unsupported extension
     */
    private FileType validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }
        String name = Objects.requireNonNull(file.getOriginalFilename(), "Filename missing")
                .toLowerCase();
        if (name.endsWith(".xlsx")) return FileType.XLSX;
        if (name.endsWith(".xls")) return FileType.XLS;
        if (name.endsWith(".csv")) return FileType.CSV;
        throw new IllegalArgumentException(
                "Unsupported file type. Only .xlsx, .xls, and .csv files are accepted.");
    }

    private boolean isRowEmpty(Row row) {
        // Cols 14 (Pack Size) and 28 (Final Price) contain pre-filled IF formulas
        // in all 500 template rows — skip them when checking emptiness.
        // Only check the mandatory Product Name column (col 2) as the real signal.
        Cell nameCell = row.getCell(COL_PRODUCT_NAME, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (nameCell == null) return true;
        String name = getString(row, COL_PRODUCT_NAME);
        return name == null || name.isBlank();
    }

    private ExcelImportResultDto importFromCsv(MultipartFile file) {
        List<RowErrorDto> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.RFC4180.builder()
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            int csvRowIndex = 0;
            for (CSVRecord record : parser) {
                if (csvRowIndex++ < DATA_START_ROW) continue;

                String productName = getCsvValue(record, COL_PRODUCT_NAME);
                if (productName == null || productName.isBlank()) continue;

                int humanRowNum = csvRowIndex;
                totalRows++;
                try {
                    importCsvRow(record, humanRowNum);
                    successCount++;
                    log.info("Row {} – product '{}' imported successfully.", humanRowNum, productName);
                } catch (Exception ex) {
                    log.warn("Row {} – import failed for '{}': {}", humanRowNum, productName, ex.getMessage());
                    errors.add(RowErrorDto.builder()
                            .rowNumber(humanRowNum)
                            .productName(productName)
                            .errorMessage(ex.getMessage())
                            .build());
                }
            }

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse CSV file: " + ex.getMessage(), ex);
        }

        return ExcelImportResultDto.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failureCount(errors.size())
                .errors(errors)
                .build();
    }

    private void importCsvRow(CSVRecord r, int humanRowNum) {
        String productName = getCsvValue(r, COL_PRODUCT_NAME);
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product Name is mandatory.");
        }

        ProductDetailsDrug product = new ProductDetailsDrug();
        product.setProductName(productName);
        product.setTherapeuticCategory(getCsvValue(r, COL_THERAPEUTIC_CAT));
        product.setTherapeuticSubcategory(getCsvValue(r, COL_THERAPEUTIC_SUBCAT));
        product.setDosageForm(getCsvValue(r, COL_DOSAGE_FORM));
        product.setWarningsPrecautions(getCsvValue(r, COL_WARNINGS));
        product.setProductDescription(getCsvValue(r, COL_DESCRIPTION));
        product.setProductImage(getCsvValue(r, COL_IMAGE_URL));
        product.setProductMarketingUrl(getCsvValue(r, COL_MARKETING_URL));
        Long strength = csvLong(r, COL_STRENGTH);
        if (strength != null) product.setStrength(strength);
        product.setProductId(generateProductId(productName));

        ProductTypeMaster productType = productTypeMasterRepository
                .findByProductTypeNameIgnoreCase("Drugs")
                .orElseThrow(() -> new IllegalStateException(
                        "Product type 'Drugs' not found in master table."));
        product.setProductCategoryId(String.valueOf(productType.getProductTypeId()));

        String moleculeCell = getCsvValue(r, COL_MOLECULES);
        if (moleculeCell != null && !moleculeCell.isBlank()) {
//            product.setMolecules(resolveMolecules(moleculeCell));
        }

        String packagingUnit = getCsvValue(r, COL_PKG_UNIT);
        if (packagingUnit != null && !packagingUnit.isBlank()) {
            PackagingDetailsDrug pkg = new PackagingDetailsDrug();
            pkg.setPackagingUnit(packagingUnit);
            Long units = csvLong(r, COL_PKG_UNITS_COUNT);
            pkg.setNumberOfUnits(units);
            if (units != null) pkg.setPackSize(units);
            pkg.setMinimumOrderQuantity(csvLong(r, COL_PKG_MIN_ORDER));
            pkg.setMaximumOrderQuantity(csvLong(r, COL_PKG_MAX_ORDER));
            pkg.setCreatedDate(LocalDateTime.now());
            pkg.setModifiedDate(LocalDateTime.now());
            pkg.setPackagingId(generatePackagingId());
            pkg.setProduct(product);
            product.setPackagingDetails(pkg);
        }

        ProductDetailsDrug saved = productRepository.save(product);

        String batchNumber = getCsvValue(r, COL_BATCH_NUMBER);
        if (batchNumber != null && !batchNumber.isBlank()) {
            PricingDetailsDrug pricing = new PricingDetailsDrug();
            pricing.setBatchLotNumber(batchNumber);
            pricing.setManufacturingDate(csvDateTime(getCsvValue(r, COL_MFG_DATE)));
            pricing.setExpiryDate(csvDateTime(getCsvValue(r, COL_EXPIRY_DATE)));
            pricing.setStorageCondition(getCsvValue(r, COL_STORAGE));
            pricing.setStockQuantity(csvLong(r, COL_STOCK_QTY));
            pricing.setPricePerUnit(csvLong(r, COL_PRICE_PER_UNIT));
            pricing.setMrp(csvLong(r, COL_MRP));
            pricing.setDiscountPercentage(csvLong(r, COL_DISCOUNT_PCT));
            pricing.setMinimumPurchaseQuantity(csvLong(r, COL_MIN_PURCHASE_QTY));
            pricing.setFinalPrice(csvLong(r, COL_FINAL_PRICE));
            pricing.setGstPercentage(csvLong(r, COL_GST_PCT));
            pricing.setHsnCode(csvLong(r, COL_HSN_CODE));
            pricing.setCreatedDate(LocalDateTime.now());
            pricing.setModifiedDate(LocalDateTime.now());
            pricing.setPricingId(generatePricingId());
            pricing.setProduct(saved);
            pricingRepository.save(pricing);
        }
    }

    private String getCsvValue(CSVRecord record, int col) {
        if (col >= record.size()) return null;
        String v = record.get(col).trim();
        return v.isEmpty() ? null : v;
    }

    private Long csvLong(CSVRecord record, int col) {
        String v = getCsvValue(record, col);
        if (v == null) return null;
        try {
            return Long.parseLong(v.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime csvDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            if (s.matches("\\d{4}-\\d{2}-\\d{2}"))
                return java.time.LocalDate.parse(s).atStartOfDay();
            if (s.matches("\\d{2}/\\d{2}/\\d{4}")) {
                String[] p = s.split("/");
                return java.time.LocalDate.of(
                        Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0])
                ).atStartOfDay();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}