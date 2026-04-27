package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.NonConsumableMaterialType;
import com.example.pharmaaggregatorserver.exception.ValidationException;
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
@Component("NON_CONSUMABLE")
@RequiredArgsConstructor
public class NonConsumableImportStrategy implements ProductImportStrategy {

    private final DeviceCategoryRepository deviceCategoryRepository;
    private final DeviceSubCategoryRepository deviceSubCategoryRepository;
    private final CertificationRepository certificationRepository;
    private final NonConsumableMaterialTypeRepository materialTypeRepository;
    private final PowerSourceMasterRepository powerSourceRepository;
    private final CountryMasterRepository countryRepository;
    private final StorageConditionMasterRepository storageConditionRepository;
    private final PackTypeRepository packTypeRepository;

    // ===== COLUMN INDEX (0-based, verified against Excel template row 0) =====
    private static final int COL_DEVICE_CATEGORY = 0;
    private static final int COL_DEVICE_SUBCATEGORY = 1;
    private static final int COL_PRODUCT_NAME = 2;
    private static final int COL_BRAND_NAME = 3;
    private static final int COL_MODEL_NAME = 4;
    private static final int COL_MODEL_NUMBER = 5;
    private static final int COL_DEVICE_CLASSIFICATION = 6;
    private static final int COL_UDI = 7;
    private static final int COL_PURPOSE = 8;
    private static final int COL_KEY_FEATURES = 9;
    private static final int COL_SAFETY_PRECAUTIONS = 10; // "Safety Instructions / Precautions*"
    private static final int COL_CERTIFICATIONS = 11; // "Certifications / Compliance*"
    private static final int COL_MATERIAL_TYPES = 12; // "Material / Build Type*"
    private static final int COL_POWER_SOURCE = 13;
    private static final int COL_WARRANTY = 14; // "Warranty Period (in months)*"
    private static final int COL_SERVICE_AVAILABILITY = 15; // "AMC / Service Availability* (Yes/No)"
    private static final int COL_COUNTRY = 16; // "Country of Origin*"
    private static final int COL_MANUFACTURER = 17; // "Manufacture Name*"
    private static final int COL_PRODUCT_DESCRIPTION = 18; // "Product Description*"
    private static final int COL_STORAGE_CONDITION = 19; // "Storage Condition (if applicable)"

    // ===== PACKAGING =====
    private static final int COL_PACK_TYPE = 20;
    private static final int COL_UNIT_PER_PACK = 21;
    private static final int COL_NUMBER_OF_PACKS = 22;
    // col 23 = Pack Size (auto-calculated formula) — not read

    // ===== PRICING =====
    private static final int COL_MIN_ORDER_QTY = 24;
    private static final int COL_MAX_ORDER_QTY = 25;
    private static final int COL_MFG_DATE = 26; // "Manufacturing Date*"
    private static final int COL_STOCK_QTY = 27;
    private static final int COL_DATE_OF_ENTRY = 28;
    private static final int COL_MRP = 29;
    private static final int COL_SELLING_PRICE = 30;
    private static final int COL_DISCOUNT = 31;
    private static final int COL_GST = 32;
    private static final int COL_HSN = 33;

    // ===== ADDITIONAL DISCOUNT SLABS =====
    // 4 slabs × 7 cols each, starting at col 35
    // Layout per slab: [Slab label | MinQty | Discount% | StartDate | StartTime | EndDate | EndTime]
    private static final int COL_ADD_DISCOUNT_START = 34;
    private static final int ADD_DISCOUNT_SLAB_SIZE = 7;
    private static final int ADD_DISCOUNT_SLAB_COUNT = 4;

    // col 63 = Product Image URL* (not persisted by this strategy)

    // ===== CSV HEADER CONSTANTS =====
    private static final String H_DEVICE_CATEGORY = "Device Category*";
    private static final String H_DEVICE_SUBCATEGORY = "Device Sub Category*";
    private static final String H_PRODUCT_NAME = "Product Name*";
    private static final String H_BRAND_NAME = "Brand Name*";
    private static final String H_MODEL_NAME = "Model Name*";
    private static final String H_MODEL_NUMBER = "Model Number*";
    private static final String H_DEVICE_CLASSIFICATION = "Device Classification (Class A/B/C/D)*";
    private static final String H_UDI = "UDI (Unique Device Identification)/Serial Number";
    private static final String H_PURPOSE = "Intended Use / Purpose*";
    private static final String H_KEY_FEATURES = "Key Features / Technical Specifications*";
    private static final String H_SAFETY_PRECAUTIONS = "Safety Instructions  / Precautions*";
    private static final String H_CERTIFICATIONS = "Certifications / Compliance*";
    private static final String H_MATERIAL_TYPES = "Material / Build Type*";
    private static final String H_POWER_SOURCE = "Power Source";
    private static final String H_WARRANTY = "Warranty Period (in months)*";
    private static final String H_SERVICE_AVAILABILITY = "AMC / Service Availability* (Yes/No)";
    private static final String H_COUNTRY = "Country of Origin*";
    private static final String H_MANUFACTURER = "Manufacture Name*";
    private static final String H_PRODUCT_DESCRIPTION = "Product Description*";
    private static final String H_STORAGE_CONDITION = "Storage Condition (if applicable)";
    private static final String H_PACK_TYPE = "Pack Type";
    private static final String H_UNIT_PER_PACK = "Unit Per Pack";
    private static final String H_NUMBER_OF_PACKS = "Number Of Packs";
    private static final String H_MIN_ORDER_QTY = "Minimum Order Qty*";
    private static final String H_MAX_ORDER_QTY = "Max Order Qty*";
    private static final String H_MFG_DATE = "Manufacturing Date*";
    private static final String H_STOCK_QTY = "Stock Quantity*";
    private static final String H_DATE_OF_ENTRY = "Date of Entry*";
    private static final String H_MRP = "MRP (INR)*";
    private static final String H_SELLING_PRICE = "Selling Price(INR)*";
    private static final String H_DISCOUNT = "Discount %";
    private static final String H_GST = "GST %";
    private static final String H_HSN = "HSN Code*";

    // Additional discount slabs — duplicate headers in CSV make name-based access
// unreliable; use index-based access matching the Excel column layout.
// Layout per slab (0-based): base | base+1 MinQty | base+2 Disc% | base+3 StartDate
//                            | base+4 StartTime  | base+5 EndDate | base+6 EndTime
// Slab 0 base=34, Slab 1 base=41, Slab 2 base=48, Slab 3 base=55
    private static final int[] CSV_SLAB_MIN_QTY_COLS = {35, 42, 49, 56};
    private static final int[] CSV_SLAB_DISCOUNT_COLS = {36, 43, 50, 57};
    private static final int[] CSV_SLAB_START_DATE_COLS = {37, 44, 51, 58};
    private static final int[] CSV_SLAB_START_TIME_COLS = {38, 45, 52, 59};
    private static final int[] CSV_SLAB_END_DATE_COLS = {39, 46, 53, 60};
    private static final int[] CSV_SLAB_END_TIME_COLS = {40, 47, 54, 61};

    @Override
    public ProductDetailsDto mapRow(Row row, Long categoryId) {
        log.info("Non-Consumable Excel import Called");

        validateMandatoryExcel(row);

        ProductDetailsDto dto = new ProductDetailsDto();

        // ===== BASIC =====
        String productName = getString(row, COL_PRODUCT_NAME);
        if (isBlank(productName)) throw new RuntimeException("Product Name is mandatory");

        dto.setProductName(productName);
        dto.setWarningsPrecautions(getString(row, COL_SAFETY_PRECAUTIONS));
        dto.setProductDescription(getString(row, COL_PRODUCT_DESCRIPTION));
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));

        // ===== PACKAGING =====
//        dto.setPackagingDetails(buildPackaging(
//                getLong(row, COL_UNIT_PER_PACK),
//                getLong(row, COL_NUMBER_OF_PACKS),
//                getLong(row, COL_MIN_ORDER_QTY),
//                getLong(row, COL_MAX_ORDER_QTY),
//                getString(row, COL_PACK_TYPE),
//                categoryId
//        ));

        // ===== ADDITIONAL DISCOUNTS =====
        Set<AdditionalDiscountDto> additionalDiscounts = buildAdditionalDiscounts(row);

        // ===== PRICING =====
        // Note: this template has no Batch/Lot Number or Expiry Date or Shelf Life columns
        dto.setPricingDetails(Set.of(buildPricing(
                null,                                     // batchNumber — not in template
                toStart(getDate(row, COL_MFG_DATE)),
                null,                                     // expiryDate — not in template
                null,
                getLong(row, COL_STOCK_QTY),
                getDate(row, COL_DATE_OF_ENTRY),
                getLong(row, COL_MRP),
                getLong(row, COL_SELLING_PRICE),
                getLong(row, COL_DISCOUNT),
                getLong(row, COL_GST),
                getLong(row, COL_HSN),
                null,                                     // shelfLifeMonths — not in template
                additionalDiscounts
        )));

        // ===== ATTRIBUTES =====
        dto.setProductAttributeNonConsumableMedicals(
                Set.of(buildNonConsumableAttr(row))
        );

        return dto;
    }

    // =========================================================
    // ================= NON CONSUMABLE ATTR ===================
    // =========================================================

    private ProductAttributeNonConsumableMedicalDto buildNonConsumableAttr(Row row) {

        ProductAttributeNonConsumableMedicalDto dto =
                new ProductAttributeNonConsumableMedicalDto();

        // ===== DEVICE CATEGORY =====
        String deviceCat = getString(row, COL_DEVICE_CATEGORY);
        if (!isBlank(deviceCat)) {
            dto.setDeviceCategoryId(
                    deviceCategoryRepository.findByDeviceName(deviceCat)
                            .orElseThrow(() -> new RuntimeException(
                                    "Device category not found: " + deviceCat))
                            .getDeviceCatId()
            );
        }

        // ===== DEVICE SUB CATEGORY =====
        String deviceSubCat = getString(row, COL_DEVICE_SUBCATEGORY);
        if (!isBlank(deviceSubCat)) {
            dto.setDeviceSubCategoryId(
                    deviceSubCategoryRepository.findBySubCategoryName(deviceSubCat)
                            .orElseThrow(() -> new RuntimeException(
                                    "Device subcategory not found: " + deviceSubCat))
                            .getDeviceSubCatId()
            );
        }

        // ===== BASIC FIELDS =====
        dto.setBrandName(getString(row, COL_BRAND_NAME));
        dto.setModelName(getString(row, COL_MODEL_NAME));
        dto.setModelNumber(getString(row, COL_MODEL_NUMBER));
        dto.setDeviceClassification(getString(row, COL_DEVICE_CLASSIFICATION));
        dto.setUdiNumber(getString(row, COL_UDI));
        dto.setPurpose(getString(row, COL_PURPOSE));
        dto.setKeyFeaturesSpecifications(getString(row, COL_KEY_FEATURES));

        // ===== CERTIFICATIONS =====
        String certCell = getString(row, COL_CERTIFICATIONS);
        log.info("Certification Type: " + certCell);
        if (!isBlank(certCell)) {
            List<ProductCertificateDocumentDto> docs = new ArrayList<>();
            for (String name : certCell.split(",")) {
                String trimmed = name.trim();
                log.info("Trying certification: {}", trimmed);
                Certification cert = certificationRepository
                        .findByCertificationName(trimmed)
                        .orElseThrow(() -> new RuntimeException(
                                "Certification not found: " + trimmed));
                ProductCertificateDocumentDto d = new ProductCertificateDocumentDto();
                d.setCertificationId(cert.getCertificationId());
                d.setCertificateUrl("NOT_UPLOADED");
                docs.add(d);
            }
            dto.setCertificateDocuments(docs);
        }

        // ===== MATERIAL TYPES =====
        String materialCell = getString(row, COL_MATERIAL_TYPES);
        if (!isBlank(materialCell)) {
            List<Long> ids = new ArrayList<>();
            for (String m : materialCell.split(",")) {
                String trimmed = m.trim();
                NonConsumableMaterialType mt = materialTypeRepository
                        .findByMaterialTypeName(trimmed)
                        .orElseThrow(() -> new RuntimeException(
                                "Material type not found: " + trimmed));
                ids.add(mt.getMaterialTypeId());
            }
            dto.setMaterialTypeIds(ids);
        }

        // ===== POWER SOURCE =====
        String power = getString(row, COL_POWER_SOURCE);
        if (!isBlank(power)) {
            dto.setPowerSourceId(
                    powerSourceRepository.findByPowerSourceName(power)
                            .orElseThrow(() -> new RuntimeException(
                                    "Power source not found: " + power))
                            .getPowerSourceId()
            );
        }

        // ===== WARRANTY =====
        dto.setWarrantyPeriod(getString(row, COL_WARRANTY));

        // ===== SERVICE AVAILABILITY =====
        String service = getString(row, COL_SERVICE_AVAILABILITY);
        if (!isBlank(service)) {
            dto.setServiceAvailability("YES".equalsIgnoreCase(service));
        }

        // ===== COUNTRY =====
        String country = getString(row, COL_COUNTRY);
        if (!isBlank(country)) {
            dto.setCountryId(
                    countryRepository.findByCountryName(country)
                            .orElseThrow(() -> new RuntimeException(
                                    "Country not found: " + country))
                            .getCountryId()
            );
        }

        // ===== MANUFACTURER =====
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));

        // ===== STORAGE CONDITION (device attribute) =====
        String storage = getString(row, COL_STORAGE_CONDITION);
        if (!isBlank(storage)) {
            dto.setStorageConditionId(
                    storageConditionRepository.findByConditionName(storage)
                            .orElseThrow(() -> new RuntimeException(
                                    "Storage condition not found: " + storage))
                            .getStorageConditionId()
            );
        }

        // ===== BROCHURE =====
        dto.setBrochurePath("NOT_UPLOADED");

        return dto;
    }

    // =========================================================
    // ================= ADDITIONAL DISCOUNTS ==================
    // =========================================================

    private Set<AdditionalDiscountDto> buildAdditionalDiscounts(Row row) {

        Set<AdditionalDiscountDto> set = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            // base points to the "Slab label" cell; data starts at base+1
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

            set.add(d);
        }

        return set;
    }

    // =========================================================
    // ================= PACKAGING =============================
    // =========================================================

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

        if (!isBlank(packType)) {
            dto.setPackId(
                    packTypeRepository.findByPackTypeAndCategory_CategoryId(packType, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Pack type not found: " + packType))
                            .getPackId()
            );
        }

        return dto;
    }

    // =========================================================
    // ================= PRICING ===============================
    // =========================================================

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
//        pricing.setStorageCondition(storageCondition);
        pricing.setStockQuantity(stockQty);
        pricing.setDateOfStockEntry(dateOfEntry);
        pricing.setMrp(mrp);
        pricing.setSellingPrice(sellingPrice);
        pricing.setDiscountPercentage(discountPct);
        pricing.setGstPercentage(gstPct);
        pricing.setHsnCode(hsnCode);
        pricing.setShelfLifeMonths(shelfLifeMonths);

        if (additionalDiscounts != null && !additionalDiscounts.isEmpty()) {
            pricing.setAdditionalDiscounts(additionalDiscounts);
        }

        return pricing;
    }

    // =========================================================
// ================= CSV ENTRY POINT =======================
// =========================================================

    @Override
    public ProductDetailsDto mapCsv(CSVRecord r, Long categoryId) {
        log.info("Non-Consumable CSV import Called");

        validateMandatoryCsv(r);

        String certifications = getCsvString(r, H_CERTIFICATIONS);

        ProductDetailsDto dto = new ProductDetailsDto();

        String productName = getCsvString(r, H_PRODUCT_NAME);
        if (isBlank(productName)) throw new RuntimeException("Product Name is mandatory");

        dto.setProductName(productName);
        dto.setWarningsPrecautions(getCsvString(r, H_SAFETY_PRECAUTIONS));
        dto.setProductDescription(getCsvString(r, H_PRODUCT_DESCRIPTION));
        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));

//        dto.setPackagingDetails(buildPackaging(
//                getCsvLong(r, H_UNIT_PER_PACK),
//                getCsvLong(r, H_NUMBER_OF_PACKS),
//                getCsvLong(r, H_MIN_ORDER_QTY),
//                getCsvLong(r, H_MAX_ORDER_QTY),
//                getCsvString(r, H_PACK_TYPE),
//                categoryId
//        ));

        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();
        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            Long minQty = getCsvLongByIndex(r, CSV_SLAB_MIN_QTY_COLS[slab]);
            Long discount = getCsvLongByIndex(r, CSV_SLAB_DISCOUNT_COLS[slab]);
            if ((minQty == null || minQty == 0) && (discount == null || discount == 0)) continue;

            AdditionalDiscountDto d = new AdditionalDiscountDto();
            d.setMinimumPurchaseQuantity(minQty);
            d.setAdditionalDiscountPercentage(discount);
            d.setEffectiveStartDate(parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_START_DATE_COLS[slab])));
            d.setEffectiveStartTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_START_TIME_COLS[slab])));
            d.setEffectiveEndDate(parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_END_DATE_COLS[slab])));
            d.setEffectiveEndTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_END_TIME_COLS[slab])));
            additionalDiscounts.add(d);
        }

        dto.setPricingDetails(Set.of(buildPricing(
                null,
                toStart(parseCsvDate(getCsvString(r, H_MFG_DATE))),
                null,
                null,
                getCsvLong(r, H_STOCK_QTY),
                parseCsvDate(getCsvString(r, H_DATE_OF_ENTRY)),
                getCsvLong(r, H_MRP),
                getCsvLong(r, H_SELLING_PRICE),
                getCsvLong(r, H_DISCOUNT),
                getCsvLong(r, H_GST),
                getCsvLong(r, H_HSN),
                null,
                additionalDiscounts
        )));

        dto.setProductAttributeNonConsumableMedicals(
                Set.of(buildNonConsumableAttrFromCsv(r))
        );

        return dto;
    }

    private ProductAttributeNonConsumableMedicalDto buildNonConsumableAttrFromCsv(CSVRecord r) {

        ProductAttributeNonConsumableMedicalDto dto =
                new ProductAttributeNonConsumableMedicalDto();

        String deviceCat = getCsvString(r, H_DEVICE_CATEGORY);
        if (!isBlank(deviceCat)) {
            dto.setDeviceCategoryId(
                    deviceCategoryRepository.findByDeviceName(deviceCat)
                            .orElseThrow(() -> new RuntimeException(
                                    "Device category not found: " + deviceCat))
                            .getDeviceCatId()
            );
        }

        String deviceSubCat = getCsvString(r, H_DEVICE_SUBCATEGORY);
        if (!isBlank(deviceSubCat)) {
            dto.setDeviceSubCategoryId(
                    deviceSubCategoryRepository.findBySubCategoryName(deviceSubCat)
                            .orElseThrow(() -> new RuntimeException(
                                    "Device subcategory not found: " + deviceSubCat))
                            .getDeviceSubCatId()
            );
        }

        dto.setBrandName(getCsvString(r, H_BRAND_NAME));
        dto.setModelName(getCsvString(r, H_MODEL_NAME));
        dto.setModelNumber(getCsvString(r, H_MODEL_NUMBER));
        dto.setDeviceClassification(getCsvString(r, H_DEVICE_CLASSIFICATION));
        dto.setUdiNumber(getCsvString(r, H_UDI));
        dto.setPurpose(getCsvString(r, H_PURPOSE));
        dto.setKeyFeaturesSpecifications(getCsvString(r, H_KEY_FEATURES));

        String certCell = getCsvString(r, H_CERTIFICATIONS);
        if (!isBlank(certCell)) {
            List<ProductCertificateDocumentDto> docs = new ArrayList<>();
            for (String name : certCell.split(",")) {
                String trimmed = name.trim();
                Certification cert = certificationRepository
                        .findByCertificationName(trimmed)
                        .orElseThrow(() -> new RuntimeException(
                                "Certification not found: " + trimmed));
                ProductCertificateDocumentDto d = new ProductCertificateDocumentDto();
                d.setCertificationId(cert.getCertificationId());
                d.setCertificateUrl("NOT_UPLOADED");
                docs.add(d);
            }
            dto.setCertificateDocuments(docs);
        }

        String materialCell = getCsvString(r, H_MATERIAL_TYPES);
        if (!isBlank(materialCell)) {
            List<Long> ids = new ArrayList<>();
            for (String m : materialCell.split(",")) {
                String trimmed = m.trim();
                NonConsumableMaterialType mt = materialTypeRepository
                        .findByMaterialTypeName(trimmed)
                        .orElseThrow(() -> new RuntimeException(
                                "Material type not found: " + trimmed));
                ids.add(mt.getMaterialTypeId());
            }
            dto.setMaterialTypeIds(ids);
        }

        String power = getCsvString(r, H_POWER_SOURCE);
        if (!isBlank(power)) {
            dto.setPowerSourceId(
                    powerSourceRepository.findByPowerSourceName(power)
                            .orElseThrow(() -> new RuntimeException(
                                    "Power source not found: " + power))
                            .getPowerSourceId()
            );
        }

        dto.setWarrantyPeriod(getCsvString(r, H_WARRANTY));

        String service = getCsvString(r, H_SERVICE_AVAILABILITY);
        if (!isBlank(service)) {
            dto.setServiceAvailability("YES".equalsIgnoreCase(service));
        }

        String country = getCsvString(r, H_COUNTRY);
        if (!isBlank(country)) {
            dto.setCountryId(
                    countryRepository.findByCountryName(country)
                            .orElseThrow(() -> new RuntimeException(
                                    "Country not found: " + country))
                            .getCountryId()
            );
        }

        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));

        String storage = getCsvString(r, H_STORAGE_CONDITION);
        if (!isBlank(storage)) {
            dto.setStorageConditionId(
                    storageConditionRepository.findByConditionName(storage)
                            .orElseThrow(() -> new RuntimeException(
                                    "Storage condition not found: " + storage))
                            .getStorageConditionId()
            );
        }

        dto.setBrochurePath("NOT_UPLOADED");
        return dto;
    }

// =========================================================
// ================= CSV HELPERS ===========================
// =========================================================

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

    private LocalDate parseCsvDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        raw = raw.trim();
        try {
            return LocalDate.parse(raw);
        } catch (Exception ignored) {
        }
        try {
            return YearMonth.parse(raw,
                    DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH)).atDay(1);
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(raw,
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception ignored) {
        }
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

    // =========================================================
    // ================= HELPERS ===============================
    // =========================================================

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell != null ? cell.toString().trim() : null;
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

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void validateMandatoryExcel(Row row) {

        List<String> errors = new ArrayList<>();

        // ===== PRODUCT DETAILS =====
        validateRequired(getString(row, COL_PRODUCT_NAME), "Product Name", errors);
        validateRequired(getString(row, COL_DEVICE_CATEGORY), "Device Category", errors);
        validateRequired(getString(row, COL_DEVICE_SUBCATEGORY), "Device Sub Category", errors);
        validateRequired(getString(row, COL_BRAND_NAME), "Brand Name", errors);
        validateRequired(getString(row, COL_MODEL_NAME), "Model Name", errors);
        validateRequired(getString(row, COL_MODEL_NUMBER), "Model Number", errors);
        validateRequired(getString(row, COL_DEVICE_CLASSIFICATION), "Device Classification", errors);
        validateRequired(getString(row, COL_PURPOSE), "Intended Use / Purpose", errors);
        validateRequired(getString(row, COL_KEY_FEATURES), "Key Features", errors);
        validateRequired(getString(row, COL_SAFETY_PRECAUTIONS), "Safety Instructions", errors);
        validateRequired(getString(row, COL_CERTIFICATIONS), "Certifications", errors);
        validateRequired(getString(row, COL_MATERIAL_TYPES), "Material Type", errors);
        validateRequired(getString(row, COL_WARRANTY), "Warranty Period", errors);
        validateRequired(getString(row, COL_SERVICE_AVAILABILITY), "Service Availability", errors);
        validateRequired(getString(row, COL_COUNTRY), "Country", errors);
        validateRequired(getString(row, COL_MANUFACTURER), "Manufacturer Name", errors);
        validateRequired(getString(row, COL_PRODUCT_DESCRIPTION), "Product Description", errors);

        // ===== PACKAGING =====
        validateRequired(getLong(row, COL_UNIT_PER_PACK), "Unit Per Pack", errors);
        validateRequired(getLong(row, COL_NUMBER_OF_PACKS), "Number Of Packs", errors);
        validateRequired(getLong(row, COL_MIN_ORDER_QTY), "Minimum Order Qty", errors);
        validateRequired(getLong(row, COL_MAX_ORDER_QTY), "Max Order Qty", errors);

        // ===== PRICING =====
        validateRequired(getDate(row, COL_MFG_DATE), "Manufacturing Date", errors);
        validateRequired(getLong(row, COL_STOCK_QTY), "Stock Quantity", errors);
        validateRequired(getDate(row, COL_DATE_OF_ENTRY), "Date of Entry", errors);
        validateRequired(getLong(row, COL_SELLING_PRICE), "Selling Price", errors);
        validateRequired(getLong(row, COL_MRP), "MRP", errors);
        validateRequired(getLong(row, COL_GST), "GST", errors);
        validateRequired(getLong(row, COL_HSN), "HSN Code", errors);

        // ===== BUSINESS RULES =====
        Long mrp = getLong(row, COL_MRP);
        Long sellingPrice = getLong(row, COL_SELLING_PRICE);

        if (mrp != null && mrp <= 0) {
            errors.add("MRP must be greater than 0");
        }

        if (sellingPrice != null && mrp != null && sellingPrice > mrp) {
            errors.add("Selling Price cannot be greater than MRP");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void validateMandatoryCsv(CSVRecord r) {

        List<String> errors = new ArrayList<>();

        // ===== PRODUCT DETAILS =====
        validateRequired(getCsvString(r, H_PRODUCT_NAME), "Product Name", errors);
        validateRequired(getCsvString(r, H_DEVICE_CATEGORY), "Device Category", errors);
        validateRequired(getCsvString(r, H_DEVICE_SUBCATEGORY), "Device Sub Category", errors);
        validateRequired(getCsvString(r, H_BRAND_NAME), "Brand Name", errors);
        validateRequired(getCsvString(r, H_MODEL_NAME), "Model Name", errors);
        validateRequired(getCsvString(r, H_MODEL_NUMBER), "Model Number", errors);
        validateRequired(getCsvString(r, H_DEVICE_CLASSIFICATION), "Device Classification", errors);
        validateRequired(getCsvString(r, H_PURPOSE), "Intended Use / Purpose", errors);
        validateRequired(getCsvString(r, H_KEY_FEATURES), "Key Features", errors);
        validateRequired(getCsvString(r, H_SAFETY_PRECAUTIONS), "Safety Instructions", errors);
        validateRequired(getCsvString(r, H_CERTIFICATIONS), "Certifications", errors);
        validateRequired(getCsvString(r, H_MATERIAL_TYPES), "Material Type", errors);
        validateRequired(getCsvString(r, H_WARRANTY), "Warranty Period", errors);
        validateRequired(getCsvString(r, H_SERVICE_AVAILABILITY), "Service Availability", errors);
        validateRequired(getCsvString(r, H_COUNTRY), "Country", errors);
        validateRequired(getCsvString(r, H_MANUFACTURER), "Manufacturer Name", errors);
        validateRequired(getCsvString(r, H_PRODUCT_DESCRIPTION), "Product Description", errors);

        // ===== PACKAGING =====
        validateRequired(getCsvLong(r, H_UNIT_PER_PACK), "Unit Per Pack", errors);
        validateRequired(getCsvLong(r, H_NUMBER_OF_PACKS), "Number Of Packs", errors);
        validateRequired(getCsvLong(r, H_MIN_ORDER_QTY), "Minimum Order Qty", errors);
        validateRequired(getCsvLong(r, H_MAX_ORDER_QTY), "Max Order Qty", errors);

        // ===== PRICING =====
        validateRequired(parseCsvDate(getCsvString(r, H_MFG_DATE)), "Manufacturing Date", errors);
        validateRequired(getCsvLong(r, H_STOCK_QTY), "Stock Quantity", errors);
        validateRequired(parseCsvDate(getCsvString(r, H_DATE_OF_ENTRY)), "Date of Entry", errors);
        validateRequired(getCsvLong(r, H_SELLING_PRICE), "Selling Price", errors);
        validateRequired(getCsvLong(r, H_MRP), "MRP", errors);
        validateRequired(getCsvLong(r, H_GST), "GST", errors);
        validateRequired(getCsvLong(r, H_HSN), "HSN Code", errors);

        // ===== BUSINESS RULES =====
        Long mrp = getCsvLong(r, H_MRP);
        Long sellingPrice = getCsvLong(r, H_SELLING_PRICE);

        if (mrp != null && mrp <= 0) {
            errors.add("MRP must be greater than 0");
        }

        if (sellingPrice != null && mrp != null && sellingPrice > mrp) {
            errors.add("Selling Price cannot be greater than MRP");
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