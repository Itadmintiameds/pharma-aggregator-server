package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import com.example.pharmaaggregatorserver.exception.ValidationException;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import com.example.pharmaaggregatorserver.repository.product.PackTypeRepository;
import com.example.pharmaaggregatorserver.repository.product.TherapeuticCategoryRepository;
import com.example.pharmaaggregatorserver.repository.product.TherapeuticSubcategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component("DRUGS")
@RequiredArgsConstructor
public class DrugImportStrategy implements ProductImportStrategy {

    private final MoleculeRepository moleculeRepository;
    private final PackTypeRepository packTypeRepository;
    private final TherapeuticCategoryRepository therapeuticCategoryRepository;
    private final TherapeuticSubcategoryRepository therapeuticSubcategoryRepository;

    // ── Column indices (0-based, data starts at row index 2) ──────────────
    private static final int COL_THERAPEUTIC_CAT = 0;
    private static final int COL_THERAPEUTIC_SUBCAT = 1;
    private static final int COL_PRODUCT_NAME = 2;
    private static final int COL_MOLECULES = 3;
    private static final int COL_DOSAGE_FORM = 4;
    private static final int COL_STRENGTH = 5;
    private static final int COL_WARNINGS = 6;
    private static final int COL_DESCRIPTION = 7;
    private static final int COL_MANUFACTURER = 8;
    private static final int COL_PACK_TYPE = 9;
    private static final int COL_UNIT_PER_PACK = 10;
    private static final int COL_NUMBER_OF_PACKS = 11;
    // col 12 = Pack Size (auto-calculated) — not read from Excel
    private static final int COL_MIN_ORDER_QTY = 13;
    private static final int COL_MAX_ORDER_QTY = 14;
    private static final int COL_BATCH_NUMBER = 15;
    private static final int COL_MFG_DATE = 16;
    private static final int COL_EXPIRY_DATE = 17;
    private static final int COL_STORAGE_CONDITION = 18;
    private static final int COL_STOCK_QTY = 19;
    private static final int COL_DATE_OF_ENTRY = 20;
    private static final int COL_MRP = 21;
    private static final int COL_SELLING_PRICE = 22;
    private static final int COL_DISCOUNT_PCT = 23;
    private static final int COL_GST_PCT = 24;
    private static final int COL_HSN_CODE = 25;
    private static final int COL_SHELF_LIFE_MONTHS = 26;
    // Additional discount slabs — 4 slabs × 7 cols each, starting at col 27
    private static final int COL_ADD_DISCOUNT_START = 27;
    private static final int ADD_DISCOUNT_SLAB_SIZE = 7;
    private static final int ADD_DISCOUNT_SLAB_COUNT = 4;
    private static final int COL_MARKETING_URL = 56;

    // ── CSV header names (match Excel row 0 exactly) ──────────────────────
    private static final String H_THERAPEUTIC_CAT = "Therapeutic Category*";
    private static final String H_THERAPEUTIC_SUBCAT = "Therapeutic Sub Category*";
    private static final String H_PRODUCT_NAME = "Product Name*";
    private static final String H_MOLECULES = "Molecule* (comma separated)";
    private static final String H_DOSAGE_FORM = "Dosage Form*";
    //    private static final String H_STRENGTH = "Strength*";
    private static final String H_WARNINGS = "Warnings / Precautions*";
    private static final String H_DESCRIPTION = "Product Description*";
    private static final String H_MANUFACTURER = "Manufacture Name";
    private static final String H_PACK_TYPE = "Pack Type";
    private static final String H_UNIT_PER_PACK = "Unit Per Pack";
    private static final String H_NUMBER_OF_PACKS = "Number Of Packs";
    private static final String H_MIN_ORDER_QTY = "Minimum Order Qty*";
    private static final String H_MAX_ORDER_QTY = "Max Order Qty*";
    private static final String H_BATCH_NUMBER = "Batch / Lot Number*";
    private static final String H_MFG_DATE = "Manufacturing Date*";
    private static final String H_EXPIRY_DATE = "Expiry Date*";
    private static final String H_STORAGE_CONDITION = "Storage Condition*";
    private static final String H_STOCK_QTY = "Stock Quantity*";
    private static final String H_DATE_OF_ENTRY = "Date of Entry*";
    private static final String H_MRP = "MRP (INR)*";
    private static final String H_SELLING_PRICE = "Selling Price(INR)*";
    private static final String H_DISCOUNT_PCT = "Discount %";
    private static final String H_GST_PCT = "GST %";
    private static final String H_HSN_CODE = "HSN Code*";
    private static final String H_SHELF_LIFE_MONTHS = "Shelf Life Months";
    private static final String H_MARKETING_URL = "Product Marketing URL";

    // Additional discount cols accessed by index — duplicate header names in CSV
    // make name-based access unreliable, so index is used for all 4 slabs
    private static final int[] CSV_SLAB_MIN_QTY_COLS = {28, 35, 42, 49};
    private static final int[] CSV_SLAB_DISCOUNT_COLS = {29, 36, 43, 50};
    private static final int[] CSV_SLAB_START_DATE_COLS = {30, 37, 44, 51};
    private static final int[] CSV_SLAB_START_TIME_COLS = {31, 38, 45, 52};
    private static final int[] CSV_SLAB_END_DATE_COLS = {32, 39, 46, 53};
    private static final int[] CSV_SLAB_END_TIME_COLS = {33, 40, 47, 54};

    @Override
    public ProductDetailsDto mapRow(Row row) {

        validateMandatoryExcel(row);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getString(row, COL_PRODUCT_NAME));
        dto.setWarningsPrecautions(getString(row, COL_WARNINGS));
        dto.setProductDescription(getString(row, COL_DESCRIPTION));
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));
        dto.setProductMarketingUrl(getString(row, COL_MARKETING_URL));

        Long unitPerPack = getNullSafeLong(row, COL_UNIT_PER_PACK);
        Long numberOfPacks = getNullSafeLong(row, COL_NUMBER_OF_PACKS);
        String packTypeName = getString(row, COL_PACK_TYPE);
        String dosageFormName = getString(row, COL_DOSAGE_FORM);

        dto.setPackagingDetails(buildPackaging(
                unitPerPack, numberOfPacks,
                getNullSafeLong(row, COL_MIN_ORDER_QTY),
                getNullSafeLong(row, COL_MAX_ORDER_QTY),
                packTypeName, dosageFormName));

        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();
        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            int base = COL_ADD_DISCOUNT_START + (slab * ADD_DISCOUNT_SLAB_SIZE);
            Long minQty = getNullSafeLong(row, base + 1);
            Long discountPct = getNullSafeLong(row, base + 2);
            if ((minQty == null || minQty == 0) && (discountPct == null || discountPct == 0)) continue;

            AdditionalDiscountDto ad = new AdditionalDiscountDto();
            ad.setMinimumPurchaseQuantity(minQty);
            ad.setAdditionalDiscountPercentage(discountPct);
            ad.setEffectiveStartDate(getLocalDate(row, base + 3));
            ad.setEffectiveStartTime(getLocalTime(row, base + 4));
            ad.setEffectiveEndDate(getLocalDate(row, base + 5));
            ad.setEffectiveEndTime(getLocalTime(row, base + 6));
            additionalDiscounts.add(ad);
        }

        dto.setPricingDetails(Set.of(buildPricing(
                getString(row, COL_BATCH_NUMBER),
                toStartOfDay(getLocalDate(row, COL_MFG_DATE)),
                toEndOfDay(getLocalDate(row, COL_EXPIRY_DATE)),
                getString(row, COL_STORAGE_CONDITION),
                getNullSafeLong(row, COL_STOCK_QTY),
                getLocalDate(row, COL_DATE_OF_ENTRY),
                getNullSafeLong(row, COL_MRP),
                getNullSafeLong(row, COL_SELLING_PRICE),
                getNullSafeLong(row, COL_DISCOUNT_PCT),
                getNullSafeLong(row, COL_GST_PCT),
                getNullSafeLong(row, COL_HSN_CODE),
                getNullSafeLong(row, COL_SHELF_LIFE_MONTHS),
                additionalDiscounts)));

        dto.setProductAttributeDrugs(Set.of(buildDrugAttr(
                dosageFormName,
                getString(row, COL_THERAPEUTIC_CAT),
                getString(row, COL_THERAPEUTIC_SUBCAT),
                getString(row, COL_MOLECULES),
                getString(row, COL_STRENGTH))));

        return dto;
    }

    // ── CSV entry point ───────────────────────────────────────────────────
    @Override
    public ProductDetailsDto mapCsv(CSVRecord r) {

        validateMandatoryCsv(r);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getCsvString(r, H_PRODUCT_NAME));
        dto.setWarningsPrecautions(getCsvString(r, H_WARNINGS));
        dto.setProductDescription(getCsvString(r, H_DESCRIPTION));
        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));
        dto.setProductMarketingUrl(getCsvString(r, H_MARKETING_URL));

        Long unitPerPack = getCsvLong(r, H_UNIT_PER_PACK);
        Long numberOfPacks = getCsvLong(r, H_NUMBER_OF_PACKS);
        String packTypeName = getCsvString(r, H_PACK_TYPE);
        String dosageFormName = getCsvString(r, H_DOSAGE_FORM);

        dto.setPackagingDetails(buildPackaging(
                unitPerPack, numberOfPacks,
                getCsvLong(r, H_MIN_ORDER_QTY),
                getCsvLong(r, H_MAX_ORDER_QTY),
                packTypeName, dosageFormName));

        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();
        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            Long minQty = getCsvLongByIndex(r, CSV_SLAB_MIN_QTY_COLS[slab]);
            Long discountPct = getCsvLongByIndex(r, CSV_SLAB_DISCOUNT_COLS[slab]);
            if ((minQty == null || minQty == 0) && (discountPct == null || discountPct == 0)) continue;

            AdditionalDiscountDto ad = new AdditionalDiscountDto();
            ad.setMinimumPurchaseQuantity(minQty);
            ad.setAdditionalDiscountPercentage(discountPct);
            ad.setEffectiveStartDate(parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_START_DATE_COLS[slab])));
            ad.setEffectiveStartTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_START_TIME_COLS[slab])));
            ad.setEffectiveEndDate(parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_END_DATE_COLS[slab])));
            ad.setEffectiveEndTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_END_TIME_COLS[slab])));
            additionalDiscounts.add(ad);
        }

        dto.setPricingDetails(Set.of(buildPricing(
                getCsvString(r, H_BATCH_NUMBER),
                toStartOfDay(parseCsvDate(getCsvString(r, H_MFG_DATE))),
                toEndOfDay(parseCsvDate(getCsvString(r, H_EXPIRY_DATE))),
                getCsvString(r, H_STORAGE_CONDITION),
                getCsvLong(r, H_STOCK_QTY),
                parseCsvDate(getCsvString(r, H_DATE_OF_ENTRY)),
                getCsvLong(r, H_MRP),
                getCsvLong(r, H_SELLING_PRICE),
                getCsvLong(r, H_DISCOUNT_PCT),
                getCsvLong(r, H_GST_PCT),
                getCsvLong(r, H_HSN_CODE),
                getCsvLong(r, H_SHELF_LIFE_MONTHS),
                additionalDiscounts)));

        dto.setProductAttributeDrugs(Set.of(buildDrugAttr(
                dosageFormName,
                getCsvString(r, H_THERAPEUTIC_CAT),
                getCsvString(r, H_THERAPEUTIC_SUBCAT),
                getCsvString(r, H_MOLECULES),
                getCsvStringByIndex(r, COL_STRENGTH))));

        return dto;
    }

    // ── Shared builders ───────────────────────────────────────────────────
    private PackagingDetailsDto buildPackaging(
            Long unitPerPack, Long numberOfPacks,
            Long minOrderQty, Long maxOrderQty,
            String packTypeName, String dosageFormName) {

        PackagingDetailsDto packaging = new PackagingDetailsDto();
        packaging.setUnitPerPack(unitPerPack);
        packaging.setNumberOfPacks(numberOfPacks);
        if (unitPerPack != null && numberOfPacks != null) {
            packaging.setPackSize(unitPerPack * numberOfPacks);
        }
        packaging.setMinimumOrderQuantity(minOrderQty);
        packaging.setMaximumOrderQuantity(maxOrderQty);

        if (packTypeName != null && !packTypeName.isBlank()
                && dosageFormName != null && !dosageFormName.isBlank()) {
            Long packId = packTypeRepository
                    .findByPackTypeAndDosageForm_DosageName(packTypeName, dosageFormName)
                    .orElseThrow(() -> new RuntimeException(
                            "Pack type '" + packTypeName + "' not found for dosage form '" + dosageFormName + "'"))
                    .getPackId();
            packaging.setPackId(packId);
        }
        return packaging;
    }

    private PricingDetailsDto buildPricing(
            String batchNumber, LocalDateTime mfgDate, LocalDateTime expiryDate,
            String storageCondition, Long stockQty, LocalDate dateOfEntry,
            Long mrp, Long sellingPrice, Long discountPct, Long gstPct,
            Long hsnCode, Long shelfLifeMonths,
            Set<AdditionalDiscountDto> additionalDiscounts) {

        PricingDetailsDto pricing = new PricingDetailsDto();
        pricing.setBatchLotNumber(batchNumber);
        pricing.setManufacturingDate(mfgDate);
        pricing.setExpiryDate(expiryDate);
        pricing.setStorageCondition(storageCondition);
        pricing.setStockQuantity(stockQty);
        pricing.setDateOfStockEntry(dateOfEntry);
        pricing.setMrp(mrp);
        pricing.setSellingPrice(sellingPrice);
        pricing.setDiscountPercentage(discountPct);
        pricing.setGstPercentage(gstPct);
        pricing.setHsnCode(hsnCode);
        pricing.setShelfLifeMonths(shelfLifeMonths);
        if (!additionalDiscounts.isEmpty()) {
            pricing.setAdditionalDiscounts(additionalDiscounts);
        }
        return pricing;
    }

    private ProductAttributeDrugDto buildDrugAttr(
            String dosageForm, String therapeuticCat, String therapeuticSubCat,
            String moleculeCell, String strengthCell) {

        ProductAttributeDrugDto attr = new ProductAttributeDrugDto();
        attr.setDosageForm(dosageForm);

        if (therapeuticCat != null && !therapeuticCat.isBlank()) {
            attr.setTherapeuticCategoryId(
                    therapeuticCategoryRepository
                            .findByTherapeuticCategory(therapeuticCat)
                            .orElseThrow(() -> new RuntimeException(
                                    "Therapeutic category not found: " + therapeuticCat))
                            .getTherapeuticCategoryId());
        }

        if (therapeuticSubCat != null && !therapeuticSubCat.isBlank()) {
            attr.setTherapeuticSubcategoryId(
                    therapeuticSubcategoryRepository
                            .findByTherapeuticSubcategory(therapeuticSubCat)
                            .orElseThrow(() -> new RuntimeException(
                                    "Therapeutic subcategory not found: " + therapeuticSubCat))
                            .getTherapeuticSubcategoryId());
        }

        if (moleculeCell != null && !moleculeCell.isBlank()) {
            String[] moleculeNames = moleculeCell.split(",");
            String[] strengths = (strengthCell != null && !strengthCell.isBlank())
                    ? strengthCell.split(",") : new String[0];

            List<ProductMoleculeDto> molecules = new ArrayList<>();
            for (int i = 0; i < moleculeNames.length; i++) {
                String name = moleculeNames[i].trim();
                String strength = (i < strengths.length) ? strengths[i].trim() : null;

                Molecule m = moleculeRepository
                        .findByMoleculeName(name)
                        .orElseThrow(() -> new RuntimeException("Molecule not found: " + name));

                ProductMoleculeDto pm = new ProductMoleculeDto();
                pm.setMoleculeId(m.getMoleculeId());
                pm.setStrength(strength);
                molecules.add(pm);
            }
            attr.setMolecules(molecules);
        }

        return attr;
    }

    // ── Excel helpers ─────────────────────────────────────────────────────
    private String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        return (cell != null) ? cell.toString().trim() : null;
    }

    private LocalDate getLocalDate(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime getLocalTime(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            return cell.getLocalDateTimeCellValue().toLocalTime();
        } catch (Exception e) {
            return null;
        }
    }

    private Long getNullSafeLong(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (long) cell.getNumericCellValue();
            case STRING:
                String s = cell.getStringCellValue().trim();
                if (s.isBlank()) return null;
                try {
                    return Long.parseLong(s);
                } catch (Exception e) {
                    return null;
                }
            case BLANK:
            case _NONE:
                return null;
            case FORMULA:
                try {
                    return (long) cell.getNumericCellValue();
                } catch (Exception e) {
                    return null;
                }
            default:
                return null;
        }
    }

    // ── CSV helpers ───────────────────────────────────────────────────────
    private String getCsvString(CSVRecord r, String header) {
        try {
            String v = r.get(header);
            return (v != null && !v.isBlank()) ? v.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCsvStringByIndex(CSVRecord r, int index) {
        try {
            String v = r.get(index);
            return (v != null && !v.isBlank()) ? v.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long getCsvLong(CSVRecord r, String header) {
        try {
            String v = getCsvString(r, header);
            return (v != null) ? Long.parseLong(v) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long getCsvLongByIndex(CSVRecord r, int index) {
        try {
            String v = getCsvStringByIndex(r, index);
            return (v != null) ? Long.parseLong(v) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Supports: "Sep-25" (MMM-yy), "04-03-2026" (dd-MM-yyyy), "2026-03-01" (ISO)
    private LocalDate parseCsvDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        raw = raw.trim();
        try {
            return LocalDate.parse(raw);
        } catch (Exception ignored) {
        }
        try {
            return YearMonth.parse(raw, DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH)).atDay(1);
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("M/d/yyyy"));
        } catch (Exception ignored) {
        }
        log.warn("Could not parse date: '{}'", raw);
        return null;
    }

    private LocalTime parseCsvTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalTime.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────
    private LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59) : null;
    }

    private void validateMandatoryExcel(Row row) {
        List<String> errors = new ArrayList<>();

        validateRequired(getString(row, COL_PRODUCT_NAME), "Product Name", errors);
        validateRequired(getString(row, COL_THERAPEUTIC_CAT), "Therapeutic Category", errors);
        validateRequired(getString(row, COL_THERAPEUTIC_SUBCAT), "Therapeutic Sub Category", errors);
        validateRequired(getString(row, COL_MOLECULES), "Molecule", errors);
        validateRequired(getString(row, COL_DOSAGE_FORM), "Dosage Form", errors);
        validateRequired(getString(row, COL_WARNINGS), "Warnings / Precautions", errors);
        validateRequired(getString(row, COL_DESCRIPTION), "Product Description", errors);
        validateRequired(getString(row, COL_BATCH_NUMBER), "Batch Number", errors);
        validateRequired(getString(row, COL_STORAGE_CONDITION), "Storage Condition", errors);

        validateRequired(getNullSafeLong(row, COL_MIN_ORDER_QTY), "Minimum Order Qty", errors);
        validateRequired(getNullSafeLong(row, COL_MAX_ORDER_QTY), "Max Order Qty", errors);
        validateRequired(getNullSafeLong(row, COL_STOCK_QTY), "Stock Quantity", errors);
        validateRequired(getNullSafeLong(row, COL_MRP), "MRP", errors);
        validateRequired(getNullSafeLong(row, COL_SELLING_PRICE), "Selling Price", errors);
        validateRequired(getNullSafeLong(row, COL_HSN_CODE), "HSN Code", errors);

        validateRequired(getLocalDate(row, COL_MFG_DATE), "Manufacturing Date", errors);
        validateRequired(getLocalDate(row, COL_EXPIRY_DATE), "Expiry Date", errors);
        validateRequired(getLocalDate(row, COL_DATE_OF_ENTRY), "Date of Entry", errors);

        // Business validations
        Long mrp = getNullSafeLong(row, COL_MRP);
        Long sellingPrice = getNullSafeLong(row, COL_SELLING_PRICE);

        if (mrp != null && mrp <= 0) {
            errors.add("MRP must be greater than 0");
        }

        if (sellingPrice != null && mrp != null && sellingPrice > mrp) {
            errors.add("Selling Price cannot be greater than MRP");
        }

        LocalDate mfg = getLocalDate(row, COL_MFG_DATE);
        LocalDate exp = getLocalDate(row, COL_EXPIRY_DATE);

        if (mfg != null && exp != null && exp.isBefore(mfg)) {
            errors.add("Expiry Date cannot be before Manufacturing Date");
        }

        String moleculeCell = getString(row, COL_MOLECULES);
        String strengthCell = getString(row, COL_STRENGTH);

        if (moleculeCell != null && !moleculeCell.isBlank()) {

            String[] molecules = Arrays.stream(moleculeCell.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);

            if (strengthCell == null || strengthCell.isBlank()) {
                errors.add("Strength is mandatory for molecules: " + String.join(", ", molecules));
            } else {

                String[] strengths = Arrays.stream(strengthCell.split(","))
                        .map(String::trim)
                        .toArray(String[]::new);

                if (molecules.length != strengths.length) {
                    errors.add("Mismatch: " + molecules.length + " molecule(s) but "
                            + strengths.length + " strength value(s) provided for molecules: "
                            + String.join(", ", molecules));
                }

                int max = Math.max(molecules.length, strengths.length);

                for (int i = 0; i < max; i++) {

                    String molecule = (i < molecules.length) ? molecules[i] : "UNKNOWN";
                    String strength = (i < strengths.length) ? strengths[i] : null;

                    if (strength == null || strength.isBlank()) {
                        errors.add("Strength missing for molecule: " + molecule);
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void validateMandatoryCsv(CSVRecord r) {
        List<String> errors = new ArrayList<>();

        validateRequired(getCsvString(r, H_PRODUCT_NAME), "Product Name", errors);
        validateRequired(getCsvString(r, H_THERAPEUTIC_CAT), "Therapeutic Category", errors);
        validateRequired(getCsvString(r, H_THERAPEUTIC_SUBCAT), "Therapeutic Sub Category", errors);
        validateRequired(getCsvString(r, H_MOLECULES), "Molecule", errors);
        validateRequired(getCsvString(r, H_DOSAGE_FORM), "Dosage Form", errors);
        validateRequired(getCsvString(r, H_WARNINGS), "Warnings", errors);
        validateRequired(getCsvString(r, H_DESCRIPTION), "Product Description", errors);
        validateRequired(getCsvString(r, H_BATCH_NUMBER), "Batch Number", errors);
        validateRequired(getCsvString(r, H_STORAGE_CONDITION), "Storage Condition", errors);

        validateRequired(getCsvLong(r, H_MIN_ORDER_QTY), "Minimum Order Qty", errors);
        validateRequired(getCsvLong(r, H_MAX_ORDER_QTY), "Max Order Qty", errors);
        validateRequired(getCsvLong(r, H_STOCK_QTY), "Stock Quantity", errors);
        validateRequired(getCsvLong(r, H_MRP), "MRP", errors);
        validateRequired(getCsvLong(r, H_SELLING_PRICE), "Selling Price", errors);
        validateRequired(getCsvLong(r, H_HSN_CODE), "HSN Code", errors);

        validateRequired(parseCsvDate(getCsvString(r, H_MFG_DATE)), "Manufacturing Date", errors);
        validateRequired(parseCsvDate(getCsvString(r, H_EXPIRY_DATE)), "Expiry Date", errors);
        validateRequired(parseCsvDate(getCsvString(r, H_DATE_OF_ENTRY)), "Date of Entry", errors);

        Long mrp = getCsvLong(r, H_MRP);
        Long sellingPrice = getCsvLong(r, H_SELLING_PRICE);

        if (mrp != null && mrp <= 0) {
            errors.add("MRP must be greater than 0");
        }

        if (sellingPrice != null && mrp != null && sellingPrice > mrp) {
            errors.add("Selling Price cannot be greater than MRP");
        }

        LocalDate mfg = parseCsvDate(getCsvString(r, H_MFG_DATE));
        LocalDate exp = parseCsvDate(getCsvString(r, H_EXPIRY_DATE));

        if (mfg != null && exp != null && exp.isBefore(mfg)) {
            errors.add("Expiry Date cannot be before Manufacturing Date");
        }

        String moleculeCell = getCsvString(r, H_MOLECULES);
        String strengthCell = getCsvStringByIndex(r, COL_STRENGTH);

        if (moleculeCell != null && !moleculeCell.isBlank()) {

            String[] molecules = Arrays.stream(moleculeCell.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);

            if (strengthCell == null || strengthCell.isBlank()) {
                errors.add("Strength is mandatory for molecules: " + String.join(", ", molecules));
            } else {

                String[] strengths = Arrays.stream(strengthCell.split(","))
                        .map(String::trim)
                        .toArray(String[]::new);

                if (molecules.length != strengths.length) {
                    errors.add("Mismatch: " + molecules.length + " molecule(s) but "
                            + strengths.length + " strength value(s) provided for molecules: "
                            + String.join(", ", molecules));
                }

                int max = Math.max(molecules.length, strengths.length);

                for (int i = 0; i < max; i++) {

                    String molecule = (i < molecules.length) ? molecules[i] : "UNKNOWN";
                    String strength = (i < strengths.length) ? strengths[i] : null;

                    if (strength == null || strength.isBlank()) {
                        errors.add("Strength missing for molecule: " + molecule);
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void validateRequired(Object value, String field, List<String> errors) {
        if (value == null) {
            errors.add(field + " is mandatory");
        } else if (value instanceof String && ((String) value).isBlank()) {
            errors.add(field + " is mandatory");
        }
    }
}