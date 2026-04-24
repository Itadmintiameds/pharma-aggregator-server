package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.repository.product.*;
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
@Component("CONSUMABLE")
@RequiredArgsConstructor
public class ConsumableImportStrategy implements ProductImportStrategy {

    private final DeviceCategoryRepository deviceCategoryRepository;
    private final DeviceSubCategoryRepository deviceSubCategoryRepository;
    private final CertificationRepository certificationRepository;
    private final ConsumableMaterialTypeRepository materialTypeRepository;
    private final CountryMasterRepository countryRepository;
    private final StorageConditionMasterRepository storageConditionRepository;
    private final PackTypeRepository packTypeRepository;

    // ===== COLUMN INDEX (0-based, verified against Excel template row 0) =====
    // ===== BASIC DETAILS =====
    private static final int COL_DEVICE_CATEGORY = 0;
    private static final int COL_DEVICE_SUBCATEGORY = 1;
    private static final int COL_PRODUCT_NAME = 2;
    private static final int COL_BRAND_NAME = 3;

    // ===== MATERIAL / PRODUCT DETAILS =====
    private static final int COL_DIMENSION_SIZE = 4;
    private static final int COL_STERILE = 5;
    private static final int COL_DISPOSABLE = 6;

    // ===== DESCRIPTION =====
    private static final int COL_PURPOSE = 7;
    private static final int COL_KEY_FEATURES = 8;
    private static final int COL_SAFETY_INSTRUCTIONS = 9;
    private static final int COL_CERTIFICATIONS = 10;

    // ===== MATERIAL =====
    private static final int COL_MATERIAL_TYPES = 11;

    // ===== MANUFACTURING =====
    private static final int COL_COUNTRY = 12;
    private static final int COL_MANUFACTURER = 13;
    private static final int COL_PRODUCT_DESCRIPTION = 14;
    private static final int COL_STORAGE_CONDITION = 15;

    // ===== PACKAGING =====
    private static final int COL_PACK_TYPE = 16;
    private static final int COL_UNIT_PER_PACK = 17;
    private static final int COL_NUMBER_OF_PACKS = 18;

    // col 19 = Pack Size (Auto-calculated)
    private static final int COL_MIN_ORDER_QTY = 20;
    private static final int COL_MAX_ORDER_QTY = 21;
    private static final int COL_BATCH_NUMBER = 22;
    private static final int COL_MANUFACTURING_DATE = 23;
    private static final int COL_EXPIRY_DATE = 24;
    private static final int COL_STOCK_QUANTITY = 25;
    private static final int COL_DATE_OF_ENTRY = 26;

    // ===== PRICING =====
    private static final int COL_MRP = 27;
    private static final int COL_SELLING_PRICE = 28;
    private static final int COL_DISCOUNT = 29;
    private static final int COL_GST = 30;
    private static final int COL_HSN = 31;

    // ===== ADDITIONAL DISCOUNT =====
    private static final int COL_ADD_DISCOUNT_START = 32;
    private static final int ADD_DISCOUNT_SLAB_SIZE = 7;
    private static final int ADD_DISCOUNT_SLAB_COUNT = 4;
    // col 60 = Product Image URL* (not persisted by this strategy)

    // ===== CSV HEADER CONSTANTS =====
    // ===== BASIC =====
    private static final String H_DEVICE_CATEGORY = "Device Category*";
    private static final String H_DEVICE_SUBCATEGORY = "Device Sub Category*";
    private static final String H_PRODUCT_NAME = "Product Name*";
    private static final String H_BRAND_NAME = "Brand Name*";

    // ===== MATERIAL =====
    private static final String H_MATERIAL_TYPES = "Material / Build Type*";
    private static final String H_DIMENSION_SIZE = "Size / Dimension / Gauge*";
    private static final String H_STERILE = "Sterile / Non-Sterile*";
    private static final String H_DISPOSABLE = "Disposable / Reusable*";

    // ===== DESCRIPTION =====
    private static final String H_PURPOSE = "Intended Use / Purpose*";
    private static final String H_KEY_FEATURES = "Key Features / Technical Specifications*";
    private static final String H_SAFETY_INSTRUCTIONS = "Safety Instructions  / Precautions*";
    private static final String H_CERTIFICATIONS = "Certifications / Compliance*";

    // ===== MANUFACTURING =====
    private static final String H_COUNTRY = "Country of Origin*";
    private static final String H_MANUFACTURER = "Manufacture Name*";
    private static final String H_PRODUCT_DESCRIPTION = "Product Description*";
    private static final String H_STORAGE_CONDITION = "Storage Condition (if applicable)";

    // ===== PACKAGING =====
    private static final String H_PACK_TYPE = "Pack Type";
    private static final String H_UNIT_PER_PACK = "Unit Per Pack";
    private static final String H_NUMBER_OF_PACKS = "Number Of Packs";

    // ===== NEW COLUMNS =====
    private static final String H_MIN_ORDER_QTY = "Minimum Order Qty*";
    private static final String H_MAX_ORDER_QTY = "Max Order Qty*";
    private static final String H_BATCH_NUMBER = "Batch Number*";
    private static final String H_MANUFACTURING_DATE = "Manufacturing Date*";
    private static final String H_EXPIRY_DATE = "Expiry Date*";
    private static final String H_STOCK_QUANTITY = "Stock Quantity*";
    private static final String H_DATE_OF_ENTRY = "Date of Entry*";

    // ===== PRICING =====
    private static final String H_MRP = "MRP (INR)*";
    private static final String H_SELLING_PRICE = "Selling Price(INR)*";
    private static final String H_DISCOUNT = "Discount %";
    private static final String H_GST = "GST %";
    private static final String H_HSN = "HSN Code*";

    // Each slab block = 7 cols: [label | minQty | disc% | startDate | startTime | endDate | endTime]
    // Slab label cols: 32, 39, 46, 53 (sub-header row 1)
    private static final int[] CSV_SLAB_MIN_QTY_COLS = {33, 40, 47, 54};
    private static final int[] CSV_SLAB_DISCOUNT_COLS = {34, 41, 48, 55};
    private static final int[] CSV_SLAB_START_DATE_COLS = {35, 42, 49, 56};
    private static final int[] CSV_SLAB_START_TIME_COLS = {36, 43, 50, 57};
    private static final int[] CSV_SLAB_END_DATE_COLS = {37, 44, 51, 58};
    private static final int[] CSV_SLAB_END_TIME_COLS = {38, 45, 52, 59};

    @Override
    public ProductDetailsDto mapRow(Row row, Long categoryId) {
        log.info("Consumable Excel import Called");

        validateMandatoryExcel(row);

        ProductDetailsDto dto = new ProductDetailsDto();

        // ===== BASIC =====
        dto.setProductName(getString(row, COL_PRODUCT_NAME));
        dto.setProductDescription(getString(row, COL_PRODUCT_DESCRIPTION));
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));
        dto.setWarningsPrecautions(getString(row, COL_SAFETY_INSTRUCTIONS));

        // ===== PACKAGING =====
        dto.setPackagingDetails(Set.of(buildPackaging(
                getLong(row, COL_UNIT_PER_PACK),
                getLong(row, COL_NUMBER_OF_PACKS),
                getLong(row, COL_MIN_ORDER_QTY),
                getLong(row, COL_MAX_ORDER_QTY),
                getString(row, COL_PACK_TYPE),
                categoryId
        )));

        // ===== ADDITIONAL DISCOUNTS =====
        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {

            int base = COL_ADD_DISCOUNT_START + (slab * ADD_DISCOUNT_SLAB_SIZE);

            Long minQty = getLong(row, base + 1);
            Long discount = getLong(row, base + 2);

            if ((minQty == null || minQty == 0) &&
                    (discount == null || discount == 0)) continue;

            AdditionalDiscountDto d = new AdditionalDiscountDto();
            d.setMinimumPurchaseQuantity(minQty);
            d.setAdditionalDiscountPercentage(discount);
            d.setEffectiveStartDate(getDate(row, base + 3));
            d.setEffectiveStartTime(getTime(row, base + 4));
            d.setEffectiveEndDate(getDate(row, base + 5));
            d.setEffectiveEndTime(getTime(row, base + 6));

            additionalDiscounts.add(d);
        }

        // ===== PRICING =====
        Long shelfLifeMonths = calculateShelfLifeMonths(
                getDate(row, COL_MANUFACTURING_DATE),
                getDate(row, COL_EXPIRY_DATE)
        );

        dto.setPricingDetails(Set.of(buildPricing(
                getString(row, COL_BATCH_NUMBER),
                toStart(getDate(row, COL_MANUFACTURING_DATE)),
                toEnd(getDate(row, COL_EXPIRY_DATE)),
                null,
                getLong(row, COL_STOCK_QUANTITY),
                getDate(row, COL_DATE_OF_ENTRY),
                getLong(row, COL_MRP),
                getLong(row, COL_SELLING_PRICE),
                getLong(row, COL_DISCOUNT),
                getLong(row, COL_GST),
                getLong(row, COL_HSN),
                shelfLifeMonths,   // ✅ FIXED HERE
                additionalDiscounts
        )));

        // ===== ATTRIBUTES =====
        dto.setProductAttributeConsumableMedicals(
                Set.of(buildConsumableAttr(row))
        );

        return dto;
    }

    private ConsumableProductAttributeDTO buildConsumableAttr(Row row) {

        ConsumableProductAttributeDTO dto = new ConsumableProductAttributeDTO();

        // ===== CATEGORY =====
        dto.setDeviceCatId(
                deviceCategoryRepository.findByDeviceName(getString(row, COL_DEVICE_CATEGORY))
                        .orElseThrow(() -> new RuntimeException("Device category not found"))
                        .getDeviceCatId()
        );

        dto.setDeviceSubCatId(
                deviceSubCategoryRepository.findBySubCategoryName(getString(row, COL_DEVICE_SUBCATEGORY))
                        .orElseThrow(() -> new RuntimeException("Device subcategory not found"))
                        .getDeviceSubCatId()
        );

        // ===== BASIC =====
        dto.setBrandName(getString(row, COL_BRAND_NAME));
        dto.setDimensionSize(getString(row, COL_DIMENSION_SIZE));
        dto.setSterileOrNonSterile(getString(row, COL_STERILE));
        dto.setDisposalOrReusable(getString(row, COL_DISPOSABLE));
        dto.setPurpose(getString(row, COL_PURPOSE));
        dto.setKeyFeaturesSpecifications(getString(row, COL_KEY_FEATURES));
        dto.setSafetyInstructions(getString(row, COL_SAFETY_INSTRUCTIONS));

        Long shelfLifeMonths = calculateShelfLifeMonths(
                getDate(row, COL_MANUFACTURING_DATE),
                getDate(row, COL_EXPIRY_DATE)
        );
        dto.setShelfLife(shelfLifeMonths.toString());

        // ===== CERTIFICATIONS =====
        String certCell = getString(row, COL_CERTIFICATIONS);
        if (certCell != null && !certCell.isBlank()) {

            List<ProductCertificateDocumentDto> docs = new ArrayList<>();

            for (String c : certCell.split(",")) {

                Certification cert = certificationRepository
                        .findByCertificationName(c.trim())
                        .orElseThrow(() -> new RuntimeException("Certification not found: " + c));

                ProductCertificateDocumentDto d = new ProductCertificateDocumentDto();
                d.setCertificationId(cert.getCertificationId());
                d.setCertificateUrl("NOT_UPLOADED");

                docs.add(d);
            }

            dto.setCertificateDocuments(docs);
        }

        // ===== MATERIAL TYPES =====
        String materialCell = getString(row, COL_MATERIAL_TYPES);
        if (materialCell != null && !materialCell.isBlank()) {

            List<Long> ids = new ArrayList<>();

            for (String m : materialCell.split(",")) {

                ids.add(
                        materialTypeRepository.findByMaterialTypeName(m.trim())
                                .orElseThrow(() -> new RuntimeException("Material not found: " + m))
                                .getMaterialTypeId()
                );
            }

            dto.setMaterialTypeId(ids);
        }

        // ===== COUNTRY =====
        dto.setCountryId(
                countryRepository.findByCountryName(getString(row, COL_COUNTRY))
                        .orElseThrow(() -> new RuntimeException("Country not found"))
                        .getCountryId()
        );

        // ===== STORAGE =====
        String storage = getString(row, COL_STORAGE_CONDITION);
        if (storage != null && !storage.isBlank()) {
            dto.setStorageConditionId(
                    storageConditionRepository.findByConditionName(storage)
                            .orElseThrow(() -> new RuntimeException("Storage condition not found"))
                            .getStorageConditionId()
            );
        }

        // ===== MANUFACTURER =====
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));

        // ===== DEFAULTS =====
        dto.setBrochurePath("NOT_UPLOADED");

        return dto;
    }

    private PackagingDetailsDto buildPackaging(Long unit, Long packs,
                                               Long min, Long max,
                                               String packType, Long categoryId) {

        PackagingDetailsDto dto = new PackagingDetailsDto();

        dto.setUnitPerPack(unit);
        dto.setNumberOfPacks(packs);
        dto.setMinimumOrderQuantity(min);
        dto.setMaximumOrderQuantity(max);

        if (unit != null && packs != null) {
            dto.setPackSize(unit * packs);
        }

        if (packType != null && !packType.isBlank()) {
            dto.setPackId(
                    packTypeRepository.findByPackTypeAndCategory_CategoryId(packType, categoryId)
                            .orElseThrow(() -> new RuntimeException("Pack type not found: " + packType))
                            .getPackId()
            );
        }

        return dto;
    }

    private PricingDetailsDto buildPricing(
            String batchNumber,
            LocalDateTime mfgDate,
            LocalDateTime expiryDate,
            String storageCondition,
            Long stockQty,
            LocalDate dateOfEntry,
            Long mrp,
            Long sellingPrice,
            Long discountPct,
            Long gstPct,
            Long hsnCode,
            Long shelfLifeMonths,
            Set<AdditionalDiscountDto> additionalDiscounts) {

        PricingDetailsDto dto = new PricingDetailsDto();

        dto.setBatchLotNumber(batchNumber);
        dto.setManufacturingDate(mfgDate);
        dto.setExpiryDate(expiryDate);
        dto.setStockQuantity(stockQty);
        dto.setDateOfStockEntry(dateOfEntry);
        dto.setMrp(mrp);
        dto.setSellingPrice(sellingPrice);
        dto.setDiscountPercentage(discountPct);
        dto.setGstPercentage(gstPct);
        dto.setHsnCode(hsnCode);
        dto.setShelfLifeMonths(shelfLifeMonths);

        if (additionalDiscounts != null && !additionalDiscounts.isEmpty()) {
            dto.setAdditionalDiscounts(additionalDiscounts);
        }

        return dto;
    }

    private String getString(Row row, int col) {
        return row.getCell(col) != null ? row.getCell(col).toString().trim() : null;
    }

    private Long getLong(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case STRING:
                    String s = cell.getStringCellValue().trim();
                    if (s.isEmpty()) return null;
                    return Long.parseLong(s);
                case FORMULA:
                    return (long) cell.getNumericCellValue();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate getDate(Row row, int col) {
        try {
            return row.getCell(col).getLocalDateTimeCellValue().toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime getTime(Row row, int col) {
        try {
            return row.getCell(col).getLocalDateTimeCellValue().toLocalTime();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime toStart(LocalDate d) {
        return d != null ? d.atStartOfDay() : null;
    }

    private LocalDateTime toEnd(LocalDate d) {
        return d != null ? d.atTime(23, 59, 59) : null;
    }

    private void validateMandatoryExcel(Row row) {

        List<String> errors = new ArrayList<>();

        // ===== PRODUCT DETAILS =====
        validateRequired(getString(row, COL_PRODUCT_NAME), "Product Name", errors);
        validateRequired(getString(row, COL_DEVICE_CATEGORY), "Device Category", errors);
        validateRequired(getString(row, COL_DEVICE_SUBCATEGORY), "Device Sub Category", errors);
        validateRequired(getString(row, COL_BRAND_NAME), "Brand Name", errors);
        validateRequired(getString(row, COL_MATERIAL_TYPES), "Material Type", errors);
        validateRequired(getString(row, COL_DIMENSION_SIZE), "Size / Dimension / Gauge", errors);
        validateRequired(getString(row, COL_STERILE), "Sterile / Non-Sterile", errors);
        validateRequired(getString(row, COL_DISPOSABLE), "Disposable / Reusable", errors);
        validateRequired(getString(row, COL_PURPOSE), "Intended Use / Purpose", errors);
        validateRequired(getString(row, COL_KEY_FEATURES), "Key Features / Specifications", errors);
        validateRequired(getString(row, COL_CERTIFICATIONS), "Certifications / Compliance", errors);
        validateRequired(getString(row, COL_COUNTRY), "Country of Origin", errors);
        validateRequired(getString(row, COL_MANUFACTURER), "Manufacturer Name", errors);
        validateRequired(getString(row, COL_PRODUCT_DESCRIPTION), "Product Description", errors);
        validateRequired(getString(row, COL_STORAGE_CONDITION), "Storage Condition", errors);

        // ===== PACKAGING =====
        validateRequired(getString(row, COL_PACK_TYPE), "Pack Type", errors);
        validateRequired(getLong(row, COL_UNIT_PER_PACK), "Units per Pack", errors);
        validateRequired(getLong(row, COL_NUMBER_OF_PACKS), "Number of Packs", errors);
        validateRequired(getLong(row, COL_MIN_ORDER_QTY), "Minimum Order Quantity", errors);
        validateRequired(getLong(row, COL_MAX_ORDER_QTY), "Maximum Order Quantity", errors);

        // ===== BATCH & STOCK =====
        validateRequired(getString(row, COL_BATCH_NUMBER), "Batch Number", errors);
        validateRequired(getDate(row, COL_MANUFACTURING_DATE), "Manufacturing Date", errors);
        validateRequired(getDate(row, COL_EXPIRY_DATE), "Expiry Date", errors);
        validateRequired(getLong(row, COL_STOCK_QUANTITY), "Stock Quantity", errors);
        validateRequired(getDate(row, COL_DATE_OF_ENTRY), "Date of Stock Entry", errors);

        // ===== PRICING =====
        validateRequired(getLong(row, COL_SELLING_PRICE), "Selling Price", errors);
        validateRequired(getLong(row, COL_MRP), "MRP", errors);
        validateRequired(getLong(row, COL_GST), "GST %", errors);
        validateRequired(getLong(row, COL_HSN), "HSN Code", errors);

        Long hsnCodeExcel = getLong(row, COL_HSN);
        if (hsnCodeExcel != null) {
            int digits = String.valueOf(hsnCodeExcel).length();
            if (digits != 4 && digits != 6 && digits != 8) {
                errors.add("HSN Code must be 4, 6, or 8 digits");
            }
        }

        // 🔹 1. Sterile validation
        String sterile = getString(row, COL_STERILE);
        if (sterile != null &&
                !sterile.equalsIgnoreCase("Sterile") &&
                !sterile.equalsIgnoreCase("Non-Sterile")) {
            errors.add("Sterile must be either 'Sterile' or 'Non-Sterile'");
        }

        // 🔹 2. Disposable validation
        String disposable = getString(row, COL_DISPOSABLE);
        if (disposable != null &&
                !disposable.equalsIgnoreCase("Disposable") &&
                !disposable.equalsIgnoreCase("Reusable")) {
            errors.add("Disposable must be either 'Disposable' or 'Reusable'");
        }

        // 🔹 3. GST validation
        Long gst = getLong(row, COL_GST);
        if (gst != null && (gst < 0 || gst > 100)) {
            errors.add("GST must be between 0 and 100");
        }


        // ===== BUSINESS VALIDATIONS =====

        LocalDate mfg = getDate(row, COL_MANUFACTURING_DATE);
        LocalDate exp = getDate(row, COL_EXPIRY_DATE);

        if (mfg != null && exp != null && exp.isBefore(mfg)) {
            errors.add("Expiry Date must be after Manufacturing Date");
        }

        Long minQty = getLong(row, COL_MIN_ORDER_QTY);
        Long maxQty = getLong(row, COL_MAX_ORDER_QTY);

        if (minQty != null && maxQty != null && maxQty < minQty) {
            errors.add("Max Order Qty must be >= Min Order Qty");
        }

        Long mrp = getLong(row, COL_MRP);
        Long selling = getLong(row, COL_SELLING_PRICE);

        if (mrp != null && selling != null && selling > mrp) {
            errors.add("Selling Price cannot be greater than MRP");
        }

        // ===== FINAL =====
        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(", ", errors));
        }
    }

    private void validateRequired(Object value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(fieldName + " is mandatory");
        } else if (value instanceof String && ((String) value).isBlank()) {
            errors.add(fieldName + " is mandatory");
        }
    }

    private Long calculateShelfLifeMonths(LocalDate mfg, LocalDate exp) {

        if (mfg == null || exp == null) return null;

        if (exp.isBefore(mfg)) {
            throw new RuntimeException("Expiry Date must be after Manufacturing Date");
        }

        return (long) java.time.Period.between(mfg, exp).toTotalMonths();
    }

    @Override
    public ProductDetailsDto mapCsv(CSVRecord r, Long categoryId) {
        log.info("Consumable CSV import Called");

        validateMandatoryCsv(r);

        ProductDetailsDto dto = new ProductDetailsDto();

        // ===== BASIC =====
        dto.setProductName(getCsvString(r, H_PRODUCT_NAME));
        dto.setProductDescription(getCsvString(r, H_PRODUCT_DESCRIPTION));
        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));
        dto.setWarningsPrecautions(getCsvString(r, H_SAFETY_INSTRUCTIONS));

        // ===== PACKAGING =====
        dto.setPackagingDetails(Set.of(buildPackaging(
                getCsvLong(r, H_UNIT_PER_PACK),
                getCsvLong(r, H_NUMBER_OF_PACKS),
                getCsvLong(r, H_MIN_ORDER_QTY),
                getCsvLong(r, H_MAX_ORDER_QTY),
                getCsvString(r, H_PACK_TYPE),
                categoryId
        )));

        // ===== ADDITIONAL DISCOUNTS =====
        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {

            Long minQty = getCsvLongByIndex(r, CSV_SLAB_MIN_QTY_COLS[slab]);
            Long discount = getCsvLongByIndex(r, CSV_SLAB_DISCOUNT_COLS[slab]);

            if ((minQty == null || minQty == 0) &&
                    (discount == null || discount == 0)) continue;

            AdditionalDiscountDto d = new AdditionalDiscountDto();
            d.setMinimumPurchaseQuantity(minQty);
            d.setAdditionalDiscountPercentage(discount);
            d.setEffectiveStartDate(parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_START_DATE_COLS[slab])));
            d.setEffectiveStartTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_START_TIME_COLS[slab])));
            d.setEffectiveEndDate(parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_END_DATE_COLS[slab])));
            d.setEffectiveEndTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_END_TIME_COLS[slab])));

            additionalDiscounts.add(d);
        }

        // ===== SHELF LIFE =====
        Long shelfLifeMonths = calculateShelfLifeMonths(
                parseCsvDate(getCsvString(r, H_MANUFACTURING_DATE)),
                parseCsvDate(getCsvString(r, H_EXPIRY_DATE))
        );

        // ===== PRICING =====
        dto.setPricingDetails(Set.of(buildPricing(
                getCsvString(r, H_BATCH_NUMBER),
                toStart(parseCsvDate(getCsvString(r, H_MANUFACTURING_DATE))),
                toEnd(parseCsvDate(getCsvString(r, H_EXPIRY_DATE))),
                null,
                getCsvLong(r, H_STOCK_QUANTITY),
                parseCsvDate(getCsvString(r, H_DATE_OF_ENTRY)),
                getCsvLong(r, H_MRP),
                getCsvLong(r, H_SELLING_PRICE),
                getCsvLong(r, H_DISCOUNT),
                getCsvLong(r, H_GST),
                getCsvLong(r, H_HSN),
                shelfLifeMonths,
                additionalDiscounts
        )));

        // ===== ATTRIBUTES =====
        dto.setProductAttributeConsumableMedicals(
                Set.of(buildConsumableAttrFromCsv(r))
        );

        return dto;
    }

    private ConsumableProductAttributeDTO buildConsumableAttrFromCsv(CSVRecord r) {

        ConsumableProductAttributeDTO dto = new ConsumableProductAttributeDTO();

        // CATEGORY
        dto.setDeviceCatId(
                deviceCategoryRepository.findByDeviceName(getCsvString(r, H_DEVICE_CATEGORY))
                        .orElseThrow(() -> new RuntimeException("Device category not found"))
                        .getDeviceCatId()
        );

        dto.setDeviceSubCatId(
                deviceSubCategoryRepository.findBySubCategoryName(getCsvString(r, H_DEVICE_SUBCATEGORY))
                        .orElseThrow(() -> new RuntimeException("Device subcategory not found"))
                        .getDeviceSubCatId()
        );

        // BASIC
        dto.setBrandName(getCsvString(r, H_BRAND_NAME));
        dto.setDimensionSize(getCsvString(r, H_DIMENSION_SIZE));
        dto.setSterileOrNonSterile(getCsvString(r, H_STERILE));
        dto.setDisposalOrReusable(getCsvString(r, H_DISPOSABLE));
        dto.setPurpose(getCsvString(r, H_PURPOSE));
        dto.setKeyFeaturesSpecifications(getCsvString(r, H_KEY_FEATURES));
        dto.setSafetyInstructions(getCsvString(r, H_SAFETY_INSTRUCTIONS));
        Long shelfLifeMonths = calculateShelfLifeMonths(
                parseCsvDate(getCsvString(r, H_MANUFACTURING_DATE)),
                parseCsvDate(getCsvString(r, H_EXPIRY_DATE))
        );
        dto.setShelfLife(shelfLifeMonths.toString());

        // CERTIFICATIONS
        String certCell = getCsvString(r, H_CERTIFICATIONS);
        if (certCell != null && !certCell.isBlank()) {

            List<ProductCertificateDocumentDto> docs = new ArrayList<>();

            for (String c : certCell.split(",")) {
                Certification cert = certificationRepository
                        .findByCertificationName(c.trim())
                        .orElseThrow(() -> new RuntimeException("Certification not found: " + c));

                ProductCertificateDocumentDto d = new ProductCertificateDocumentDto();
                d.setCertificationId(cert.getCertificationId());
                d.setCertificateUrl("NOT_UPLOADED");

                docs.add(d);
            }

            dto.setCertificateDocuments(docs);
        }

        // MATERIAL TYPES
        String materialCell = getCsvString(r, H_MATERIAL_TYPES);
        if (materialCell != null && !materialCell.isBlank()) {

            List<Long> ids = new ArrayList<>();

            for (String m : materialCell.split(",")) {
                ids.add(
                        materialTypeRepository.findByMaterialTypeName(m.trim())
                                .orElseThrow(() -> new RuntimeException("Material not found: " + m))
                                .getMaterialTypeId()
                );
            }

            dto.setMaterialTypeId(ids);
        }

        // COUNTRY
        dto.setCountryId(
                countryRepository.findByCountryName(getCsvString(r, H_COUNTRY))
                        .orElseThrow(() -> new RuntimeException("Country not found"))
                        .getCountryId()
        );

        // STORAGE
        String storage = getCsvString(r, H_STORAGE_CONDITION);
        if (storage != null && !storage.isBlank()) {
            dto.setStorageConditionId(
                    storageConditionRepository.findByConditionName(storage)
                            .orElseThrow(() -> new RuntimeException("Storage condition not found"))
                            .getStorageConditionId()
            );
        }

        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));
        dto.setBrochurePath("NOT_UPLOADED");

        return dto;
    }

    private void validateMandatoryCsv(CSVRecord r) {

        List<String> errors = new ArrayList<>();

        // ===== PRODUCT DETAILS =====
        validateRequired(getCsvString(r, H_PRODUCT_NAME), "Product Name", errors);
        validateRequired(getCsvString(r, H_DEVICE_CATEGORY), "Device Category", errors);
        validateRequired(getCsvString(r, H_DEVICE_SUBCATEGORY), "Device Sub Category", errors);
        validateRequired(getCsvString(r, H_BRAND_NAME), "Brand Name", errors);
        validateRequired(getCsvString(r, H_MATERIAL_TYPES), "Material Type", errors);
        validateRequired(getCsvString(r, H_DIMENSION_SIZE), "Size / Dimension / Gauge", errors);
        validateRequired(getCsvString(r, H_STERILE), "Sterile / Non-Sterile", errors);
        validateRequired(getCsvString(r, H_DISPOSABLE), "Disposable / Reusable", errors);
        validateRequired(getCsvString(r, H_PURPOSE), "Intended Use / Purpose", errors);
        validateRequired(getCsvString(r, H_KEY_FEATURES), "Key Features / Specifications", errors);

        validateRequired(getCsvString(r, H_CERTIFICATIONS), "Certifications / Compliance", errors);

        validateRequired(getCsvString(r, H_COUNTRY), "Country of Origin", errors);
        validateRequired(getCsvString(r, H_MANUFACTURER), "Manufacturer Name", errors);
        validateRequired(getCsvString(r, H_PRODUCT_DESCRIPTION), "Product Description", errors);
        validateRequired(getCsvString(r, H_STORAGE_CONDITION), "Storage Condition", errors);

        // ===== PACKAGING =====
        validateRequired(getCsvString(r, H_PACK_TYPE), "Pack Type", errors);
        validateRequired(getCsvLong(r, H_UNIT_PER_PACK), "Units per Pack", errors);
        validateRequired(getCsvLong(r, H_NUMBER_OF_PACKS), "Number of Packs", errors);
        validateRequired(getCsvLong(r, H_MIN_ORDER_QTY), "Minimum Order Quantity", errors);
        validateRequired(getCsvLong(r, H_MAX_ORDER_QTY), "Maximum Order Quantity", errors);

        // ===== BATCH & STOCK =====
        validateRequired(getCsvString(r, H_BATCH_NUMBER), "Batch Number", errors);

        LocalDate mfg = parseCsvDate(getCsvString(r, H_MANUFACTURING_DATE));
        LocalDate exp = parseCsvDate(getCsvString(r, H_EXPIRY_DATE));

        validateRequired(mfg, "Manufacturing Date", errors);
        validateRequired(exp, "Expiry Date", errors);

        validateRequired(getCsvLong(r, H_STOCK_QUANTITY), "Stock Quantity", errors);
        validateRequired(parseCsvDate(getCsvString(r, H_DATE_OF_ENTRY)), "Date of Stock Entry", errors);

        // ===== PRICING =====
        validateRequired(getCsvLong(r, H_SELLING_PRICE), "Selling Price", errors);
        validateRequired(getCsvLong(r, H_MRP), "MRP", errors);

        validateRequired(getCsvLong(r, H_GST), "GST %", errors);

        validateRequired(getCsvLong(r, H_HSN), "HSN Code", errors);

        Long hsnCodeCsv = getCsvLong(r, H_HSN);
        if (hsnCodeCsv != null) {
            int digits = String.valueOf(hsnCodeCsv).length();
            if (digits != 4 && digits != 6 && digits != 8) {
                errors.add("HSN Code must be 4, 6, or 8 digits");
            }
        }

        // ===== BUSINESS VALIDATIONS =====

        if (mfg != null && exp != null && exp.isBefore(mfg)) {
            errors.add("Expiry Date must be after Manufacturing Date");
        }

        Long minQty = getCsvLong(r, H_MIN_ORDER_QTY);
        Long maxQty = getCsvLong(r, H_MAX_ORDER_QTY);

        if (minQty != null && maxQty != null && maxQty < minQty) {
            errors.add("Max Order Qty must be >= Min Order Qty");
        }

//        Long mrp = getCsvLong(r, H_MRP);
//        Long selling = getCsvLong(r, H_SELLING_PRICE);
//
//        if (mrp != null && selling != null && selling > mrp) {
//            errors.add("Selling Price cannot be greater than MRP");
//        }

        Long gst = getCsvLong(r, H_GST);
        if (gst != null && (gst < 0 || gst > 100)) {
            errors.add("GST must be between 0 and 100");
        }

        // ===== FINAL =====
        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(", ", errors));
        }
    }

    private String getCsvString(CSVRecord r, String header) {
        try {
            String v = r.get(header);
            return (v != null && !v.isBlank()) ? v.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long getCsvLong(CSVRecord r, String header) {
        try {
            String v = getCsvString(r, header);
            return v != null ? Long.parseLong(v) : null;
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

    private Long getCsvLongByIndex(CSVRecord r, int index) {
        try {
            String v = getCsvStringByIndex(r, index);
            return v != null ? Long.parseLong(v) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseCsvDate(String raw) {

        if (raw == null || raw.isBlank()) return null;

        raw = raw.trim();

        // 1. ISO format → 2026-03-01
        try {
            return LocalDate.parse(raw);
        } catch (Exception ignored) {
        }

        // 2. MMM-yy → Sep-25
        try {
            return YearMonth.parse(raw,
                    DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH)
            ).atDay(1);
        } catch (Exception ignored) {
        }

        // 3. dd-MM-yyyy → 04-03-2026
        try {
            return LocalDate.parse(raw,
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception ignored) {
        }

        // 4. M/d/yyyy → 3/4/2026
        try {
            return LocalDate.parse(raw,
                    DateTimeFormatter.ofPattern("M/d/yyyy"));
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
}
