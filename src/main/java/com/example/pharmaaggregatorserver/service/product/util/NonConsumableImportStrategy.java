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

    // ── Valid GST percentages ─────────────────────────────────────────────
    private static final Set<Long> VALID_GST_VALUES = Set.of(0L, 5L, 12L, 18L);

    // ===== COLUMN INDEX (0-based) =========================================
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
    private static final int COL_DATE_OF_ENTRY = 28; // ignored — always LocalDate.now()
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
    private static final String H_DATE_OF_ENTRY = "Date of Entry*"; // ignored — always LocalDate.now()
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

    // =========================================================
    // ================= EXCEL ENTRY POINT =====================
    // =========================================================

    @Override
    public ProductDetailsDto mapRow(Row row, Long categoryId) {
        log.info("Non-Consumable Excel import Called");

        validateMandatoryExcel(row);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getString(row, COL_PRODUCT_NAME));
        dto.setWarningsPrecautions(getString(row, COL_SAFETY_PRECAUTIONS));
        dto.setProductDescription(getString(row, COL_PRODUCT_DESCRIPTION));
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));

        // ===== PACKAGING =====
        dto.setPackagingDetails(Set.of(buildPackaging(
                getLong(row, COL_UNIT_PER_PACK),
                getLong(row, COL_NUMBER_OF_PACKS),
                getLong(row, COL_MIN_ORDER_QTY),
                getLong(row, COL_MAX_ORDER_QTY),
                getString(row, COL_PACK_TYPE),
                categoryId
        )));

        Long mainDiscountPct = getLong(row, COL_DISCOUNT);
        Long mainMinOrderQty = getLong(row, COL_MIN_ORDER_QTY);
        Long mainMaxOrderQty = getLong(row, COL_MAX_ORDER_QTY);
        Set<Long> seenSlabMpqs = new HashSet<>();
        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            int base = COL_ADD_DISCOUNT_START + (slab * ADD_DISCOUNT_SLAB_SIZE);
            Long minQty = getLong(row, base + 1);
            Long discount = getLong(row, base + 2);

            if ((minQty == null || minQty == 0) && (discount == null || discount == 0)) continue;

            validateAdditionalDiscountSlab(
                    slab + 1, minQty, discount,
                    mainMinOrderQty, mainMaxOrderQty, mainDiscountPct,
                    getDate(row, base + 3), getDate(row, base + 5),
                    seenSlabMpqs);

            AdditionalDiscountDto d = new AdditionalDiscountDto();
            d.setMinimumPurchaseQuantity(minQty);
            d.setAdditionalDiscountPercentage(discount);
            d.setEffectiveStartDate(getDate(row, base + 3));
            d.setEffectiveStartTime(getTime(row, base + 4));
            d.setEffectiveEndDate(getDate(row, base + 5));
            d.setEffectiveEndTime(getTime(row, base + 6));
            additionalDiscounts.add(d);
        }

        dto.setPricingDetails(Set.of(buildPricing(
                null,
                toStart(getDate(row, COL_MFG_DATE)),
                null,
                null,
                getLong(row, COL_STOCK_QTY),
                LocalDate.now(),                    // Date of Stock Entry — always today
                getLong(row, COL_MRP),
                getLong(row, COL_SELLING_PRICE),
                mainDiscountPct,
                getLong(row, COL_GST),
                getLong(row, COL_HSN),
                null,                               // shelfLifeMonths — not in template
                additionalDiscounts
        )));

        dto.setProductAttributeNonConsumableMedicals(
                Set.of(buildNonConsumableAttr(row, categoryId))
        );

        return dto;
    }

    // =========================================================
    // ================= CSV ENTRY POINT =======================
    // =========================================================

    @Override
    public ProductDetailsDto mapCsv(CSVRecord r, Long categoryId) {
        log.info("Non-Consumable CSV import Called");

        validateMandatoryCsv(r);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getCsvString(r, H_PRODUCT_NAME));
        dto.setWarningsPrecautions(getCsvString(r, H_SAFETY_PRECAUTIONS));
        dto.setProductDescription(getCsvString(r, H_PRODUCT_DESCRIPTION));
        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));

        dto.setPackagingDetails(Set.of(buildPackaging(
                getCsvLong(r, H_UNIT_PER_PACK),
                getCsvLong(r, H_NUMBER_OF_PACKS),
                getCsvLong(r, H_MIN_ORDER_QTY),
                getCsvLong(r, H_MAX_ORDER_QTY),
                getCsvString(r, H_PACK_TYPE),
                categoryId
        )));

        Long mainDiscountPct = getCsvLong(r, H_DISCOUNT);
        Long mainMinOrderQty = getCsvLong(r, H_MIN_ORDER_QTY);
        Long mainMaxOrderQty = getCsvLong(r, H_MAX_ORDER_QTY);
        Set<Long> seenSlabMpqs = new HashSet<>();
        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            Long minQty = getCsvLongByIndex(r, CSV_SLAB_MIN_QTY_COLS[slab]);
            Long discount = getCsvLongByIndex(r, CSV_SLAB_DISCOUNT_COLS[slab]);

            if ((minQty == null || minQty == 0) && (discount == null || discount == 0)) continue;

            LocalDate slabStartDate = parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_START_DATE_COLS[slab]));
            LocalDate slabEndDate = parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_END_DATE_COLS[slab]));

            validateAdditionalDiscountSlab(
                    slab + 1, minQty, discount,
                    mainMinOrderQty, mainMaxOrderQty, mainDiscountPct,
                    slabStartDate, slabEndDate,
                    seenSlabMpqs);

            AdditionalDiscountDto d = new AdditionalDiscountDto();
            d.setMinimumPurchaseQuantity(minQty);
            d.setAdditionalDiscountPercentage(discount);
            d.setEffectiveStartDate(slabStartDate);
            d.setEffectiveStartTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_START_TIME_COLS[slab])));
            d.setEffectiveEndDate(slabEndDate);
            d.setEffectiveEndTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_END_TIME_COLS[slab])));
            additionalDiscounts.add(d);
        }

        dto.setPricingDetails(Set.of(buildPricing(
                null,
                toStart(parseCsvDate(getCsvString(r, H_MFG_DATE))),
                null,
                null,
                getCsvLong(r, H_STOCK_QTY),
                LocalDate.now(),                    // Date of Stock Entry — always today
                getCsvLong(r, H_MRP),
                getCsvLong(r, H_SELLING_PRICE),
                mainDiscountPct,
                getCsvLong(r, H_GST),
                getCsvLong(r, H_HSN),
                null,
                additionalDiscounts
        )));

        dto.setProductAttributeNonConsumableMedicals(
                Set.of(buildNonConsumableAttrFromCsv(r, categoryId))
        );

        return dto;
    }

    // =========================================================
    // ================= NON CONSUMABLE ATTR (Excel) ===========
    // =========================================================

    private ProductAttributeNonConsumableMedicalDto buildNonConsumableAttr(Row row, Long categoryId) {

        ProductAttributeNonConsumableMedicalDto dto =
                new ProductAttributeNonConsumableMedicalDto();

        // ===== DEVICE CATEGORY =====
        String deviceCat = getString(row, COL_DEVICE_CATEGORY);
        if (!isBlank(deviceCat)) {
            dto.setDeviceCategoryId(
                    deviceCategoryRepository.findByDeviceNameIgnoreCase(deviceCat)
                            .orElseThrow(() -> new RuntimeException(
                                    "Device category not found: " + deviceCat))
                            .getDeviceCatId()
            );
        }

        // ===== DEVICE SUB CATEGORY =====
        String deviceSubCat = getString(row, COL_DEVICE_SUBCATEGORY);
        if (!isBlank(deviceSubCat)) {
            dto.setDeviceSubCategoryId(
                    deviceSubCategoryRepository.findBySubCategoryNameIgnoreCase(deviceSubCat)
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
                        .findByCertificationNameIgnoreCaseAndCategory_CategoryId(trimmed, categoryId)
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
                        .findByMaterialTypeNameIgnoreCase(trimmed)
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
                    powerSourceRepository.findByPowerSourceNameIgnoreCase(power)
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
                    countryRepository.findByCountryNameIgnoreCase(country)
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
                    storageConditionRepository
                            .findByConditionNameIgnoreCaseAndCategory_CategoryId(storage, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Storage condition not found: " + storage))
                            .getStorageConditionId()
            );
        }

        dto.setBrochurePath("NOT_UPLOADED");
        return dto;
    }

    // =========================================================
    // ================= NON CONSUMABLE ATTR (CSV) =============
    // =========================================================

    private ProductAttributeNonConsumableMedicalDto buildNonConsumableAttrFromCsv(CSVRecord r, Long categoryId) {

        ProductAttributeNonConsumableMedicalDto dto =
                new ProductAttributeNonConsumableMedicalDto();

        String deviceCat = getCsvString(r, H_DEVICE_CATEGORY);
        if (!isBlank(deviceCat)) {
            dto.setDeviceCategoryId(
                    deviceCategoryRepository.findByDeviceNameIgnoreCase(deviceCat)
                            .orElseThrow(() -> new RuntimeException(
                                    "Device category not found: " + deviceCat))
                            .getDeviceCatId()
            );
        }

        String deviceSubCat = getCsvString(r, H_DEVICE_SUBCATEGORY);
        if (!isBlank(deviceSubCat)) {
            dto.setDeviceSubCategoryId(
                    deviceSubCategoryRepository.findBySubCategoryNameIgnoreCase(deviceSubCat)
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
                        .findByCertificationNameIgnoreCaseAndCategory_CategoryId(trimmed, categoryId)
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
                        .findByMaterialTypeNameIgnoreCase(trimmed)
                        .orElseThrow(() -> new RuntimeException(
                                "Material type not found: " + trimmed));
                ids.add(mt.getMaterialTypeId());
            }
            dto.setMaterialTypeIds(ids);
        }

        String power = getCsvString(r, H_POWER_SOURCE);
        if (!isBlank(power)) {
            dto.setPowerSourceId(
                    powerSourceRepository.findByPowerSourceNameIgnoreCase(power)
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
                    countryRepository.findByCountryNameIgnoreCase(country)
                            .orElseThrow(() -> new RuntimeException(
                                    "Country not found: " + country))
                            .getCountryId()
            );
        }

        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));

        String storage = getCsvString(r, H_STORAGE_CONDITION);
        if (!isBlank(storage)) {
            dto.setStorageConditionId(
                    storageConditionRepository
                            .findByConditionNameIgnoreCaseAndCategory_CategoryId(storage, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Storage condition not found: " + storage))
                            .getStorageConditionId()
            );
        }

        dto.setBrochurePath("NOT_UPLOADED");
        return dto;
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
                    packTypeRepository.findByPackTypeIgnoreCaseAndCategory_CategoryId(packType, categoryId)
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
    // ================= ADDITIONAL DISCOUNT SLAB VALIDATION ===
    // =========================================================

    /**
     * Validates a single additional discount slab.
     * Throws ValidationException immediately on the first bad slab so the
     * error message clearly identifies which slab (1-based) is at fault.
     */
    private void validateAdditionalDiscountSlab(
            int slabNumber,
            Long mpq, Long discountPct,
            Long mainMoq, Long mainMaxQty, Long mainDiscountPct,
            LocalDate startDate, LocalDate endDate,
            Set<Long> seenMpqs) {

        List<String> errors = new ArrayList<>();
        String prefix = "Additional Discount Slab " + slabNumber + ": ";

        // ── MPQ validations ───────────────────────────────────────────────
        if (mpq == null) {
            errors.add(prefix + "MPQ is required");
        } else {
            if (mpq <= 0) {
                errors.add(prefix + "MPQ must be greater than 0");
            }
            if (mainMoq != null && mpq <= mainMoq) {
                errors.add(prefix + "MPQ (" + mpq + ") must be greater than main Minimum Order Quantity (" + mainMoq + ")");
            }
            if (mainMaxQty != null && mpq > mainMaxQty) {
                errors.add(prefix + "MPQ (" + mpq + ") must be ≤ main Maximum Order Quantity (" + mainMaxQty + ")");
            }
            if (!seenMpqs.add(mpq)) {
                errors.add(prefix + "Duplicate MPQ value (" + mpq + ") — each slab must have a unique MPQ");
            }
        }

        // ── Discount % validations ────────────────────────────────────────
        if (discountPct == null) {
            if (mpq != null) {
                errors.add(prefix + "Discount % cannot be blank when MPQ is entered");
            }
        } else {
            if (discountPct <= 0 || discountPct > 100) {
                errors.add(prefix + "Discount % must be between 1 and 100");
            }
            if (mainDiscountPct != null && discountPct < mainDiscountPct) {
                errors.add(prefix + "Discount % (" + discountPct + ") must be ≥ main Discount % (" + mainDiscountPct + ")");
            }
        }

        // ── Effective date validations ────────────────────────────────────
        if (startDate == null) {
            errors.add(prefix + "Effective Start Date is mandatory for an additional discount slab");
        }
        if (endDate == null) {
            errors.add(prefix + "Effective End Date is mandatory for an additional discount slab");
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            errors.add(prefix + "Effective End Date cannot be before Effective Start Date");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    // =========================================================
    // ================= EXCEL VALIDATION ======================
    // =========================================================

    private void validateMandatoryExcel(Row row) {
        List<String> errors = new ArrayList<>();

        // ── Product Name ──────────────────────────────────────────────────
        String productName = getString(row, COL_PRODUCT_NAME);
        validateRequired(productName, "Product Name", errors);
        if (!isBlank(productName)) {
            if (productName.length() < 3) errors.add("Product Name must be at least 3 characters");
            if (productName.length() > 150) errors.add("Product Name must not exceed 150 characters");
        }

        // ── Device Category / Sub Category ────────────────────────────────
        validateRequired(getString(row, COL_DEVICE_CATEGORY), "Device Category", errors);
        validateRequired(getString(row, COL_DEVICE_SUBCATEGORY), "Device Sub Category", errors);

        // ── Brand Name ────────────────────────────────────────────────────
        String brandName = getString(row, COL_BRAND_NAME);
        validateRequired(brandName, "Brand Name", errors);
        if (!isBlank(brandName) && brandName.length() > 60) {
            errors.add("Brand Name must not exceed 60 characters");
        }

        // ── Model Name ────────────────────────────────────────────────────
        String modelName = getString(row, COL_MODEL_NAME);
        validateRequired(modelName, "Model Name", errors);
        if (!isBlank(modelName) && modelName.length() > 60) {
            errors.add("Model Name must not exceed 60 characters");
        }

        // ── Model Number ──────────────────────────────────────────────────
        String modelNumber = getString(row, COL_MODEL_NUMBER);
        validateRequired(modelNumber, "Model Number", errors);
        if (!isBlank(modelNumber) && modelNumber.length() > 60) {
            errors.add("Model Number must not exceed 60 characters");
        }

        // ── Device Classification ─────────────────────────────────────────
        validateRequired(getString(row, COL_DEVICE_CLASSIFICATION), "Device Classification", errors);

        // ── UDI (optional) ────────────────────────────────────────────────
        String udi = getString(row, COL_UDI);
        if (!isBlank(udi) && udi.length() > 60) {
            errors.add("UDI must not exceed 60 characters");
        }

        // ── Intended Use / Purpose ────────────────────────────────────────
        String purpose = getString(row, COL_PURPOSE);
        validateRequired(purpose, "Intended Use / Purpose", errors);
        if (!isBlank(purpose) && purpose.length() < 10) {
            errors.add("Intended Use / Purpose must be at least 10 characters");
        }

        // ── Key Features ──────────────────────────────────────────────────
        String keyFeatures = getString(row, COL_KEY_FEATURES);
        validateRequired(keyFeatures, "Key Features / Technical Specifications", errors);
        if (!isBlank(keyFeatures) && keyFeatures.length() < 10) {
            errors.add("Key Features / Technical Specifications must be at least 10 characters");
        }

        // ── Warnings / Precautions ────────────────────────────────────────
        String warnings = getString(row, COL_SAFETY_PRECAUTIONS);
        validateRequired(warnings, "Safety Instructions / Precautions", errors);
        if (!isBlank(warnings)) {
            if (warnings.length() < 10) errors.add("Safety Instructions / Precautions must be at least 10 characters");
            if (warnings.length() > 1000)
                errors.add("Safety Instructions / Precautions must not exceed 1000 characters");
        }

        // ── Certifications / Material ─────────────────────────────────────
        validateRequired(getString(row, COL_CERTIFICATIONS), "Certifications", errors);
        validateRequired(getString(row, COL_MATERIAL_TYPES), "Material Type", errors);

        // ── Warranty Period ───────────────────────────────────────────────
        String warrantyRaw = getString(row, COL_WARRANTY);
        validateRequired(warrantyRaw, "Warranty Period", errors);
        if (!isBlank(warrantyRaw)) {
            validateWarrantyPeriod(warrantyRaw, errors);
        }

        // ── Service Availability / Country ────────────────────────────────
        validateRequired(getString(row, COL_SERVICE_AVAILABILITY), "Service Availability", errors);
        validateRequired(getString(row, COL_COUNTRY), "Country", errors);

        // ── Manufacturer Name ─────────────────────────────────────────────
        String manufacturer = getString(row, COL_MANUFACTURER);
        validateRequired(manufacturer, "Manufacturer Name", errors);
        if (!isBlank(manufacturer) && manufacturer.length() > 100) {
            errors.add("Manufacturer Name must not exceed 100 characters");
        }

        // ── Product Description ───────────────────────────────────────────
        String description = getString(row, COL_PRODUCT_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (!isBlank(description) && description.length() > 1000) {
            errors.add("Product Description must not exceed 1000 characters");
        }

        // ── Pack Type ─────────────────────────────────────────────────────
        validateRequired(getString(row, COL_PACK_TYPE), "Pack Type", errors);

        // ── Unit Per Pack ─────────────────────────────────────────────────
        String unitPerPackRaw = getString(row, COL_UNIT_PER_PACK);
        Long unitPerPack = getLong(row, COL_UNIT_PER_PACK);
        validateRequired(unitPerPackRaw, "Number of Units per Pack Type", errors);
        if (!isBlank(unitPerPackRaw)) {
            if (unitPerPack == null) errors.add("Number of Units per Pack Type must be numeric");
            else if (unitPerPack <= 0) errors.add("Number of Units per Pack Type must be a positive value");
        }

        // ── Number of Packs ───────────────────────────────────────────────
        String numberOfPacksRaw = getString(row, COL_NUMBER_OF_PACKS);
        Long numberOfPacks = getLong(row, COL_NUMBER_OF_PACKS);
        validateRequired(numberOfPacksRaw, "Number of Packs", errors);
        if (!isBlank(numberOfPacksRaw)) {
            if (numberOfPacks == null) errors.add("Number of Packs must be numeric");
            else if (numberOfPacks <= 0) errors.add("Number of Packs must be a positive value");
        }

        // ── Min / Max Order Quantities ────────────────────────────────────
        String minOrderQtyRaw = getString(row, COL_MIN_ORDER_QTY);
        Long minOrderQty = getLong(row, COL_MIN_ORDER_QTY);
        validateRequired(minOrderQtyRaw, "Minimum Order Qty", errors);
        if (!isBlank(minOrderQtyRaw)) {
            if (minOrderQty == null) errors.add("Minimum Order Qty must be numeric");
            else if (minOrderQty <= 0) errors.add("Minimum Order Qty must be a positive value");
        }

        String maxOrderQtyRaw = getString(row, COL_MAX_ORDER_QTY);
        Long maxOrderQty = getLong(row, COL_MAX_ORDER_QTY);
        validateRequired(maxOrderQtyRaw, "Max Order Qty", errors);
        if (!isBlank(maxOrderQtyRaw)) {
            if (maxOrderQty == null) errors.add("Max Order Qty must be numeric");
            else if (maxOrderQty <= 0) errors.add("Max Order Qty must be a positive value");
        }

        if (minOrderQty != null && maxOrderQty != null
                && minOrderQty > 0 && maxOrderQty > 0
                && minOrderQty > maxOrderQty) {
            errors.add("Minimum Order Qty must be ≤ Maximum Order Qty");
        }

        // ── Manufacturing Date ────────────────────────────────────────────
        LocalDate mfgDate = getDate(row, COL_MFG_DATE);
        validateRequired(mfgDate, "Manufacturing Date", errors);
        if (mfgDate != null && YearMonth.from(mfgDate).isAfter(YearMonth.now())) {
            errors.add("Manufacturing Date cannot be a future month");
        }

        // ── Stock Quantity ────────────────────────────────────────────────
        String stockQtyRaw = getString(row, COL_STOCK_QTY);
        Long stockQty = getLong(row, COL_STOCK_QTY);
        validateRequired(stockQtyRaw, "Stock Quantity", errors);
        if (!isBlank(stockQtyRaw)) {
            if (stockQty == null) errors.add("Stock Quantity must be numeric");
            else if (stockQty <= 0) errors.add("Stock Quantity must be a positive value");
            else if (minOrderQty != null && stockQty < minOrderQty)
                errors.add("Stock Quantity (" + stockQty + ") must be ≥ Minimum Order Quantity (" + minOrderQty + ")");
        }

        // ── Date of Stock Entry — ignored, always today ───────────────────

        // ── MRP ───────────────────────────────────────────────────────────
        String mrpRaw = getString(row, COL_MRP);
        Long mrp = getLong(row, COL_MRP);
        validateRequired(mrpRaw, "MRP", errors);
        if (!isBlank(mrpRaw)) {
            if (mrp == null) errors.add("MRP must be numeric");
            else if (mrp <= 0) errors.add("MRP must be greater than 0");
        }

        // ── Selling Price ─────────────────────────────────────────────────
        String sellingPriceRaw = getString(row, COL_SELLING_PRICE);
        Long sellingPrice = getLong(row, COL_SELLING_PRICE);
        validateRequired(sellingPriceRaw, "Selling Price", errors);
        if (!isBlank(sellingPriceRaw)) {
            if (sellingPrice == null) errors.add("Selling Price must be numeric");
            else if (sellingPrice <= 0) errors.add("Selling Price must be greater than 0");
            else if (mrp != null && sellingPrice > mrp) errors.add("Selling Price cannot be greater than MRP");
        }

        // ── Discount % ────────────────────────────────────────────────────
        String discountRaw = getString(row, COL_DISCOUNT);
        Long discount = getLong(row, COL_DISCOUNT);
        if (!isBlank(discountRaw)) {
            if (discount == null) errors.add("Discount % must be numeric");
            else if (discount < 0 || discount > 100) errors.add("Discount % must be in the range 0–100");
        }

        // ── GST % ─────────────────────────────────────────────────────────
        String gstRaw = getString(row, COL_GST);
        Long gst = getLong(row, COL_GST);
        validateRequired(gstRaw, "GST %", errors);
        if (!isBlank(gstRaw)) {
            if (gst == null) errors.add("GST % must be numeric");
            else if (!VALID_GST_VALUES.contains(gst)) errors.add("GST % must be one of: 0, 5, 12, 18");
        }

        // ── HSN Code ──────────────────────────────────────────────────────
        String hsnRaw = getString(row, COL_HSN);
        Long hsn = getLong(row, COL_HSN);
        validateRequired(hsnRaw, "HSN Code", errors);
        if (!isBlank(hsnRaw)) {
            if (hsn == null) {
                errors.add("HSN Code must be numeric");
            } else {
                int digits = String.valueOf(hsn).length();
                if (digits != 4 && digits != 6 && digits != 8)
                    errors.add("HSN Code must be 4, 6, or 8 digits");
            }
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    // =========================================================
    // ================= CSV VALIDATION ========================
    // =========================================================

    private void validateMandatoryCsv(CSVRecord r) {
        List<String> errors = new ArrayList<>();

        // ── Product Name ──────────────────────────────────────────────────
        String productName = getCsvString(r, H_PRODUCT_NAME);
        validateRequired(productName, "Product Name", errors);
        if (!isBlank(productName)) {
            if (productName.length() < 3) errors.add("Product Name must be at least 3 characters");
            if (productName.length() > 150) errors.add("Product Name must not exceed 150 characters");
        }

        // ── Device Category / Sub Category ────────────────────────────────
        validateRequired(getCsvString(r, H_DEVICE_CATEGORY), "Device Category", errors);
        validateRequired(getCsvString(r, H_DEVICE_SUBCATEGORY), "Device Sub Category", errors);

        // ── Brand Name ────────────────────────────────────────────────────
        String brandName = getCsvString(r, H_BRAND_NAME);
        validateRequired(brandName, "Brand Name", errors);
        if (!isBlank(brandName) && brandName.length() > 60) {
            errors.add("Brand Name must not exceed 60 characters");
        }

        // ── Model Name ────────────────────────────────────────────────────
        String modelName = getCsvString(r, H_MODEL_NAME);
        validateRequired(modelName, "Model Name", errors);
        if (!isBlank(modelName) && modelName.length() > 60) {
            errors.add("Model Name must not exceed 60 characters");
        }

        // ── Model Number ──────────────────────────────────────────────────
        String modelNumber = getCsvString(r, H_MODEL_NUMBER);
        validateRequired(modelNumber, "Model Number", errors);
        if (!isBlank(modelNumber) && modelNumber.length() > 60) {
            errors.add("Model Number must not exceed 60 characters");
        }

        // ── Device Classification ─────────────────────────────────────────
        validateRequired(getCsvString(r, H_DEVICE_CLASSIFICATION), "Device Classification", errors);

        // ── UDI (optional) ────────────────────────────────────────────────
        String udi = getCsvString(r, H_UDI);
        if (!isBlank(udi) && udi.length() > 60) {
            errors.add("UDI must not exceed 60 characters");
        }

        // ── Intended Use / Purpose ────────────────────────────────────────
        String purpose = getCsvString(r, H_PURPOSE);
        validateRequired(purpose, "Intended Use / Purpose", errors);
        if (!isBlank(purpose) && purpose.length() < 10) {
            errors.add("Intended Use / Purpose must be at least 10 characters");
        }

        // ── Key Features ──────────────────────────────────────────────────
        String keyFeatures = getCsvString(r, H_KEY_FEATURES);
        validateRequired(keyFeatures, "Key Features / Technical Specifications", errors);
        if (!isBlank(keyFeatures) && keyFeatures.length() < 10) {
            errors.add("Key Features / Technical Specifications must be at least 10 characters");
        }

        // ── Warnings / Precautions ────────────────────────────────────────
        String warnings = getCsvString(r, H_SAFETY_PRECAUTIONS);
        validateRequired(warnings, "Safety Instructions / Precautions", errors);
        if (!isBlank(warnings)) {
            if (warnings.length() < 10) errors.add("Safety Instructions / Precautions must be at least 10 characters");
            if (warnings.length() > 1000)
                errors.add("Safety Instructions / Precautions must not exceed 1000 characters");
        }

        // ── Certifications / Material ─────────────────────────────────────
        validateRequired(getCsvString(r, H_CERTIFICATIONS), "Certifications", errors);
        validateRequired(getCsvString(r, H_MATERIAL_TYPES), "Material Type", errors);

        // ── Warranty Period ───────────────────────────────────────────────
        String warrantyRaw = getCsvString(r, H_WARRANTY);
        validateRequired(warrantyRaw, "Warranty Period", errors);
        if (!isBlank(warrantyRaw)) {
            validateWarrantyPeriod(warrantyRaw, errors);
        }

        // ── Service Availability / Country ────────────────────────────────
        validateRequired(getCsvString(r, H_SERVICE_AVAILABILITY), "Service Availability", errors);
        validateRequired(getCsvString(r, H_COUNTRY), "Country", errors);

        // ── Manufacturer Name ─────────────────────────────────────────────
        String manufacturer = getCsvString(r, H_MANUFACTURER);
        validateRequired(manufacturer, "Manufacturer Name", errors);
        if (!isBlank(manufacturer) && manufacturer.length() > 100) {
            errors.add("Manufacturer Name must not exceed 100 characters");
        }

        // ── Product Description ───────────────────────────────────────────
        String description = getCsvString(r, H_PRODUCT_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (!isBlank(description) && description.length() > 1000) {
            errors.add("Product Description must not exceed 1000 characters");
        }

        // ── Pack Type ─────────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_PACK_TYPE), "Pack Type", errors);

        // ── Unit Per Pack ─────────────────────────────────────────────────
        String unitPerPackRaw = getCsvString(r, H_UNIT_PER_PACK);
        Long unitPerPack = getCsvLong(r, H_UNIT_PER_PACK);
        validateRequired(unitPerPackRaw, "Number of Units per Pack Type", errors);
        if (!isBlank(unitPerPackRaw)) {
            if (unitPerPack == null) errors.add("Number of Units per Pack Type must be numeric");
            else if (unitPerPack <= 0) errors.add("Number of Units per Pack Type must be a positive value");
        }

        // ── Number of Packs ───────────────────────────────────────────────
        String numberOfPacksRaw = getCsvString(r, H_NUMBER_OF_PACKS);
        Long numberOfPacks = getCsvLong(r, H_NUMBER_OF_PACKS);
        validateRequired(numberOfPacksRaw, "Number of Packs", errors);
        if (!isBlank(numberOfPacksRaw)) {
            if (numberOfPacks == null) errors.add("Number of Packs must be numeric");
            else if (numberOfPacks <= 0) errors.add("Number of Packs must be a positive value");
        }

        // ── Min / Max Order Quantities ────────────────────────────────────
        String minOrderQtyRaw = getCsvString(r, H_MIN_ORDER_QTY);
        Long minOrderQty = getCsvLong(r, H_MIN_ORDER_QTY);
        validateRequired(minOrderQtyRaw, "Minimum Order Qty", errors);
        if (!isBlank(minOrderQtyRaw)) {
            if (minOrderQty == null) errors.add("Minimum Order Qty must be numeric");
            else if (minOrderQty <= 0) errors.add("Minimum Order Qty must be a positive value");
        }

        String maxOrderQtyRaw = getCsvString(r, H_MAX_ORDER_QTY);
        Long maxOrderQty = getCsvLong(r, H_MAX_ORDER_QTY);
        validateRequired(maxOrderQtyRaw, "Max Order Qty", errors);
        if (!isBlank(maxOrderQtyRaw)) {
            if (maxOrderQty == null) errors.add("Max Order Qty must be numeric");
            else if (maxOrderQty <= 0) errors.add("Max Order Qty must be a positive value");
        }

        if (minOrderQty != null && maxOrderQty != null
                && minOrderQty > 0 && maxOrderQty > 0
                && minOrderQty > maxOrderQty) {
            errors.add("Minimum Order Qty must be ≤ Maximum Order Qty");
        }

        // ── Manufacturing Date ────────────────────────────────────────────
        LocalDate mfgDate = parseCsvDate(getCsvString(r, H_MFG_DATE));
        validateRequired(mfgDate, "Manufacturing Date", errors);
        if (mfgDate != null && YearMonth.from(mfgDate).isAfter(YearMonth.now())) {
            errors.add("Manufacturing Date cannot be a future month");
        }

        // ── Stock Quantity ────────────────────────────────────────────────
        String stockQtyRaw = getCsvString(r, H_STOCK_QTY);
        Long stockQty = getCsvLong(r, H_STOCK_QTY);
        validateRequired(stockQtyRaw, "Stock Quantity", errors);
        if (!isBlank(stockQtyRaw)) {
            if (stockQty == null) errors.add("Stock Quantity must be numeric");
            else if (stockQty <= 0) errors.add("Stock Quantity must be a positive value");
            else if (minOrderQty != null && stockQty < minOrderQty)
                errors.add("Stock Quantity (" + stockQty + ") must be ≥ Minimum Order Quantity (" + minOrderQty + ")");
        }

        // ── Date of Stock Entry — ignored, always today ───────────────────

        // ── MRP ───────────────────────────────────────────────────────────
        String mrpRaw = getCsvString(r, H_MRP);
        Long mrp = getCsvLong(r, H_MRP);
        validateRequired(mrpRaw, "MRP", errors);
        if (!isBlank(mrpRaw)) {
            if (mrp == null) errors.add("MRP must be numeric");
            else if (mrp <= 0) errors.add("MRP must be greater than 0");
        }

        // ── Selling Price ─────────────────────────────────────────────────
        String sellingPriceRaw = getCsvString(r, H_SELLING_PRICE);
        Long sellingPrice = getCsvLong(r, H_SELLING_PRICE);
        validateRequired(sellingPriceRaw, "Selling Price", errors);
        if (!isBlank(sellingPriceRaw)) {
            if (sellingPrice == null) errors.add("Selling Price must be numeric");
            else if (sellingPrice <= 0) errors.add("Selling Price must be greater than 0");
            else if (mrp != null && sellingPrice > mrp) errors.add("Selling Price cannot be greater than MRP");
        }

        // ── Discount % ────────────────────────────────────────────────────
        String discountRaw = getCsvString(r, H_DISCOUNT);
        Long discount = getCsvLong(r, H_DISCOUNT);
        if (!isBlank(discountRaw)) {
            if (discount == null) errors.add("Discount % must be numeric");
            else if (discount < 0 || discount > 100) errors.add("Discount % must be in the range 0–100");
        }

        // ── GST % ─────────────────────────────────────────────────────────
        String gstRaw = getCsvString(r, H_GST);
        Long gst = getCsvLong(r, H_GST);
        validateRequired(gstRaw, "GST %", errors);
        if (!isBlank(gstRaw)) {
            if (gst == null) errors.add("GST % must be numeric");
            else if (!VALID_GST_VALUES.contains(gst)) errors.add("GST % must be one of: 0, 5, 12, 18");
        }

        // ── HSN Code ──────────────────────────────────────────────────────
        String hsnRaw = getCsvString(r, H_HSN);
        Long hsn = getCsvLong(r, H_HSN);
        validateRequired(hsnRaw, "HSN Code", errors);
        if (!isBlank(hsnRaw)) {
            if (hsn == null) {
                errors.add("HSN Code must be numeric");
            } else {
                int digits = String.valueOf(hsn).length();
                if (digits != 4 && digits != 6 && digits != 8)
                    errors.add("HSN Code must be 4, 6, or 8 digits");
            }
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    // =========================================================
    // ================= SHARED HELPERS ========================
    // =========================================================

    /**
     * Validates Warranty Period: must be a parseable integer in range 1–999.
     * Stored as-is (String); no DTO type change needed.
     */
    private void validateWarrantyPeriod(String raw, List<String> errors) {
        // Strip any accidental decimal part Excel might have added (e.g. "12.0")
        String cleaned = raw.contains(".") ? raw.substring(0, raw.indexOf('.')) : raw;
        try {
            int value = Integer.parseInt(cleaned.trim());
            if (value < 1 || value > 999) {
                errors.add("Warranty Period must be between 1 and 999 months");
            }
        } catch (NumberFormatException e) {
            errors.add("Warranty Period must be numeric (in months, max 3 digits)");
        }
    }

    private void validateRequired(Object value, String field, List<String> errors) {
        if (value == null) {
            errors.add(field + " is mandatory");
        } else if (value instanceof String && ((String) value).isBlank()) {
            errors.add(field + " is mandatory");
        }
    }

    // =========================================================
    // ================= EXCEL HELPERS =========================
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

    // =========================================================
    // ================= DATE UTILS ============================
    // =========================================================

    private LocalDateTime toStart(LocalDate d) {
        return d != null ? d.atStartOfDay() : null;
    }

    private LocalDateTime toEnd(LocalDate d) {
        return d != null ? d.atTime(23, 59, 59) : null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}