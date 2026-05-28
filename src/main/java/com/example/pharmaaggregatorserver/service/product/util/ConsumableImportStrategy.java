package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.exception.ValidationException;
import com.example.pharmaaggregatorserver.repository.product.*;
import com.example.pharmaaggregatorserver.service.product.PricingDetailsService;
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
    private final DeviceSpecificationUnitRepository deviceSpecificationUnitRepository;
    private final PricingDetailsService pricingDetailsService;

    // ── Valid GST percentages ─────────────────────────────────────────────
    private static final Set<Long> VALID_GST_VALUES = Set.of(0L, 5L, 12L, 18L);

    // ===== COLUMN INDEX (0-based) =========================================
    private static final int COL_DEVICE_CATEGORY            = 0;
    private static final int COL_DEVICE_SUBCATEGORY         = 1;
    private static final int COL_PRODUCT_NAME               = 2;
    private static final int COL_BRAND_NAME                 = 3;

    // ===== MATERIAL / PRODUCT DETAILS =====
    private static final int COL_DIMENSION_SIZE             = 4;
    private static final int COL_DEV_SPECIFICATION_UNIT_NAME = 5;  // FIX: was missing value + compile error
    private static final int COL_STERILE                    = 6;   // shifted by 1
    private static final int COL_DISPOSABLE                 = 7;   // shifted by 1

    // ===== DESCRIPTION =====
    private static final int COL_PURPOSE                    = 8;
    private static final int COL_KEY_FEATURES               = 9;
    private static final int COL_SAFETY_INSTRUCTIONS        = 10;
    private static final int COL_CERTIFICATIONS             = 11;

    // ===== MATERIAL =====
    private static final int COL_MATERIAL_TYPES             = 12;

    // ===== MANUFACTURING =====
    private static final int COL_COUNTRY                    = 13;
    private static final int COL_MANUFACTURER               = 14;
    private static final int COL_PRODUCT_DESCRIPTION        = 15;
    private static final int COL_STORAGE_CONDITION          = 16;

    // ===== PACKAGING =====
    private static final int COL_PACK_TYPE                  = 17;
    private static final int COL_UNIT_PER_PACK              = 18;
    private static final int COL_NUMBER_OF_PACKS            = 19;

    // col 20 = Pack Size (Auto-calculated)
    private static final int COL_MIN_ORDER_QTY              = 21;
    private static final int COL_MAX_ORDER_QTY              = 22;
    private static final int COL_BATCH_NUMBER               = 23;
    private static final int COL_MANUFACTURING_DATE         = 24;
    private static final int COL_EXPIRY_DATE                = 25;
    private static final int COL_STOCK_QUANTITY             = 26;
    private static final int COL_DATE_OF_ENTRY              = 27; // ignored — always LocalDate.now()

    // ===== PRICING =====
    private static final int COL_MRP                        = 28;
    private static final int COL_SELLING_PRICE              = 29;
    private static final int COL_DISCOUNT                   = 30;
    private static final int COL_GST                        = 31;
    private static final int COL_HSN                        = 32;

    // ===== ADDITIONAL DISCOUNT =====
    private static final int COL_ADD_DISCOUNT_START         = 33;
    private static final int ADD_DISCOUNT_SLAB_SIZE         = 7;
    private static final int ADD_DISCOUNT_SLAB_COUNT        = 4;

    // ===== CSV HEADER CONSTANTS =====
    // ===== BASIC =====
    private static final String H_DEVICE_CATEGORY           = "Device Category*";
    private static final String H_DEVICE_SUBCATEGORY        = "Device Sub Category*";
    private static final String H_PRODUCT_NAME              = "Product Name*";
    private static final String H_BRAND_NAME                = "Brand Name*";

    // ===== MATERIAL =====
    private static final String H_MATERIAL_TYPES            = "Material / Build Type*";
    private static final String H_DIMENSION_SIZE            = "Size / Dimension / Gauge*";
    private static final String H_DEV_SPECIFICATION_UNIT_NAME = "Device Specification Unit Name*";
    private static final String H_STERILE                   = "Sterile / Non-Sterile*";
    private static final String H_DISPOSABLE                = "Disposable / Reusable*";

    // ===== DESCRIPTION =====
    private static final String H_PURPOSE                   = "Intended Use / Purpose*";
    private static final String H_KEY_FEATURES              = "Key Features / Technical Specifications*";
    private static final String H_SAFETY_INSTRUCTIONS       = "Safety Instructions  / Precautions*";
    private static final String H_CERTIFICATIONS            = "Certifications / Compliance*";

    // ===== MANUFACTURING =====
    private static final String H_COUNTRY                   = "Country of Origin*";
    private static final String H_MANUFACTURER              = "Manufacture Name*";
    private static final String H_PRODUCT_DESCRIPTION       = "Product Description*";
    private static final String H_STORAGE_CONDITION         = "Storage Condition (if applicable)";

    // ===== PACKAGING =====
    private static final String H_PACK_TYPE                 = "Pack Type";
    private static final String H_UNIT_PER_PACK             = "Unit Per Pack";
    private static final String H_NUMBER_OF_PACKS           = "Number Of Packs";

    // ===== NEW COLUMNS =====
    private static final String H_MIN_ORDER_QTY             = "Minimum Order Qty*";
    private static final String H_MAX_ORDER_QTY             = "Max Order Qty*";
    private static final String H_BATCH_NUMBER              = "Batch Number*";
    private static final String H_MANUFACTURING_DATE        = "Manufacturing Date*";
    private static final String H_EXPIRY_DATE               = "Expiry Date*";
    private static final String H_STOCK_QUANTITY            = "Stock Quantity*";
    private static final String H_DATE_OF_ENTRY             = "Date of Entry*"; // ignored — always LocalDate.now()

    // ===== PRICING =====
    private static final String H_MRP                       = "MRP (INR)*";
    private static final String H_SELLING_PRICE             = "Selling Price(INR)*";
    private static final String H_DISCOUNT                  = "Discount %";
    private static final String H_GST                       = "GST %";
    private static final String H_HSN                       = "HSN Code*";

    // Each slab block = 7 cols: [label | minQty | disc% | startDate | startTime | endDate | endTime]
    // Slab label cols: 33, 40, 47, 54 (sub-header row 1) — shifted by 1 for the new unit column
    private static final int[] CSV_SLAB_MIN_QTY_COLS    = {34, 41, 48, 55};
    private static final int[] CSV_SLAB_DISCOUNT_COLS   = {35, 42, 49, 56};
    private static final int[] CSV_SLAB_START_DATE_COLS = {36, 43, 50, 57};
    private static final int[] CSV_SLAB_START_TIME_COLS = {37, 44, 51, 58};
    private static final int[] CSV_SLAB_END_DATE_COLS   = {38, 45, 52, 59};
    private static final int[] CSV_SLAB_END_TIME_COLS   = {39, 46, 53, 60};

    // =========================================================
    // ================= EXCEL ENTRY POINT =====================
    // =========================================================

    @Override
    public ProductDetailsDto mapRow(Row row, Long categoryId, Long userId) {
        log.info("Consumable Excel import Called");

        validateMandatoryExcel(row, userId);

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

        Long mainDiscountPct  = getLong(row, COL_DISCOUNT);
        Long mainMinOrderQty  = getLong(row, COL_MIN_ORDER_QTY);
        Long mainMaxOrderQty  = getLong(row, COL_MAX_ORDER_QTY);
        Set<Long> seenSlabMpqs = new HashSet<>();
        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            int base     = COL_ADD_DISCOUNT_START + (slab * ADD_DISCOUNT_SLAB_SIZE);
            Long minQty  = getLong(row, base + 1);
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

        LocalDate mfgDate    = getDate(row, COL_MANUFACTURING_DATE);
        LocalDate expiryDate = getDate(row, COL_EXPIRY_DATE);
        Long shelfLifeMonths = calculateShelfLifeMonths(mfgDate, expiryDate);

        dto.setPricingDetails(Set.of(buildPricing(
                getString(row, COL_BATCH_NUMBER),
                toStart(mfgDate),
                toEnd(expiryDate),
                null,
                getLong(row, COL_STOCK_QUANTITY),
                LocalDate.now(),
                getLong(row, COL_MRP),
                getLong(row, COL_SELLING_PRICE),
                mainDiscountPct,
                getLong(row, COL_GST),
                getLong(row, COL_HSN),
                shelfLifeMonths,
                additionalDiscounts
        )));

        dto.setProductAttributeConsumableMedicals(
                Set.of(buildConsumableAttr(row, categoryId))
        );

        return dto;
    }

    // =========================================================
    // ================= CSV ENTRY POINT =======================
    // =========================================================

    @Override
    public ProductDetailsDto mapCsv(CSVRecord r, Long categoryId, Long userId) {
        log.info("Consumable CSV import Called");

        validateMandatoryCsv(r, userId);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getCsvString(r, H_PRODUCT_NAME));
        dto.setProductDescription(getCsvString(r, H_PRODUCT_DESCRIPTION));
        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));
        dto.setWarningsPrecautions(getCsvString(r, H_SAFETY_INSTRUCTIONS));

        dto.setPackagingDetails(Set.of(buildPackaging(
                getCsvLong(r, H_UNIT_PER_PACK),
                getCsvLong(r, H_NUMBER_OF_PACKS),
                getCsvLong(r, H_MIN_ORDER_QTY),
                getCsvLong(r, H_MAX_ORDER_QTY),
                getCsvString(r, H_PACK_TYPE),
                categoryId
        )));

        Long mainDiscountPct  = getCsvLong(r, H_DISCOUNT);
        Long mainMinOrderQty  = getCsvLong(r, H_MIN_ORDER_QTY);
        Long mainMaxOrderQty  = getCsvLong(r, H_MAX_ORDER_QTY);
        Set<Long> seenSlabMpqs = new HashSet<>();
        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            Long minQty  = getCsvLongByIndex(r, CSV_SLAB_MIN_QTY_COLS[slab]);
            Long discount = getCsvLongByIndex(r, CSV_SLAB_DISCOUNT_COLS[slab]);

            if ((minQty == null || minQty == 0) && (discount == null || discount == 0)) continue;

            LocalDate slabStartDate = parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_START_DATE_COLS[slab]));
            LocalDate slabEndDate   = parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_END_DATE_COLS[slab]));

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

        LocalDate mfgDate    = parseCsvDate(getCsvString(r, H_MANUFACTURING_DATE));
        LocalDate expiryDate = parseCsvDate(getCsvString(r, H_EXPIRY_DATE));
        Long shelfLifeMonths = calculateShelfLifeMonths(mfgDate, expiryDate);

        dto.setPricingDetails(Set.of(buildPricing(
                getCsvString(r, H_BATCH_NUMBER),
                toStart(mfgDate),
                toEnd(expiryDate),
                null,
                getCsvLong(r, H_STOCK_QUANTITY),
                LocalDate.now(),
                getCsvLong(r, H_MRP),
                getCsvLong(r, H_SELLING_PRICE),
                mainDiscountPct,
                getCsvLong(r, H_GST),
                getCsvLong(r, H_HSN),
                shelfLifeMonths,
                additionalDiscounts
        )));

        dto.setProductAttributeConsumableMedicals(
                Set.of(buildConsumableAttrFromCsv(r, categoryId))
        );

        return dto;
    }

    // =========================================================
    // ================= CONSUMABLE ATTR (Excel) ===============
    // =========================================================

    private ConsumableProductAttributeDTO buildConsumableAttr(Row row, Long categoryId) {

        ConsumableProductAttributeDTO dto = new ConsumableProductAttributeDTO();

        dto.setDeviceCatId(
                deviceCategoryRepository.findByDeviceNameIgnoreCase(getString(row, COL_DEVICE_CATEGORY))
                        .orElseThrow(() -> new RuntimeException("Device category not found"))
                        .getDeviceCatId()
        );

        dto.setDeviceSubCatId(
                deviceSubCategoryRepository.findBySubCategoryNameIgnoreCase(getString(row, COL_DEVICE_SUBCATEGORY))
                        .orElseThrow(() -> new RuntimeException("Device subcategory not found"))
                        .getDeviceSubCatId()
        );

        dto.setBrandName(getString(row, COL_BRAND_NAME));

        // FIX: was commented out — now consistently set for both Excel and CSV paths
        dto.setDimensionSize(getDouble(row, COL_DIMENSION_SIZE));

        // Look up DeviceSpecificationUnit by sub-category + unitName
        String devSpecUnitName = getString(row, COL_DEV_SPECIFICATION_UNIT_NAME);
        if (devSpecUnitName != null && !devSpecUnitName.isBlank()) {
            dto.setDeviceSpecificationUnitId(
                    deviceSpecificationUnitRepository
                            .findByDeviceSubCategory_DeviceSubCatIdAndUnitNameIgnoreCase(
                                    dto.getDeviceSubCatId(), devSpecUnitName)
                            .orElseThrow(() -> new RuntimeException("Device Specification Unit not found: " + devSpecUnitName))
                            .getUnitId()
            );
        }

        dto.setSterileOrNonSterile(getString(row, COL_STERILE));
        dto.setDisposalOrReusable(getString(row, COL_DISPOSABLE));
        dto.setPurpose(getString(row, COL_PURPOSE));
        dto.setKeyFeaturesSpecifications(getString(row, COL_KEY_FEATURES));
        dto.setSafetyInstructions(getString(row, COL_SAFETY_INSTRUCTIONS));

        Long shelfLifeMonths = calculateShelfLifeMonths(
                getDate(row, COL_MANUFACTURING_DATE),
                getDate(row, COL_EXPIRY_DATE)
        );
        dto.setShelfLife(shelfLifeMonths != null ? shelfLifeMonths.toString() : null);

        String certCell = getString(row, COL_CERTIFICATIONS);
        if (certCell != null && !certCell.isBlank()) {
            List<ProductCertificateDocumentDto> docs = new ArrayList<>();
            for (String c : certCell.split(",")) {
                Certification cert = certificationRepository
                        .findByCertificationNameIgnoreCaseAndCategory_CategoryId(c.trim(), categoryId)
                        .orElseThrow(() -> new RuntimeException("Certification not found: " + c));
                ProductCertificateDocumentDto d = new ProductCertificateDocumentDto();
                d.setCertificationId(cert.getCertificationId());
                d.setCertificateUrl("NOT_UPLOADED");
                docs.add(d);
            }
            dto.setCertificateDocuments(docs);
        }

        String materialCell = getString(row, COL_MATERIAL_TYPES);
        if (materialCell != null && !materialCell.isBlank()) {
            List<Long> ids = new ArrayList<>();
            for (String m : materialCell.split(",")) {
                ids.add(
                        materialTypeRepository.findByMaterialTypeNameIgnoreCase(m.trim())
                                .orElseThrow(() -> new RuntimeException("Material not found: " + m))
                                .getMaterialTypeId()
                );
            }
            dto.setMaterialTypeId(ids);
        }

        dto.setCountryId(
                countryRepository.findByCountryNameIgnoreCase(getString(row, COL_COUNTRY))
                        .orElseThrow(() -> new RuntimeException("Country not found"))
                        .getCountryId()
        );

        String storage = getString(row, COL_STORAGE_CONDITION);
        if (storage != null && !storage.isBlank()) {
            dto.setStorageConditionId(
                    storageConditionRepository.findByConditionNameIgnoreCaseAndCategory_CategoryId(storage, categoryId)
                            .orElseThrow(() -> new RuntimeException("Storage condition not found"))
                            .getStorageConditionId()
            );
        }

        dto.setManufacturerName(getString(row, COL_MANUFACTURER));
        dto.setBrochurePath("NOT_UPLOADED");

        return dto;
    }

    // =========================================================
    // ================= CONSUMABLE ATTR (CSV) =================
    // =========================================================

    private ConsumableProductAttributeDTO buildConsumableAttrFromCsv(CSVRecord r, Long categoryId) {

        ConsumableProductAttributeDTO dto = new ConsumableProductAttributeDTO();

        dto.setDeviceCatId(
                deviceCategoryRepository.findByDeviceNameIgnoreCase(getCsvString(r, H_DEVICE_CATEGORY))
                        .orElseThrow(() -> new RuntimeException("Device category not found"))
                        .getDeviceCatId()
        );

        dto.setDeviceSubCatId(
                deviceSubCategoryRepository.findBySubCategoryNameIgnoreCase(getCsvString(r, H_DEVICE_SUBCATEGORY))
                        .orElseThrow(() -> new RuntimeException("Device subcategory not found"))
                        .getDeviceSubCatId()
        );

        dto.setBrandName(getCsvString(r, H_BRAND_NAME));
        dto.setDimensionSize(getCsvDouble(r, H_DIMENSION_SIZE));

        // Look up DeviceSpecificationUnit by sub-category + unitName
        String devSpecUnitName = getCsvString(r, H_DEV_SPECIFICATION_UNIT_NAME);
        if (devSpecUnitName != null && !devSpecUnitName.isBlank()) {
            dto.setDeviceSpecificationUnitId(
                    deviceSpecificationUnitRepository
                            .findByDeviceSubCategory_DeviceSubCatIdAndUnitNameIgnoreCase(
                                    dto.getDeviceSubCatId(), devSpecUnitName)
                            .orElseThrow(() -> new RuntimeException("Device Specification Unit not found: " + devSpecUnitName))
                            .getUnitId()
            );
        }

        dto.setSterileOrNonSterile(getCsvString(r, H_STERILE));
        dto.setDisposalOrReusable(getCsvString(r, H_DISPOSABLE));
        dto.setPurpose(getCsvString(r, H_PURPOSE));
        dto.setKeyFeaturesSpecifications(getCsvString(r, H_KEY_FEATURES));
        dto.setSafetyInstructions(getCsvString(r, H_SAFETY_INSTRUCTIONS));

        Long shelfLifeMonths = calculateShelfLifeMonths(
                parseCsvDate(getCsvString(r, H_MANUFACTURING_DATE)),
                parseCsvDate(getCsvString(r, H_EXPIRY_DATE))
        );
        dto.setShelfLife(shelfLifeMonths != null ? shelfLifeMonths.toString() : null);

        String certCell = getCsvString(r, H_CERTIFICATIONS);
        if (certCell != null && !certCell.isBlank()) {
            List<ProductCertificateDocumentDto> docs = new ArrayList<>();
            for (String c : certCell.split(",")) {
                Certification cert = certificationRepository
                        .findByCertificationNameIgnoreCaseAndCategory_CategoryId(c.trim(), categoryId)
                        .orElseThrow(() -> new RuntimeException("Certification not found: " + c));
                ProductCertificateDocumentDto d = new ProductCertificateDocumentDto();
                d.setCertificationId(cert.getCertificationId());
                d.setCertificateUrl("NOT_UPLOADED");
                docs.add(d);
            }
            dto.setCertificateDocuments(docs);
        }

        String materialCell = getCsvString(r, H_MATERIAL_TYPES);
        if (materialCell != null && !materialCell.isBlank()) {
            List<Long> ids = new ArrayList<>();
            for (String m : materialCell.split(",")) {
                ids.add(
                        materialTypeRepository.findByMaterialTypeNameIgnoreCase(m.trim())
                                .orElseThrow(() -> new RuntimeException("Material not found: " + m))
                                .getMaterialTypeId()
                );
            }
            dto.setMaterialTypeId(ids);
        }

        dto.setCountryId(
                countryRepository.findByCountryNameIgnoreCase(getCsvString(r, H_COUNTRY))
                        .orElseThrow(() -> new RuntimeException("Country not found"))
                        .getCountryId()
        );

        String storage = getCsvString(r, H_STORAGE_CONDITION);
        if (storage != null && !storage.isBlank()) {
            dto.setStorageConditionId(
                    storageConditionRepository.findByConditionNameIgnoreCaseAndCategory_CategoryId(storage, categoryId)
                            .orElseThrow(() -> new RuntimeException("Storage condition not found"))
                            .getStorageConditionId()
            );
        }

        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));
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

        if (packType != null && !packType.isBlank()) {
            dto.setPackId(
                    packTypeRepository.findByPackTypeIgnoreCaseAndCategory_CategoryId(packType, categoryId)
                            .orElseThrow(() -> new RuntimeException("Pack type not found: " + packType))
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

    // =========================================================
    // ================= ADDITIONAL DISCOUNT SLAB VALIDATION ===
    // =========================================================

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

    private void validateMandatoryExcel(Row row, Long userId) {
        List<String> errors = new ArrayList<>();

        // ── Product Name ──────────────────────────────────────────────────
        String productName = getString(row, COL_PRODUCT_NAME);
        validateRequired(productName, "Product Name", errors);
        if (productName != null && !productName.isBlank()) {
            if (productName.length() < 3) errors.add("Product Name must be at least 3 characters");
            if (productName.length() > 150) errors.add("Product Name must not exceed 150 characters");
        }

        // ── Device Category / Sub Category ────────────────────────────────
        validateRequired(getString(row, COL_DEVICE_CATEGORY), "Device Category", errors);
        validateRequired(getString(row, COL_DEVICE_SUBCATEGORY), "Device Sub Category", errors);

        // ── Brand Name ────────────────────────────────────────────────────
        String brandName = getString(row, COL_BRAND_NAME);
        validateRequired(brandName, "Brand Name", errors);
        if (brandName != null && !brandName.isBlank() && brandName.length() > 60) {
            errors.add("Brand Name must not exceed 60 characters");
        }

        // ── Size / Dimension / Gauge ──────────────────────────────────────
        String dimensionSizeRaw = getString(row, COL_DIMENSION_SIZE);
        validateRequired(dimensionSizeRaw, "Size / Dimension / Gauge", errors);
        if (dimensionSizeRaw != null && !dimensionSizeRaw.isBlank()) {
            try {
                double d = Double.parseDouble(dimensionSizeRaw);
                if (d <= 0) errors.add("Size / Dimension / Gauge must be a positive number");
            } catch (NumberFormatException e) {
                errors.add("Size / Dimension / Gauge must be a numeric value");
            }
        }

        // ── Device Specification Unit Name ────────────────────────────────
        validateRequired(getString(row, COL_DEV_SPECIFICATION_UNIT_NAME), "Device Specification Unit Name", errors);

        // ── Sterile ───────────────────────────────────────────────────────
        String sterile = getString(row, COL_STERILE);
        validateRequired(sterile, "Sterile / Non-Sterile", errors);
        if (sterile != null && !sterile.isBlank()
                && !sterile.equalsIgnoreCase("Sterile")
                && !sterile.equalsIgnoreCase("Non-Sterile")) {
            errors.add("Sterile must be either 'Sterile' or 'Non-Sterile'");
        }

        // ── Disposable ────────────────────────────────────────────────────
        String disposable = getString(row, COL_DISPOSABLE);
        validateRequired(disposable, "Disposable / Reusable", errors);
        if (disposable != null && !disposable.isBlank()
                && !disposable.equalsIgnoreCase("Disposable")
                && !disposable.equalsIgnoreCase("Reusable")) {
            errors.add("Disposable must be either 'Disposable' or 'Reusable'");
        }

        // ── Intended Use / Purpose ────────────────────────────────────────
        String purpose = getString(row, COL_PURPOSE);
        validateRequired(purpose, "Intended Use / Purpose", errors);
        if (purpose != null && !purpose.isBlank() && purpose.length() < 10) {
            errors.add("Intended Use / Purpose must be at least 10 characters");
        }

        // ── Key Features ──────────────────────────────────────────────────
        String keyFeatures = getString(row, COL_KEY_FEATURES);
        validateRequired(keyFeatures, "Key Features / Technical Specifications", errors);
        if (keyFeatures != null && !keyFeatures.isBlank() && keyFeatures.length() < 10) {
            errors.add("Key Features / Technical Specifications must be at least 10 characters");
        }

        // ── Safety Instructions / Precautions ─────────────────────────────
        String safetyInstructions = getString(row, COL_SAFETY_INSTRUCTIONS);
        validateRequired(safetyInstructions, "Safety Instructions / Precautions", errors);
        if (safetyInstructions != null && !safetyInstructions.isBlank()) {
            if (safetyInstructions.length() < 10)
                errors.add("Safety Instructions / Precautions must be at least 10 characters");
            if (safetyInstructions.length() > 1000)
                errors.add("Safety Instructions / Precautions must not exceed 1000 characters");
        }

        // ── Certifications / Material ─────────────────────────────────────
        validateRequired(getString(row, COL_CERTIFICATIONS), "Certifications / Compliance", errors);
        validateRequired(getString(row, COL_MATERIAL_TYPES), "Material Type", errors);

        // ── Country ───────────────────────────────────────────────────────
        validateRequired(getString(row, COL_COUNTRY), "Country of Origin", errors);

        // ── Manufacturer Name ─────────────────────────────────────────────
        String manufacturer = getString(row, COL_MANUFACTURER);
        validateRequired(manufacturer, "Manufacturer Name", errors);
        if (manufacturer != null && !manufacturer.isBlank() && manufacturer.length() > 100) {
            errors.add("Manufacturer Name must not exceed 100 characters");
        }

        // ── Product Description ───────────────────────────────────────────
        String description = getString(row, COL_PRODUCT_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (description != null && !description.isBlank() && description.length() > 1000) {
            errors.add("Product Description must not exceed 1000 characters");
        }

        // ── Storage Condition ─────────────────────────────────────────────
        validateRequired(getString(row, COL_STORAGE_CONDITION), "Storage Condition", errors);

        // ── Pack Type ─────────────────────────────────────────────────────
        validateRequired(getString(row, COL_PACK_TYPE), "Pack Type", errors);

        // ── Unit Per Pack ─────────────────────────────────────────────────
        String unitPerPackRaw = getString(row, COL_UNIT_PER_PACK);
        Long unitPerPack = getLong(row, COL_UNIT_PER_PACK);
        validateRequired(unitPerPackRaw, "Number of Units per Pack Type", errors);
        if (unitPerPackRaw != null && !unitPerPackRaw.isBlank()) {
            if (unitPerPack == null) errors.add("Number of Units per Pack Type must be numeric");
            else if (unitPerPack <= 0) errors.add("Number of Units per Pack Type must be a positive value");
        }

        // ── Number of Packs ───────────────────────────────────────────────
        String numberOfPacksRaw = getString(row, COL_NUMBER_OF_PACKS);
        Long numberOfPacks = getLong(row, COL_NUMBER_OF_PACKS);
        validateRequired(numberOfPacksRaw, "Number of Packs", errors);
        if (numberOfPacksRaw != null && !numberOfPacksRaw.isBlank()) {
            if (numberOfPacks == null) errors.add("Number of Packs must be numeric");
            else if (numberOfPacks <= 0) errors.add("Number of Packs must be a positive value");
        }

        // ── Min / Max Order Quantities ────────────────────────────────────
        String minOrderQtyRaw = getString(row, COL_MIN_ORDER_QTY);
        Long minOrderQty = getLong(row, COL_MIN_ORDER_QTY);
        validateRequired(minOrderQtyRaw, "Minimum Order Quantity", errors);
        if (minOrderQtyRaw != null && !minOrderQtyRaw.isBlank()) {
            if (minOrderQty == null) errors.add("Minimum Order Quantity must be numeric");
            else if (minOrderQty <= 0) errors.add("Minimum Order Quantity must be a positive value");
        }

        String maxOrderQtyRaw = getString(row, COL_MAX_ORDER_QTY);
        Long maxOrderQty = getLong(row, COL_MAX_ORDER_QTY);
        validateRequired(maxOrderQtyRaw, "Maximum Order Quantity", errors);
        if (maxOrderQtyRaw != null && !maxOrderQtyRaw.isBlank()) {
            if (maxOrderQty == null) errors.add("Maximum Order Quantity must be numeric");
            else if (maxOrderQty <= 0) errors.add("Maximum Order Quantity must be a positive value");
        }

        if (minOrderQty != null && maxOrderQty != null
                && minOrderQty > 0 && maxOrderQty > 0
                && minOrderQty > maxOrderQty) {
            errors.add("Minimum Order Quantity must be ≤ Maximum Order Quantity");
        }

        // ── Batch Number ──────────────────────────────────────────────────
        String batchNumber = getString(row, COL_BATCH_NUMBER);
        validateRequired(batchNumber, "Batch Number", errors);
        if (batchNumber != null && !batchNumber.isBlank()) {
            if (!batchNumber.matches("[A-Za-z0-9]+"))
                errors.add("Batch Number must be alphanumeric only (no special characters)");
            if (batchNumber.length() < 3)
                errors.add("Batch Number must be at least 3 characters");
            if (batchNumber.length() > 20)
                errors.add("Batch Number must not exceed 20 characters");
            if (pricingDetailsService.isBatchNumberExistsForSeller(batchNumber, userId))
                errors.add("Batch Number '" + batchNumber + "' already exists for this seller");
        }

        // ── Manufacturing Date ────────────────────────────────────────────
        LocalDate mfgDate = getDate(row, COL_MANUFACTURING_DATE);
        validateRequired(mfgDate, "Manufacturing Date", errors);
        if (mfgDate != null && YearMonth.from(mfgDate).isAfter(YearMonth.now())) {
            errors.add("Manufacturing Date cannot be a future month");
        }

        // ── Expiry Date ───────────────────────────────────────────────────
        LocalDate expiryDate = getDate(row, COL_EXPIRY_DATE);
        validateRequired(expiryDate, "Expiry Date", errors);
        if (mfgDate != null && expiryDate != null && expiryDate.isBefore(mfgDate)) {
            errors.add("Expiry Date must be after Manufacturing Date");
        }

        // ── Stock Quantity ────────────────────────────────────────────────
        String stockQtyRaw = getString(row, COL_STOCK_QUANTITY);
        Long stockQty = getLong(row, COL_STOCK_QUANTITY);
        validateRequired(stockQtyRaw, "Stock Quantity", errors);
        if (stockQtyRaw != null && !stockQtyRaw.isBlank()) {
            if (stockQty == null) errors.add("Stock Quantity must be numeric");
            else if (stockQty <= 0) errors.add("Stock Quantity must be a positive value");
            else if (minOrderQty != null && stockQty < minOrderQty)
                errors.add("Stock Quantity (" + stockQty + ") must be ≥ Minimum Order Quantity (" + minOrderQty + ")");
        }

        // ── MRP ───────────────────────────────────────────────────────────
        String mrpRaw = getString(row, COL_MRP);
        Long mrp = getLong(row, COL_MRP);
        validateRequired(mrpRaw, "MRP", errors);
        if (mrpRaw != null && !mrpRaw.isBlank()) {
            if (mrp == null) errors.add("MRP must be numeric");
            else if (mrp <= 0) errors.add("MRP must be greater than 0");
        }

        // ── Selling Price ─────────────────────────────────────────────────
        String sellingPriceRaw = getString(row, COL_SELLING_PRICE);
        Long sellingPrice = getLong(row, COL_SELLING_PRICE);
        validateRequired(sellingPriceRaw, "Selling Price", errors);
        if (sellingPriceRaw != null && !sellingPriceRaw.isBlank()) {
            if (sellingPrice == null) errors.add("Selling Price must be numeric");
            else if (sellingPrice <= 0) errors.add("Selling Price must be greater than 0");
            else if (mrp != null && sellingPrice > mrp)
                errors.add("Selling Price cannot be greater than MRP");
        }

        // ── Discount % ────────────────────────────────────────────────────
        String discountRaw = getString(row, COL_DISCOUNT);
        Long discount = getLong(row, COL_DISCOUNT);
        if (discountRaw != null && !discountRaw.isBlank()) {
            if (discount == null) errors.add("Discount % must be numeric");
            else if (discount < 0 || discount > 100)
                errors.add("Discount % must be in the range 0–100");
        }

        // ── GST % ─────────────────────────────────────────────────────────
        String gstRaw = getString(row, COL_GST);
        Long gst = getLong(row, COL_GST);
        validateRequired(gstRaw, "GST %", errors);
        if (gstRaw != null && !gstRaw.isBlank()) {
            if (gst == null) errors.add("GST % must be numeric");
            else if (!VALID_GST_VALUES.contains(gst))
                errors.add("GST % must be one of: 0, 5, 12, 18");
        }

        // ── HSN Code ──────────────────────────────────────────────────────
        String hsnRaw = getString(row, COL_HSN);
        Long hsn = getLong(row, COL_HSN);
        validateRequired(hsnRaw, "HSN Code", errors);
        if (hsnRaw != null && !hsnRaw.isBlank()) {
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

    private void validateMandatoryCsv(CSVRecord r, Long userId) {
        List<String> errors = new ArrayList<>();

        // ── Product Name ──────────────────────────────────────────────────
        String productName = getCsvString(r, H_PRODUCT_NAME);
        validateRequired(productName, "Product Name", errors);
        if (productName != null && !productName.isBlank()) {
            if (productName.length() < 3) errors.add("Product Name must be at least 3 characters");
            if (productName.length() > 150) errors.add("Product Name must not exceed 150 characters");
        }

        // ── Device Category / Sub Category ────────────────────────────────
        validateRequired(getCsvString(r, H_DEVICE_CATEGORY), "Device Category", errors);
        validateRequired(getCsvString(r, H_DEVICE_SUBCATEGORY), "Device Sub Category", errors);

        // ── Brand Name ────────────────────────────────────────────────────
        String brandName = getCsvString(r, H_BRAND_NAME);
        validateRequired(brandName, "Brand Name", errors);
        if (brandName != null && !brandName.isBlank() && brandName.length() > 60) {
            errors.add("Brand Name must not exceed 60 characters");
        }

        // ── Size / Dimension / Gauge ──────────────────────────────────────
        String dimensionSizeRaw = getCsvString(r, H_DIMENSION_SIZE);
        validateRequired(dimensionSizeRaw, "Size / Dimension / Gauge", errors);
        if (dimensionSizeRaw != null && !dimensionSizeRaw.isBlank()) {
            try {
                double d = Double.parseDouble(dimensionSizeRaw);
                if (d <= 0) errors.add("Size / Dimension / Gauge must be a positive number");
            } catch (NumberFormatException e) {
                errors.add("Size / Dimension / Gauge must be a numeric value");
            }
        }

        // ── Device Specification Unit Name ────────────────────────────────
        validateRequired(getCsvString(r, H_DEV_SPECIFICATION_UNIT_NAME), "Device Specification Unit Name", errors);

        // ── Sterile ───────────────────────────────────────────────────────
        String sterile = getCsvString(r, H_STERILE);
        validateRequired(sterile, "Sterile / Non-Sterile", errors);
        if (sterile != null && !sterile.isBlank()
                && !sterile.equalsIgnoreCase("Sterile")
                && !sterile.equalsIgnoreCase("Non-Sterile")) {
            errors.add("Sterile must be either 'Sterile' or 'Non-Sterile'");
        }

        // ── Disposable ────────────────────────────────────────────────────
        String disposable = getCsvString(r, H_DISPOSABLE);
        validateRequired(disposable, "Disposable / Reusable", errors);
        if (disposable != null && !disposable.isBlank()
                && !disposable.equalsIgnoreCase("Disposable")
                && !disposable.equalsIgnoreCase("Reusable")) {
            errors.add("Disposable must be either 'Disposable' or 'Reusable'");
        }

        // ── Intended Use / Purpose ────────────────────────────────────────
        String purpose = getCsvString(r, H_PURPOSE);
        validateRequired(purpose, "Intended Use / Purpose", errors);
        if (purpose != null && !purpose.isBlank() && purpose.length() < 10) {
            errors.add("Intended Use / Purpose must be at least 10 characters");
        }

        // ── Key Features ──────────────────────────────────────────────────
        String keyFeatures = getCsvString(r, H_KEY_FEATURES);
        validateRequired(keyFeatures, "Key Features / Technical Specifications", errors);
        if (keyFeatures != null && !keyFeatures.isBlank() && keyFeatures.length() < 10) {
            errors.add("Key Features / Technical Specifications must be at least 10 characters");
        }

        // ── Safety Instructions / Precautions ─────────────────────────────
        String safetyInstructions = getCsvString(r, H_SAFETY_INSTRUCTIONS);
        validateRequired(safetyInstructions, "Safety Instructions / Precautions", errors);
        if (safetyInstructions != null && !safetyInstructions.isBlank()) {
            if (safetyInstructions.length() < 10)
                errors.add("Safety Instructions / Precautions must be at least 10 characters");
            if (safetyInstructions.length() > 1000)
                errors.add("Safety Instructions / Precautions must not exceed 1000 characters");
        }

        // ── Certifications / Material ─────────────────────────────────────
        validateRequired(getCsvString(r, H_CERTIFICATIONS), "Certifications / Compliance", errors);
        validateRequired(getCsvString(r, H_MATERIAL_TYPES), "Material Type", errors);

        // ── Country ───────────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_COUNTRY), "Country of Origin", errors);

        // ── Manufacturer Name ─────────────────────────────────────────────
        String manufacturer = getCsvString(r, H_MANUFACTURER);
        validateRequired(manufacturer, "Manufacturer Name", errors);
        if (manufacturer != null && !manufacturer.isBlank() && manufacturer.length() > 100) {
            errors.add("Manufacturer Name must not exceed 100 characters");
        }

        // ── Product Description ───────────────────────────────────────────
        String description = getCsvString(r, H_PRODUCT_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (description != null && !description.isBlank() && description.length() > 1000) {
            errors.add("Product Description must not exceed 1000 characters");
        }

        // ── Storage Condition ─────────────────────────────────────────────
        validateRequired(getCsvString(r, H_STORAGE_CONDITION), "Storage Condition", errors);

        // ── Pack Type ─────────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_PACK_TYPE), "Pack Type", errors);

        // ── Unit Per Pack ─────────────────────────────────────────────────
        String unitPerPackRaw = getCsvString(r, H_UNIT_PER_PACK);
        Long unitPerPack = getCsvLong(r, H_UNIT_PER_PACK);
        validateRequired(unitPerPackRaw, "Number of Units per Pack Type", errors);
        if (unitPerPackRaw != null && !unitPerPackRaw.isBlank()) {
            if (unitPerPack == null) errors.add("Number of Units per Pack Type must be numeric");
            else if (unitPerPack <= 0) errors.add("Number of Units per Pack Type must be a positive value");
        }

        // ── Number of Packs ───────────────────────────────────────────────
        String numberOfPacksRaw = getCsvString(r, H_NUMBER_OF_PACKS);
        Long numberOfPacks = getCsvLong(r, H_NUMBER_OF_PACKS);
        validateRequired(numberOfPacksRaw, "Number of Packs", errors);
        if (numberOfPacksRaw != null && !numberOfPacksRaw.isBlank()) {
            if (numberOfPacks == null) errors.add("Number of Packs must be numeric");
            else if (numberOfPacks <= 0) errors.add("Number of Packs must be a positive value");
        }

        // ── Min / Max Order Quantities ────────────────────────────────────
        String minOrderQtyRaw = getCsvString(r, H_MIN_ORDER_QTY);
        Long minOrderQty = getCsvLong(r, H_MIN_ORDER_QTY);
        validateRequired(minOrderQtyRaw, "Minimum Order Quantity", errors);
        if (minOrderQtyRaw != null && !minOrderQtyRaw.isBlank()) {
            if (minOrderQty == null) errors.add("Minimum Order Quantity must be numeric");
            else if (minOrderQty <= 0) errors.add("Minimum Order Quantity must be a positive value");
        }

        String maxOrderQtyRaw = getCsvString(r, H_MAX_ORDER_QTY);
        Long maxOrderQty = getCsvLong(r, H_MAX_ORDER_QTY);
        validateRequired(maxOrderQtyRaw, "Maximum Order Quantity", errors);
        if (maxOrderQtyRaw != null && !maxOrderQtyRaw.isBlank()) {
            if (maxOrderQty == null) errors.add("Maximum Order Quantity must be numeric");
            else if (maxOrderQty <= 0) errors.add("Maximum Order Quantity must be a positive value");
        }

        if (minOrderQty != null && maxOrderQty != null
                && minOrderQty > 0 && maxOrderQty > 0
                && minOrderQty > maxOrderQty) {
            errors.add("Minimum Order Quantity must be ≤ Maximum Order Quantity");
        }

        // ── Batch Number ──────────────────────────────────────────────────
        String batchNumber = getCsvString(r, H_BATCH_NUMBER);
        validateRequired(batchNumber, "Batch Number", errors);
        if (batchNumber != null && !batchNumber.isBlank()) {
            if (!batchNumber.matches("[A-Za-z0-9]+"))
                errors.add("Batch Number must be alphanumeric only (no special characters)");
            if (batchNumber.length() < 3)
                errors.add("Batch Number must be at least 3 characters");
            if (batchNumber.length() > 20)
                errors.add("Batch Number must not exceed 20 characters");
            if (pricingDetailsService.isBatchNumberExistsForSeller(batchNumber, userId))
                errors.add("Batch Number '" + batchNumber + "' already exists for this seller");
        }

        // ── Manufacturing Date ────────────────────────────────────────────
        LocalDate mfgDate = parseCsvDate(getCsvString(r, H_MANUFACTURING_DATE));
        validateRequired(mfgDate, "Manufacturing Date", errors);
        if (mfgDate != null && YearMonth.from(mfgDate).isAfter(YearMonth.now())) {
            errors.add("Manufacturing Date cannot be a future month");
        }

        // ── Expiry Date ───────────────────────────────────────────────────
        LocalDate expiryDate = parseCsvDate(getCsvString(r, H_EXPIRY_DATE));
        validateRequired(expiryDate, "Expiry Date", errors);
        if (mfgDate != null && expiryDate != null && expiryDate.isBefore(mfgDate)) {
            errors.add("Expiry Date must be after Manufacturing Date");
        }

        // ── Stock Quantity ────────────────────────────────────────────────
        String stockQtyRaw = getCsvString(r, H_STOCK_QUANTITY);
        Long stockQty = getCsvLong(r, H_STOCK_QUANTITY);
        validateRequired(stockQtyRaw, "Stock Quantity", errors);
        if (stockQtyRaw != null && !stockQtyRaw.isBlank()) {
            if (stockQty == null) errors.add("Stock Quantity must be numeric");
            else if (stockQty <= 0) errors.add("Stock Quantity must be a positive value");
            else if (minOrderQty != null && stockQty < minOrderQty)
                errors.add("Stock Quantity (" + stockQty + ") must be ≥ Minimum Order Quantity (" + minOrderQty + ")");
        }

        // ── MRP ───────────────────────────────────────────────────────────
        String mrpRaw = getCsvString(r, H_MRP);
        Long mrp = getCsvLong(r, H_MRP);
        validateRequired(mrpRaw, "MRP", errors);
        if (mrpRaw != null && !mrpRaw.isBlank()) {
            if (mrp == null) errors.add("MRP must be numeric");
            else if (mrp <= 0) errors.add("MRP must be greater than 0");
        }

        // ── Selling Price ─────────────────────────────────────────────────
        String sellingPriceRaw = getCsvString(r, H_SELLING_PRICE);
        Long sellingPrice = getCsvLong(r, H_SELLING_PRICE);
        validateRequired(sellingPriceRaw, "Selling Price", errors);
        if (sellingPriceRaw != null && !sellingPriceRaw.isBlank()) {
            if (sellingPrice == null) errors.add("Selling Price must be numeric");
            else if (sellingPrice <= 0) errors.add("Selling Price must be greater than 0");
            else if (mrp != null && sellingPrice > mrp)
                errors.add("Selling Price cannot be greater than MRP");
        }

        // ── Discount % ────────────────────────────────────────────────────
        String discountRaw = getCsvString(r, H_DISCOUNT);
        Long discount = getCsvLong(r, H_DISCOUNT);
        if (discountRaw != null && !discountRaw.isBlank()) {
            if (discount == null) errors.add("Discount % must be numeric");
            else if (discount < 0 || discount > 100)
                errors.add("Discount % must be in the range 0–100");
        }

        // ── GST % ─────────────────────────────────────────────────────────
        String gstRaw = getCsvString(r, H_GST);
        Long gst = getCsvLong(r, H_GST);
        validateRequired(gstRaw, "GST %", errors);
        if (gstRaw != null && !gstRaw.isBlank()) {
            if (gst == null) errors.add("GST % must be numeric");
            else if (!VALID_GST_VALUES.contains(gst))
                errors.add("GST % must be one of: 0, 5, 12, 18");
        }

        // ── HSN Code ──────────────────────────────────────────────────────
        String hsnRaw = getCsvString(r, H_HSN);
        Long hsn = getCsvLong(r, H_HSN);
        validateRequired(hsnRaw, "HSN Code", errors);
        if (hsnRaw != null && !hsnRaw.isBlank()) {
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

    private Long calculateShelfLifeMonths(LocalDate mfg, LocalDate exp) {
        if (mfg == null || exp == null) return null;
        return (long) java.time.Period.between(mfg, exp).toTotalMonths();
    }

    private void validateRequired(Object value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(fieldName + " is mandatory");
        } else if (value instanceof String && ((String) value).isBlank()) {
            errors.add(fieldName + " is mandatory");
        }
    }

    // =========================================================
    // ================= EXCEL HELPERS =========================
    // =========================================================

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

    private Double getDouble(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String s = cell.getStringCellValue().trim();
                    if (s.isEmpty()) return null;
                    return Double.parseDouble(s);
                case FORMULA:
                    return cell.getNumericCellValue();
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
            return v != null ? Long.parseLong(v) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Double getCsvDouble(CSVRecord r, String header) {
        try {
            String v = getCsvString(r, header);
            return v != null ? Double.parseDouble(v) : null;
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
        try { return LocalDate.parse(raw); } catch (Exception ignored) {}
        try { return YearMonth.parse(raw, DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH)).atDay(1); } catch (Exception ignored) {}
        try { return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd-MM-yyyy")); } catch (Exception ignored) {}
        try { return LocalDate.parse(raw, DateTimeFormatter.ofPattern("M/d/yyyy")); } catch (Exception ignored) {}
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
}