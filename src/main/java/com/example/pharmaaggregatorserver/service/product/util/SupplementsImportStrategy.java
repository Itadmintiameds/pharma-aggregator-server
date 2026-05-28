package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component("SUPPLEMENTS_OR_NUTRACEUTICALS")
@RequiredArgsConstructor
public class SupplementsImportStrategy implements ProductImportStrategy {

    private final TherapeuticCategoryRepository therapeuticCategoryRepository;
    private final TherapeuticSubcategoryRepository therapeuticSubcategoryRepository;
    private final DosageFormRepository dosageFormRepository;
    private final AgeGroupMasterRepository ageGroupMasterRepository;
    private final FlavourRepository flavourRepository;
    private final CountryMasterRepository countryMasterRepository;
    private final StorageConditionMasterRepository storageConditionRepository;
    private final PackTypeRepository packTypeRepository;
    private final CertificationRepository certificationRepository;
    private final NetQuantityUnitRepository netQuantityUnitRepository;
    private final ServingSizeUnitRepository servingSizeUnitRepository;

    // ── Valid GST percentages ─────────────────────────────────────────────
    private static final Set<Long> VALID_GST_VALUES = Set.of(0L, 5L, 12L, 18L);

    // ── Valid Gender values ───────────────────────────────────────────────
    private static final Set<String> VALID_GENDER_VALUES = Set.of("male", "female", "unisex");

    // ── Valid Veg/Non-Veg values ──────────────────────────────────────────
    private static final Set<String> VALID_VEG_NON_VEG_VALUES = Set.of("veg", "non-veg");

    // ── Valid Nutritional Information values ──────────────────────────────
    private static final Set<String> VALID_NUTRITIONAL_INFO_VALUES = Set.of("as per the label", "image upload");

    // ── Column indices (0-based, data starts at row index 2) ──────────────
    private static final int COL_THERAPEUTIC_CAT = 0;
    private static final int COL_THERAPEUTIC_SUBCAT = 1;
    private static final int COL_PRODUCT_NAME = 2;
    private static final int COL_BRAND_NAME = 3;
    private static final int COL_VARIANT_NAME = 4;
    private static final int COL_DOSAGE_FORM = 5;
    private static final int COL_NET_QUANTITY = 6;
    private static final int COL_NET_QUANTITY_UNIT = 7;     // NEW
    private static final int COL_SERVING_SIZE = 8;          // NEW
    private static final int COL_SERVING_SIZE_UNIT = 9;     // NEW
    private static final int COL_STRENGTH = 10;  // was 7
    private static final int COL_ACTIVE_INGREDIENTS = 11;  // was 8
    private static final int COL_OTHER_INGREDIENTS = 12;  // was 9
    private static final int COL_NUTRITIONAL_INFO = 13;  // was 10
    private static final int COL_INTENDED_USE = 14;  // was 11
    private static final int COL_AGE_GROUP = 15;  // was 12
    private static final int COL_GENDER = 16;  // was 13
    private static final int COL_VEG_NON_VEG = 17;  // was 14
    private static final int COL_ALLERGEN_INFO = 18;  // was 15
    private static final int COL_FLAVOUR = 19;  // was 16
    private static final int COL_PRODUCT_CLAIMS = 20;  // was 17
    private static final int COL_WARNINGS = 21;  // was 18
    private static final int COL_DESCRIPTION = 22;  // was 19
    private static final int COL_STORAGE_CONDITION = 23;  // was 20
    private static final int COL_MANUFACTURER = 24;  // was 21
    private static final int COL_COUNTRY = 25;  // was 22
    private static final int COL_CERTIFICATIONS = 26;  // was 23 → ProductCertificateDocumentDto list
    private static final int COL_PACK_TYPE = 27;  // was 24
    private static final int COL_UNIT_PER_PACK = 28;  // was 25
    private static final int COL_NUMBER_OF_PACKS = 29;  // was 26
    // col 30 = Pack Size (Auto) — not read
    private static final int COL_MIN_ORDER_QTY = 31;  // was 28
    private static final int COL_MAX_ORDER_QTY = 32;  // was 29
    private static final int COL_BATCH_NUMBER = 33;  // was 30
    private static final int COL_MFG_DATE = 34;  // was 31
    private static final int COL_EXPIRY_DATE = 35;  // was 32
    private static final int COL_STOCK_QTY = 36;  // was 33
    private static final int COL_DATE_OF_ENTRY = 37;  // was 34 — ignored, always LocalDate.now()
    private static final int COL_MRP = 38;  // was 35
    private static final int COL_SELLING_PRICE = 39;  // was 36
    private static final int COL_DISCOUNT_PCT = 40;  // was 37
    private static final int COL_GST_PCT = 41;  // was 38
    private static final int COL_HSN_CODE = 42;  // was 39
    // col 71 = Product Image URL* — skipped

    // ── Additional discount slabs ─────────────────────────────────────────
    // 4 slabs × 7 cols each, starting at col 43
    // Per slab: [Slab label | MinQty | Discount% | StartDate | StartTime | EndDate | EndTime]
    private static final int COL_ADD_DISCOUNT_START = 43;  // was 40
    private static final int ADD_DISCOUNT_SLAB_SIZE = 7;
    private static final int ADD_DISCOUNT_SLAB_COUNT = 4;

    // ── CSV header names (match Excel row 0 exactly) ──────────────────────
    private static final String H_THERAPEUTIC_CAT = "Therapeutic Category*";
    private static final String H_THERAPEUTIC_SUBCAT = "Therapeutic Sub Category*";
    private static final String H_PRODUCT_NAME = "Product Name*";
    private static final String H_BRAND_NAME = "Brand Name*";
    private static final String H_VARIANT_NAME = "Variant Name";
    private static final String H_DOSAGE_FORM = "Dosage Form*";
    private static final String H_NET_QUANTITY = "Net Quantity*";
    private static final String H_NET_QUANTITY_UNIT = "Net Quantity Unit*";   // NEW
    private static final String H_SERVING_SIZE = "Serving Size*";        // NEW
    private static final String H_SERVING_SIZE_UNIT = "Serving Size Unit*";   // NEW
    private static final String H_STRENGTH = "Strength / Composition*";
    private static final String H_ACTIVE_INGREDIENTS = "Active Ingredients*";
    private static final String H_OTHER_INGREDIENTS = "Excipients / Other Ingredients";
    private static final String H_NUTRITIONAL_INFO = "Nutritional Information Table";
    private static final String H_INTENDED_USE = "Intended Use / Health Benefit*";
    private static final String H_AGE_GROUP = "Age Group*";
    private static final String H_GENDER = "Gender*";
    private static final String H_VEG_NON_VEG = "Veg / Non-Veg Indicator*";
    private static final String H_ALLERGEN_INFO = "Allergen Information*";
    private static final String H_FLAVOUR = "Flavour*";
    private static final String H_PRODUCT_CLAIMS = "Product Claims*";
    private static final String H_WARNINGS = "Warnings / Precautions*";
    private static final String H_DESCRIPTION = "Product Description*";
    private static final String H_STORAGE_CONDITION = "Storage Condition*";
    private static final String H_MANUFACTURER = "Manufacturer Name*";
    private static final String H_COUNTRY = "Country of Origin*";
    private static final String H_CERTIFICATIONS = "Certifications / Compliance*";
    private static final String H_PACK_TYPE = "Pack Type";
    private static final String H_UNIT_PER_PACK = "Unit Per Pack";
    private static final String H_NUMBER_OF_PACKS = "Number Of Packs";
    private static final String H_MIN_ORDER_QTY = "Minimum Order Qty*";
    private static final String H_MAX_ORDER_QTY = "Max Order Qty*";
    private static final String H_BATCH_NUMBER = "Batch Number*";
    private static final String H_MFG_DATE = "Manufacturing Date*";
    private static final String H_EXPIRY_DATE = "Expiry Date*";
    private static final String H_STOCK_QTY = "Stock Quantity*";
    private static final String H_DATE_OF_ENTRY = "Date of Entry*";  // ignored — always today
    private static final String H_MRP = "MRP (INR)*";
    private static final String H_SELLING_PRICE = "Selling Price(INR)*";
    private static final String H_DISCOUNT_PCT = "Discount %";
    private static final String H_GST_PCT = "GST %";
    private static final String H_HSN_CODE = "HSN Code*";

    // Additional discount slabs — duplicate headers in CSV; use index-based access.
// Slab 0: base 43, Slab 1: base 50, Slab 2: base 57, Slab 3: base 64
    private static final int[] CSV_SLAB_MIN_QTY_COLS = {44, 51, 58, 65};  // was {41,48,55,62}
    private static final int[] CSV_SLAB_DISCOUNT_COLS = {45, 52, 59, 66};  // was {42,49,56,63}
    private static final int[] CSV_SLAB_START_DATE_COLS = {46, 53, 60, 67};  // was {43,50,57,64}
    private static final int[] CSV_SLAB_START_TIME_COLS = {47, 54, 61, 68};  // was {44,51,58,65}
    private static final int[] CSV_SLAB_END_DATE_COLS = {48, 55, 62, 69};  // was {45,52,59,66}
    private static final int[] CSV_SLAB_END_TIME_COLS = {49, 56, 63, 70};  // was {46,53,60,67}

    // =========================================================
    // ================= EXCEL ENTRY POINT =====================
    // =========================================================

    @Override
    public ProductDetailsDto mapRow(Row row, Long categoryId) {
        log.info("Supplements/Nutraceuticals Excel import Called");

        validateMandatoryExcel(row);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getString(row, COL_PRODUCT_NAME));
        dto.setWarningsPrecautions(getString(row, COL_WARNINGS));
        dto.setProductDescription(getString(row, COL_DESCRIPTION));
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));

        // ── Packaging ─────────────────────────────────────────────────────
        Long unitPerPack = getLong(row, COL_UNIT_PER_PACK);
        Long numberOfPacks = getLong(row, COL_NUMBER_OF_PACKS);
        String packTypeName = getString(row, COL_PACK_TYPE);
        String dosageFormName = getString(row, COL_DOSAGE_FORM);

        dto.setPackagingDetails(Set.of(buildPackaging(
                unitPerPack, numberOfPacks,
                getLong(row, COL_MIN_ORDER_QTY),
                getLong(row, COL_MAX_ORDER_QTY),
                packTypeName, categoryId, dosageFormName)));

        // ── Pricing ───────────────────────────────────────────────────────
        LocalDate mfgDate = getDate(row, COL_MFG_DATE);
        LocalDate expiryDate = getDate(row, COL_EXPIRY_DATE);
        Long shelfLifeMonths = computeShelfLifeMonths(mfgDate, expiryDate);

        Long mainDiscountPct = getLong(row, COL_DISCOUNT_PCT);
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

            AdditionalDiscountDto ad = new AdditionalDiscountDto();
            ad.setMinimumPurchaseQuantity(minQty);
            ad.setAdditionalDiscountPercentage(discount);
            ad.setEffectiveStartDate(getDate(row, base + 3));
            ad.setEffectiveStartTime(getTime(row, base + 4));
            ad.setEffectiveEndDate(getDate(row, base + 5));
            ad.setEffectiveEndTime(getTime(row, base + 6));
            additionalDiscounts.add(ad);
        }

        dto.setPricingDetails(Set.of(buildPricing(
                getString(row, COL_BATCH_NUMBER),
                toStartOfDay(mfgDate),
                toEndOfDay(expiryDate),
                getLong(row, COL_STOCK_QTY),
                LocalDate.now(),
                getLong(row, COL_MRP),
                getLong(row, COL_SELLING_PRICE),
                mainDiscountPct,
                getLong(row, COL_GST_PCT),
                getLong(row, COL_HSN_CODE),
                shelfLifeMonths,
                additionalDiscounts)));

        // ── Supplement Attributes ─────────────────────────────────────────
        dto.setProductAttributeSupplementsOrNutraceuticals(
                Set.of(buildSupplementAttr(row, categoryId)));

        return dto;
    }

    // =========================================================
    // ================= CSV ENTRY POINT =======================
    // =========================================================

    @Override
    public ProductDetailsDto mapCsv(CSVRecord r, Long categoryId) {
        log.info("Supplements/Nutraceuticals CSV import Called");

        validateMandatoryCsv(r);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getCsvString(r, H_PRODUCT_NAME));
        dto.setWarningsPrecautions(getCsvString(r, H_WARNINGS));
        dto.setProductDescription(getCsvString(r, H_DESCRIPTION));
        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));

        // ── Packaging ─────────────────────────────────────────────────────
        Long unitPerPack = getCsvLong(r, H_UNIT_PER_PACK);
        Long numberOfPacks = getCsvLong(r, H_NUMBER_OF_PACKS);
        String packTypeName = getCsvString(r, H_PACK_TYPE);
        String dosageFormName = getCsvString(r, H_DOSAGE_FORM);

        dto.setPackagingDetails(Set.of(buildPackaging(
                unitPerPack, numberOfPacks,
                getCsvLong(r, H_MIN_ORDER_QTY),
                getCsvLong(r, H_MAX_ORDER_QTY),
                packTypeName, categoryId, dosageFormName)));

        // ── Pricing ───────────────────────────────────────────────────────
        LocalDate mfgDate = parseCsvDate(getCsvString(r, H_MFG_DATE));
        LocalDate expiryDate = parseCsvDate(getCsvString(r, H_EXPIRY_DATE));
        Long shelfLifeMonths = computeShelfLifeMonths(mfgDate, expiryDate);

        Long mainDiscountPct = getCsvLong(r, H_DISCOUNT_PCT);
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

            AdditionalDiscountDto ad = new AdditionalDiscountDto();
            ad.setMinimumPurchaseQuantity(minQty);
            ad.setAdditionalDiscountPercentage(discount);
            ad.setEffectiveStartDate(slabStartDate);
            ad.setEffectiveStartTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_START_TIME_COLS[slab])));
            ad.setEffectiveEndDate(slabEndDate);
            ad.setEffectiveEndTime(parseCsvTime(getCsvStringByIndex(r, CSV_SLAB_END_TIME_COLS[slab])));
            additionalDiscounts.add(ad);
        }

        dto.setPricingDetails(Set.of(buildPricing(
                getCsvString(r, H_BATCH_NUMBER),
                toStartOfDay(mfgDate),
                toEndOfDay(expiryDate),
                getCsvLong(r, H_STOCK_QTY),
                LocalDate.now(),
                getCsvLong(r, H_MRP),
                getCsvLong(r, H_SELLING_PRICE),
                mainDiscountPct,
                getCsvLong(r, H_GST_PCT),
                getCsvLong(r, H_HSN_CODE),
                shelfLifeMonths,
                additionalDiscounts)));

        // ── Supplement Attributes ─────────────────────────────────────────
        dto.setProductAttributeSupplementsOrNutraceuticals(
                Set.of(buildSupplementAttrFromCsv(r, categoryId)));

        return dto;
    }

    // =========================================================
    // ================= SUPPLEMENT ATTR (Excel) ===============
    // =========================================================

    private ProductAttributeSupplementsOrNutraceuticalsDto buildSupplementAttr(Row row, Long categoryId) {

        ProductAttributeSupplementsOrNutraceuticalsDto attr =
                new ProductAttributeSupplementsOrNutraceuticalsDto();

        // ── Therapeutic Category ──────────────────────────────────────────
        String therapeuticCat = getString(row, COL_THERAPEUTIC_CAT);
        if (!isBlank(therapeuticCat)) {
            attr.setTherapeuticCategoryId(
                    therapeuticCategoryRepository
                            .findByTherapeuticCategoryIgnoreCaseAndCategory_CategoryId(therapeuticCat, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Therapeutic category not found: " + therapeuticCat))
                            .getTherapeuticCategoryId()
                            .toString());
        }

        // ── Therapeutic Sub Category ──────────────────────────────────────
        String therapeuticSubCat = getString(row, COL_THERAPEUTIC_SUBCAT);
        if (!isBlank(therapeuticSubCat)) {
            attr.setTherapeuticSubCategoryId(
                    therapeuticSubcategoryRepository
                            .findByTherapeuticSubcategoryIgnoreCaseAndTherapeuticCategoryMaster_TherapeuticCategoryId(therapeuticSubCat, attr.getTherapeuticCategoryId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Therapeutic subcategory not found: " + therapeuticSubCat))
                            .getTherapeuticSubcategoryId()
                            .toString());
        }

        // ── Basic text fields ─────────────────────────────────────────────
        attr.setBrandName(getString(row, COL_BRAND_NAME));
        attr.setVariantName(getString(row, COL_VARIANT_NAME));
        attr.setNetQuantity(getDouble(row, COL_NET_QUANTITY));

        String netQuantityUnit = getString(row, COL_NET_QUANTITY_UNIT);
        if (!isBlank(netQuantityUnit)) {
            attr.setNetQuantityUnitId(
                    netQuantityUnitRepository
                            .findByUnitNameIgnoreCaseAndCategory_CategoryId(netQuantityUnit, categoryId)
                            .orElseThrow(() -> new RuntimeException("Net quantity unit not found: " + netQuantityUnit))
                            .getUnitId()
            );
        }

        attr.setStrength(getString(row, COL_STRENGTH));
        attr.setActiveIngredients(getString(row, COL_ACTIVE_INGREDIENTS));
        attr.setOtherIngredients(getString(row, COL_OTHER_INGREDIENTS));
        attr.setNutritionalInformation(getString(row, COL_NUTRITIONAL_INFO));
        attr.setIntendedUse(getString(row, COL_INTENDED_USE));
        attr.setGender(getString(row, COL_GENDER));
        attr.setVegOrNonVegIndicator(getString(row, COL_VEG_NON_VEG));
        attr.setAllergenInformation(getString(row, COL_ALLERGEN_INFO));
        attr.setProductClaims(getString(row, COL_PRODUCT_CLAIMS));

        // ── Dosage Form ───────────────────────────────────────────────────
        String dosageFormName = getString(row, COL_DOSAGE_FORM);
        Long dosageId = 0L;
        if (!isBlank(dosageFormName)) {
            dosageId = dosageFormRepository
                    .findByDosageNameIgnoreCaseAndCategory_CategoryId(dosageFormName, categoryId)
                    .orElseThrow(() -> new RuntimeException(
                            "Dosage form not found: " + dosageFormName))
                    .getDosageId();
            attr.setDosageFormId(dosageId);
        }

        attr.setServingSize(getDouble(row, COL_SERVING_SIZE));

        String servingSizeUnit = getString(row, COL_SERVING_SIZE_UNIT);
        if (!isBlank(servingSizeUnit)) {
            attr.setServingSizeUnitId(
                    servingSizeUnitRepository
                            .findByServingSizeUnitIgnoreCaseAndDosageForm_DosageId(servingSizeUnit, dosageId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Serving size unit not found: " + servingSizeUnit))
                            .getId()
            );
        }

        // ── Age Group ─────────────────────────────────────────────────────
//        String ageGroupName = getString(row, COL_AGE_GROUP);
//        if (!isBlank(ageGroupName)) {
//            attr.setAgeGroupId(
//                    ageGroupMasterRepository
//                            .findByAgeGroupIgnoreCase(ageGroupName)
//                            .orElseThrow(() -> new RuntimeException(
//                                    "Age group not found: " + ageGroupName))
//                            .getAgeGroupId());
//        }

        String ageGroupName = getString(row, COL_AGE_GROUP);
        if (!isBlank(ageGroupName)) {
            List<Long> ageGroupIds = new ArrayList<>();
            for (String agName : ageGroupName.split(",")) {
                String trimmed = agName.trim();
                ageGroupIds.add(
                        ageGroupMasterRepository
                                .findByAgeGroupIgnoreCase(trimmed)
                                .orElseThrow(() -> new RuntimeException(
                                        "Age group not found: " + trimmed))
                                .getAgeGroupId());
            }
            attr.setAgeGroupIds(ageGroupIds);
        }

        // ── Flavour ───────────────────────────────────────────────────────
        String flavourName = getString(row, COL_FLAVOUR);
        if (!isBlank(flavourName)) {
            attr.setFlavourId(
                    flavourRepository
                            .findByFlavourNameIgnoreCase(flavourName)
                            .orElseThrow(() -> new RuntimeException(
                                    "Flavour not found: " + flavourName))
                            .getFlavourId());
        }

        // ── Storage Condition (single) ────────────────────────────────────
        String storage = getString(row, COL_STORAGE_CONDITION);
        if (!isBlank(storage)) {
            attr.setStorageConditionId(
                    storageConditionRepository
                            .findByConditionNameIgnoreCaseAndCategory_CategoryId(storage, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Storage condition not found: " + storage))
                            .getStorageConditionId());
        }

        // ── Country ───────────────────────────────────────────────────────
        String country = getString(row, COL_COUNTRY);
        if (!isBlank(country)) {
            attr.setCountryId(
                    countryMasterRepository
                            .findByCountryNameIgnoreCase(country)
                            .orElseThrow(() -> new RuntimeException(
                                    "Country not found: " + country))
                            .getCountryId());
        }

        // ── Certifications — same pattern as NonConsumable ────────────────
        String certCell = getString(row, COL_CERTIFICATIONS);
        log.info("Supplement Certification: {}", certCell);
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
            attr.setCertificateDocuments(docs);
        }

        attr.setBrochurePath("NOT_UPLOADED");

        return attr;
    }

    // =========================================================
    // ================= SUPPLEMENT ATTR (CSV) =================
    // =========================================================

    private ProductAttributeSupplementsOrNutraceuticalsDto buildSupplementAttrFromCsv(CSVRecord r, Long categoryId) {

        ProductAttributeSupplementsOrNutraceuticalsDto attr =
                new ProductAttributeSupplementsOrNutraceuticalsDto();

        // ── Therapeutic Category ──────────────────────────────────────────
        String therapeuticCat = getCsvString(r, H_THERAPEUTIC_CAT);
        if (!isBlank(therapeuticCat)) {
            attr.setTherapeuticCategoryId(
                    therapeuticCategoryRepository
                            .findByTherapeuticCategoryIgnoreCaseAndCategory_CategoryId(therapeuticCat, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Therapeutic category not found: " + therapeuticCat))
                            .getTherapeuticCategoryId()
                            .toString());
        }

        // ── Therapeutic Sub Category ──────────────────────────────────────
        String therapeuticSubCat = getCsvString(r, H_THERAPEUTIC_SUBCAT);
        if (!isBlank(therapeuticSubCat)) {
            attr.setTherapeuticSubCategoryId(
                    therapeuticSubcategoryRepository
                            .findByTherapeuticSubcategoryIgnoreCaseAndTherapeuticCategoryMaster_TherapeuticCategoryId(therapeuticSubCat, attr.getTherapeuticCategoryId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Therapeutic subcategory not found: " + therapeuticSubCat))
                            .getTherapeuticSubcategoryId()
                            .toString());
        }

        // ── Basic text fields ─────────────────────────────────────────────
        attr.setBrandName(getCsvString(r, H_BRAND_NAME));
        attr.setVariantName(getCsvString(r, H_VARIANT_NAME));
        attr.setNetQuantity(getCsvDouble(r, H_NET_QUANTITY));

        String netQuantityUnit = getCsvString(r, H_NET_QUANTITY_UNIT);
        if (!isBlank(netQuantityUnit)) {
            attr.setNetQuantityUnitId(
                    netQuantityUnitRepository.findByUnitNameIgnoreCaseAndCategory_CategoryId(netQuantityUnit, categoryId)
                            .orElseThrow(() -> new NotFoundException("Net Quantity Unit not found: " + netQuantityUnit))
                            .getUnitId()
            );
        }

        attr.setStrength(getCsvString(r, H_STRENGTH));
        attr.setActiveIngredients(getCsvString(r, H_ACTIVE_INGREDIENTS));
        attr.setOtherIngredients(getCsvString(r, H_OTHER_INGREDIENTS));
        attr.setNutritionalInformation(getCsvString(r, H_NUTRITIONAL_INFO));
        attr.setIntendedUse(getCsvString(r, H_INTENDED_USE));
        attr.setGender(getCsvString(r, H_GENDER));
        attr.setVegOrNonVegIndicator(getCsvString(r, H_VEG_NON_VEG));
        attr.setAllergenInformation(getCsvString(r, H_ALLERGEN_INFO));
        attr.setProductClaims(getCsvString(r, H_PRODUCT_CLAIMS));

        // ── Dosage Form ───────────────────────────────────────────────────
        String dosageFormName = getCsvString(r, H_DOSAGE_FORM);
        Long dosageId = 0L;
        if (!isBlank(dosageFormName)) {
            dosageId = dosageFormRepository
                    .findByDosageNameIgnoreCaseAndCategory_CategoryId(dosageFormName, categoryId)
                    .orElseThrow(() -> new RuntimeException(
                            "Dosage form not found: " + dosageFormName))
                    .getDosageId();
            attr.setDosageFormId(dosageId);
        }

        attr.setServingSize(getCsvDouble(r, H_SERVING_SIZE));

        String servingSizeUnit = getCsvString(r, H_SERVING_SIZE_UNIT);
        if (!isBlank(servingSizeUnit)) {
            attr.setServingSizeUnitId(
                    servingSizeUnitRepository
                            .findByServingSizeUnitIgnoreCaseAndDosageForm_DosageId(servingSizeUnit, dosageId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Serving size unit not found: " + servingSizeUnit))
                            .getId()
            );
        }

        // ── Age Group ─────────────────────────────────────────────────────
//        String ageGroupName = getCsvString(r, H_AGE_GROUP);
//        if (!isBlank(ageGroupName)) {
//            attr.setAgeGroupId(
//                    ageGroupMasterRepository
//                            .findByAgeGroupIgnoreCase(ageGroupName)
//                            .orElseThrow(() -> new RuntimeException(
//                                    "Age group not found: " + ageGroupName))
//                            .getAgeGroupId());
//        }

        String ageGroupName = getCsvString(r, H_AGE_GROUP);
        if (!isBlank(ageGroupName)) {
            List<Long> ageGroupIds = new ArrayList<>();
            for (String agName : ageGroupName.split(",")) {
                String normalizedCsv = agName.replaceAll("[^a-zA-Z0-9() ]", "").trim().toLowerCase();
                ageGroupIds.add(
                        ageGroupMasterRepository.findAll().stream()
                                .filter(ag -> ag.getAgeGroup()
                                        .replaceAll("[^a-zA-Z0-9() ]", "")
                                        .trim()
                                        .toLowerCase()
                                        .equals(normalizedCsv))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException(
                                        "Age group not found: " + agName.trim()))
                                .getAgeGroupId()
                );
            }
            attr.setAgeGroupIds(ageGroupIds);
        }

        // ── Flavour ───────────────────────────────────────────────────────
        String flavourName = getCsvString(r, H_FLAVOUR);
        if (!isBlank(flavourName)) {
            attr.setFlavourId(
                    flavourRepository
                            .findByFlavourNameIgnoreCase(flavourName)
                            .orElseThrow(() -> new RuntimeException(
                                    "Flavour not found: " + flavourName))
                            .getFlavourId());
        }

        // ── Storage Condition (single) ────────────────────────────────────
        String storage = getCsvString(r, H_STORAGE_CONDITION);
        if (!isBlank(storage)) {
            attr.setStorageConditionId(
                    storageConditionRepository
                            .findByConditionNameIgnoreCaseAndCategory_CategoryId(storage, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Storage condition not found: " + storage))
                            .getStorageConditionId());
        }

        // ── Country ───────────────────────────────────────────────────────
        String country = getCsvString(r, H_COUNTRY);
        if (!isBlank(country)) {
            attr.setCountryId(
                    countryMasterRepository
                            .findByCountryNameIgnoreCase(country)
                            .orElseThrow(() -> new RuntimeException(
                                    "Country not found: " + country))
                            .getCountryId());
        }

        // ── Certifications — same pattern as NonConsumable ────────────────
        String certCell = getCsvString(r, H_CERTIFICATIONS);
        log.info("Supplement Certification: {}", certCell);
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
            attr.setCertificateDocuments(docs);
        }

        attr.setBrochurePath("NOT_UPLOADED");

        return attr;
    }

    // =========================================================
    // ================= SHARED BUILDERS =======================
    // =========================================================

    private PackagingDetailsDto buildPackaging(
            Long unitPerPack, Long numberOfPacks,
            Long minOrderQty, Long maxOrderQty,
            String packTypeName, Long categoryId, String dosageFormName) {

        PackagingDetailsDto packaging = new PackagingDetailsDto();
        packaging.setUnitPerPack(unitPerPack);
        packaging.setNumberOfPacks(numberOfPacks);
        if (unitPerPack != null && numberOfPacks != null) {
            packaging.setPackSize(unitPerPack * numberOfPacks);
        }
        packaging.setMinimumOrderQuantity(minOrderQty);
        packaging.setMaximumOrderQuantity(maxOrderQty);

        if (!isBlank(packTypeName) && !isBlank(dosageFormName)) {
            Long packId = packTypeRepository
                    .findByPackTypeIgnoreCaseAndCategory_CategoryIdAndDosageForm_DosageName(
                            packTypeName, categoryId, dosageFormName)
                    .orElseThrow(() -> new RuntimeException(
                            "Pack type '" + packTypeName + "' not found for dosage form '" + dosageFormName + "'"))
                    .getPackId();
            packaging.setPackId(packId);
        }

        return packaging;
    }

    private PricingDetailsDto buildPricing(
            String batchNumber, LocalDateTime mfgDate, LocalDateTime expiryDate,
            Long stockQty, LocalDate dateOfEntry,
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
    // ================= SHELF LIFE ============================
    // =========================================================

    private Long computeShelfLifeMonths(LocalDate mfgDate, LocalDate expiryDate) {
        if (mfgDate == null || expiryDate == null) return null;
        return ChronoUnit.MONTHS.between(mfgDate, expiryDate);
    }

    // =========================================================
    // ================= ADDITIONAL DISCOUNT VALIDATION ========
    // =========================================================

    private void validateAdditionalDiscountSlab(
            int slabNumber,
            Long mpq, Long discountPct,
            Long mainMoq, Long mainMaxQty, Long mainDiscountPct,
            LocalDate startDate, LocalDate endDate,
            Set<Long> seenMpqs) {

        List<String> errors = new ArrayList<>();
        String prefix = "Additional Discount Slab " + slabNumber + ": ";

        if (mpq == null) {
            errors.add(prefix + "MPQ is required");
        } else {
            if (mpq <= 0)
                errors.add(prefix + "MPQ must be greater than 0");
            if (mainMoq != null && mpq <= mainMoq)
                errors.add(prefix + "MPQ (" + mpq + ") must be greater than main Minimum Order Quantity (" + mainMoq + ")");
            if (mainMaxQty != null && mpq > mainMaxQty)
                errors.add(prefix + "MPQ (" + mpq + ") must be ≤ main Maximum Order Quantity (" + mainMaxQty + ")");
            if (!seenMpqs.add(mpq))
                errors.add(prefix + "Duplicate MPQ value (" + mpq + ") — each slab must have a unique MPQ");
        }

        if (discountPct == null) {
            if (mpq != null)
                errors.add(prefix + "Discount % cannot be blank when MPQ is entered");
        } else {
            if (discountPct <= 0 || discountPct > 100)
                errors.add(prefix + "Discount % must be between 1 and 100");
            if (mainDiscountPct != null && discountPct < mainDiscountPct)
                errors.add(prefix + "Discount % (" + discountPct + ") must be ≥ main Discount % (" + mainDiscountPct + ")");
        }

        if (startDate == null)
            errors.add(prefix + "Effective Start Date is mandatory for an additional discount slab");
        if (endDate == null)
            errors.add(prefix + "Effective End Date is mandatory for an additional discount slab");
        if (startDate != null && endDate != null && endDate.isBefore(startDate))
            errors.add(prefix + "Effective End Date cannot be before Effective Start Date");

        if (!errors.isEmpty()) throw new ValidationException(errors);
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

        // ── Therapeutic Category / Sub Category ───────────────────────────
        validateRequired(getString(row, COL_THERAPEUTIC_CAT), "Therapeutic Category", errors);
        validateRequired(getString(row, COL_THERAPEUTIC_SUBCAT), "Therapeutic Sub Category", errors);

        // ── Brand Name ────────────────────────────────────────────────────
        String brandName = getString(row, COL_BRAND_NAME);
        validateRequired(brandName, "Brand Name", errors);
        if (!isBlank(brandName) && brandName.length() > 60)
            errors.add("Brand Name must not exceed 60 characters");

        // ── Variant Name (optional) ───────────────────────────────────────
        String variantName = getString(row, COL_VARIANT_NAME);
        if (!isBlank(variantName) && variantName.length() > 60)
            errors.add("Variant Name must not exceed 60 characters");

        // ── Dosage Form ───────────────────────────────────────────────────
        validateRequired(getString(row, COL_DOSAGE_FORM), "Dosage Form", errors);

        // ── Net Quantity ──────────────────────────────────────────────────
        String netQuantityRaw = getString(row, COL_NET_QUANTITY);
        Double netQuantityVal = getDouble(row, COL_NET_QUANTITY);
        validateRequired(netQuantityRaw, "Net Quantity", errors);
        if (!isBlank(netQuantityRaw)) {
            if (netQuantityVal == null)
                errors.add("Net Quantity must be a numeric or decimal value");
            else if (netQuantityVal <= 0)
                errors.add("Net Quantity must be greater than 0");
            if (netQuantityRaw.length() > 10)
                errors.add("Net Quantity must not exceed 10 characters");
        }

        // ── Net Quantity Unit ─────────────────────────────────────────────
        validateRequired(getString(row, COL_NET_QUANTITY_UNIT), "Net Quantity Unit", errors);

        // ── Serving Size ──────────────────────────────────────────────────
        // COMMENTED OUT - Serving Size is now non-mandatory
        // String servingSizeRaw = getString(row, COL_SERVING_SIZE);
        // Double servingSizeVal = getDouble(row, COL_SERVING_SIZE);
        // validateRequired(servingSizeRaw, "Serving Size", errors);
        // if (!isBlank(servingSizeRaw)) {
        //     if (servingSizeVal == null)
        //         errors.add("Serving Size must be a numeric or decimal value");
        //     else if (servingSizeVal <= 0)
        //         errors.add("Serving Size must be greater than 0");
        //     if (servingSizeRaw.length() > 10)
        //         errors.add("Serving Size must not exceed 10 characters");
        //     if (servingSizeVal != null) {
        //         String[] parts = servingSizeRaw.split("\\.");
        //         if (parts.length == 2 && parts[1].length() > 2)
        //             errors.add("Serving Size must not have more than 2 decimal places");
        //     }
        // }

        // ── Serving Size Unit ─────────────────────────────────────────────
        // COMMENTED OUT - Serving Size Unit is now non-mandatory
        // validateRequired(getString(row, COL_SERVING_SIZE_UNIT), "Serving Size Unit", errors);

        // ── Strength / Composition ────────────────────────────────────────
        validateRequired(getString(row, COL_STRENGTH), "Strength / Composition", errors);

        // ── Active Ingredients ────────────────────────────────────────────
        String activeIngredients = getString(row, COL_ACTIVE_INGREDIENTS);
        validateRequired(activeIngredients, "Active Ingredients", errors);
        if (!isBlank(activeIngredients) && activeIngredients.length() > 1000)
            errors.add("Active Ingredients must not exceed 1000 characters");

        // ── Excipients / Other Ingredients (optional) ─────────────────────
        String otherIngredients = getString(row, COL_OTHER_INGREDIENTS);
        if (!isBlank(otherIngredients)) {
            if (!otherIngredients.matches("[A-Za-z0-9 ,\\-]+"))
                errors.add("Excipients / Other Ingredients must contain only alphabets, numbers, and spaces");
        }

        // ── Nutritional Information Table ─────────────────────────────────
        String nutritionalInfo = getString(row, COL_NUTRITIONAL_INFO);
        validateRequired(nutritionalInfo, "Nutritional Information Table", errors);
        if (!isBlank(nutritionalInfo) && !VALID_NUTRITIONAL_INFO_VALUES.contains(nutritionalInfo.toLowerCase()))
            errors.add("Nutritional Information Table must be one of: 'As per the label', 'Image upload'");

        // ── Intended Use ──────────────────────────────────────────────────
        String intendedUse = getString(row, COL_INTENDED_USE);
        validateRequired(intendedUse, "Intended Use / Health Benefit", errors);
        if (!isBlank(intendedUse)) {
            if (intendedUse.length() < 10) errors.add("Intended Use / Health Benefit must be at least 10 characters");
            if (intendedUse.length() > 1000)
                errors.add("Intended Use / Health Benefit must not exceed 1000 characters");
        }

        // ── Age Group ─────────────────────────────────────────────────────
        validateRequired(getString(row, COL_AGE_GROUP), "Age Group", errors);

        // ── Gender ────────────────────────────────────────────────────────
        String gender = getString(row, COL_GENDER);
        validateRequired(gender, "Gender", errors);
        if (!isBlank(gender) && !VALID_GENDER_VALUES.contains(gender.toLowerCase()))
            errors.add("Gender must be one of: Male, Female, Unisex");

        // ── Veg / Non-Veg ─────────────────────────────────────────────────
        String vegNonVeg = getString(row, COL_VEG_NON_VEG);
        validateRequired(vegNonVeg, "Veg / Non-Veg Indicator", errors);
        if (!isBlank(vegNonVeg) && !VALID_VEG_NON_VEG_VALUES.contains(vegNonVeg.toLowerCase()))
            errors.add("Veg / Non-Veg Indicator must be one of: Veg, Non-Veg");

        // ── Allergen Information ──────────────────────────────────────────
        String allergenInfo = getString(row, COL_ALLERGEN_INFO);
        validateRequired(allergenInfo, "Allergen Information", errors);
        if (!isBlank(allergenInfo)) {
            if (allergenInfo.length() < 3) errors.add("Allergen Information must be at least 3 characters");
            if (allergenInfo.length() > 500) errors.add("Allergen Information must not exceed 500 characters");
        }

        // ── Flavour ───────────────────────────────────────────────────────
        validateRequired(getString(row, COL_FLAVOUR), "Flavour", errors);

        // ── Product Claims ────────────────────────────────────────────────
        String productClaims = getString(row, COL_PRODUCT_CLAIMS);
        validateRequired(productClaims, "Product Claims", errors);
        if (!isBlank(productClaims)) {
            if (!productClaims.matches("[A-Za-z0-9 ,\\-]+"))
                errors.add("Product Claims must contain only alphabets, numbers, spaces, hyphens, and commas");
            if (productClaims.length() > 1000)
                errors.add("Product Claims must not exceed 1000 characters");
        }

        // ── Warnings / Precautions ────────────────────────────────────────
        String warnings = getString(row, COL_WARNINGS);
        validateRequired(warnings, "Warnings / Precautions", errors);
        if (!isBlank(warnings) && warnings.length() > 1000)
            errors.add("Warnings / Precautions must not exceed 1000 characters");

        // ── Product Description ───────────────────────────────────────────
        String description = getString(row, COL_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (!isBlank(description)) {
            if (description.length() < 10) errors.add("Product Description must be at least 10 characters");
            if (description.length() > 1000) errors.add("Product Description must not exceed 1000 characters");
        }

        // ── Storage Condition ─────────────────────────────────────────────
        validateRequired(getString(row, COL_STORAGE_CONDITION), "Storage Condition", errors);

        // ── Manufacturer ──────────────────────────────────────────────────
        String manufacturer = getString(row, COL_MANUFACTURER);
        validateRequired(manufacturer, "Manufacturer Name", errors);
        if (!isBlank(manufacturer) && manufacturer.length() > 100)
            errors.add("Manufacturer Name must not exceed 100 characters");

        // ── Country ───────────────────────────────────────────────────────
        validateRequired(getString(row, COL_COUNTRY), "Country of Origin", errors);

        // ── Certifications ────────────────────────────────────────────────
        validateRequired(getString(row, COL_CERTIFICATIONS), "Certifications / Compliance", errors);

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
        Long numberOfPacks = getLong(row, COL_NUMBER_OF_PACKS);
        validateRequired(numberOfPacks, "Number of Packs", errors);
        if (numberOfPacks != null && numberOfPacks <= 0)
            errors.add("Number of Packs must be a positive value");

        // ── Min / Max Order Quantities ────────────────────────────────────
        Long minOrderQty = getLong(row, COL_MIN_ORDER_QTY);
        Long maxOrderQty = getLong(row, COL_MAX_ORDER_QTY);

        validateRequired(minOrderQty, "Minimum Order Qty", errors);
        if (minOrderQty != null && minOrderQty <= 0)
            errors.add("Minimum Order Qty must be a positive value");

        validateRequired(maxOrderQty, "Max Order Qty", errors);
        if (maxOrderQty != null && maxOrderQty <= 0)
            errors.add("Max Order Qty must be a positive value");

        if (minOrderQty != null && maxOrderQty != null
                && minOrderQty > 0 && maxOrderQty > 0
                && minOrderQty > maxOrderQty)
            errors.add("Minimum Order Qty must be ≤ Maximum Order Qty");

        // ── Batch Number ──────────────────────────────────────────────────
        String batchNumber = getString(row, COL_BATCH_NUMBER);
        validateRequired(batchNumber, "Batch Number", errors);
        if (!isBlank(batchNumber)) {
            if (!batchNumber.matches("[A-Za-z0-9]+"))
                errors.add("Batch Number must be alphanumeric only (no special characters)");
            if (batchNumber.length() < 3)
                errors.add("Batch Number must be at least 3 characters");
            if (batchNumber.length() > 20)
                errors.add("Batch Number must not exceed 20 characters");
        }

        // ── Manufacturing Date ────────────────────────────────────────────
        LocalDate mfgDate = getDate(row, COL_MFG_DATE);
        validateRequired(mfgDate, "Manufacturing Date", errors);
        if (mfgDate != null && YearMonth.from(mfgDate).isAfter(YearMonth.now()))
            errors.add("Manufacturing Date cannot be a future month");

        // ── Expiry Date ───────────────────────────────────────────────────
        LocalDate expiryDate = getDate(row, COL_EXPIRY_DATE);
        validateRequired(expiryDate, "Expiry Date", errors);
        if (mfgDate != null && expiryDate != null && expiryDate.isBefore(mfgDate))
            errors.add("Expiry Date cannot be before Manufacturing Date");

        // ── Stock Quantity ────────────────────────────────────────────────
        Long stockQty = getLong(row, COL_STOCK_QTY);
        validateRequired(stockQty, "Stock Quantity", errors);
        if (stockQty != null && stockQty <= 0)
            errors.add("Stock Quantity must be a positive value");
        if (stockQty != null && minOrderQty != null && stockQty < minOrderQty)
            errors.add("Stock Quantity (" + stockQty + ") must be ≥ Minimum Order Quantity (" + minOrderQty + ")");

        // ── MRP ───────────────────────────────────────────────────────────
        Long mrp = getLong(row, COL_MRP);
        validateRequired(mrp, "MRP", errors);
        if (mrp != null && mrp <= 0)
            errors.add("MRP must be greater than 0");

        // ── Selling Price ─────────────────────────────────────────────────
        Long sellingPrice = getLong(row, COL_SELLING_PRICE);
        validateRequired(sellingPrice, "Selling Price", errors);
        if (sellingPrice != null && sellingPrice <= 0)
            errors.add("Selling Price must be greater than 0");
        if (sellingPrice != null && mrp != null && sellingPrice > mrp)
            errors.add("Selling Price cannot be greater than MRP");

        // ── Discount % ────────────────────────────────────────────────────
        Long discountPct = getLong(row, COL_DISCOUNT_PCT);
        if (discountPct != null && (discountPct < 0 || discountPct > 100))
            errors.add("Discount % must be in the range 0–100");

        // ── GST % ─────────────────────────────────────────────────────────
        Long gstPct = getLong(row, COL_GST_PCT);
        validateRequired(gstPct, "GST %", errors);
        if (gstPct != null && !VALID_GST_VALUES.contains(gstPct))
            errors.add("GST % must be one of: 0, 5, 12, 18");

        // ── HSN Code ──────────────────────────────────────────────────────
        Long hsnCode = getLong(row, COL_HSN_CODE);
        validateRequired(hsnCode, "HSN Code", errors);
        if (hsnCode != null) {
            int digits = String.valueOf(hsnCode).length();
            if (digits != 4 && digits != 6 && digits != 8)
                errors.add("HSN Code must be 4, 6, or 8 digits");
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

        // ── Therapeutic Category / Sub Category ───────────────────────────
        validateRequired(getCsvString(r, H_THERAPEUTIC_CAT), "Therapeutic Category", errors);
        validateRequired(getCsvString(r, H_THERAPEUTIC_SUBCAT), "Therapeutic Sub Category", errors);

        // ── Brand Name ────────────────────────────────────────────────────
        String brandName = getCsvString(r, H_BRAND_NAME);
        validateRequired(brandName, "Brand Name", errors);
        if (!isBlank(brandName) && brandName.length() > 60)
            errors.add("Brand Name must not exceed 60 characters");

        // ── Variant Name (optional) ───────────────────────────────────────
        String variantName = getCsvString(r, H_VARIANT_NAME);
        if (!isBlank(variantName) && variantName.length() > 60)
            errors.add("Variant Name must not exceed 60 characters");

        // ── Dosage Form ───────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_DOSAGE_FORM), "Dosage Form", errors);

        // ── Net Quantity ──────────────────────────────────────────────────
        String netQuantityRaw = getCsvString(r, H_NET_QUANTITY);
        Double netQuantityVal = getCsvDouble(r, H_NET_QUANTITY);
        validateRequired(netQuantityRaw, "Net Quantity", errors);
        if (!isBlank(netQuantityRaw)) {
            if (netQuantityVal == null)
                errors.add("Net Quantity must be a numeric or decimal value");
            else if (netQuantityVal <= 0)
                errors.add("Net Quantity must be greater than 0");
            if (netQuantityRaw.length() > 10)
                errors.add("Net Quantity must not exceed 10 characters");
        }

        // ── Net Quantity Unit ─────────────────────────────────────────────
        validateRequired(getCsvString(r, H_NET_QUANTITY_UNIT), "Net Quantity Unit", errors);

        // ── Serving Size ──────────────────────────────────────────────────
        // COMMENTED OUT - Serving Size is now non-mandatory
        // String servingSizeRaw = getCsvString(r, H_SERVING_SIZE);
        // Double servingSizeVal = getCsvDouble(r, H_SERVING_SIZE);
        // validateRequired(servingSizeRaw, "Serving Size", errors);
        // if (!isBlank(servingSizeRaw)) {
        //     if (servingSizeVal == null)
        //         errors.add("Serving Size must be a numeric or decimal value");
        //     else if (servingSizeVal <= 0)
        //         errors.add("Serving Size must be greater than 0");
        //     if (servingSizeRaw.length() > 10)
        //         errors.add("Serving Size must not exceed 10 characters");
        //     if (servingSizeVal != null) {
        //         String[] parts = servingSizeRaw.split("\\.");
        //         if (parts.length == 2 && parts[1].length() > 2)
        //             errors.add("Serving Size must not have more than 2 decimal places");
        //     }
        // }

        // ── Serving Size Unit ─────────────────────────────────────────────
        // COMMENTED OUT - Serving Size Unit is now non-mandatory
        // validateRequired(getCsvString(r, H_SERVING_SIZE_UNIT), "Serving Size Unit", errors);

        // ── Strength / Composition ────────────────────────────────────────
        validateRequired(getCsvString(r, H_STRENGTH), "Strength / Composition", errors);

        // ── Active Ingredients ────────────────────────────────────────────
        String activeIngredients = getCsvString(r, H_ACTIVE_INGREDIENTS);
        validateRequired(activeIngredients, "Active Ingredients", errors);
        if (!isBlank(activeIngredients) && activeIngredients.length() > 1000)
            errors.add("Active Ingredients must not exceed 1000 characters");

        // ── Excipients / Other Ingredients (optional) ─────────────────────
        String otherIngredients = getCsvString(r, H_OTHER_INGREDIENTS);
        if (!isBlank(otherIngredients)) {
            if (!otherIngredients.matches("[A-Za-z0-9 ,\\-]+"))
                errors.add("Excipients / Other Ingredients must contain only alphabets, numbers, and spaces");
        }

        // ── Nutritional Information Table ─────────────────────────────────
        String nutritionalInfo = getCsvString(r, H_NUTRITIONAL_INFO);
        validateRequired(nutritionalInfo, "Nutritional Information Table", errors);
        if (!isBlank(nutritionalInfo) && !VALID_NUTRITIONAL_INFO_VALUES.contains(nutritionalInfo.toLowerCase()))
            errors.add("Nutritional Information Table must be one of: 'As per the label', 'Image upload'");

        // ── Intended Use ──────────────────────────────────────────────────
        String intendedUse = getCsvString(r, H_INTENDED_USE);
        validateRequired(intendedUse, "Intended Use / Health Benefit", errors);
        if (!isBlank(intendedUse)) {
            if (intendedUse.length() < 10) errors.add("Intended Use / Health Benefit must be at least 10 characters");
            if (intendedUse.length() > 1000)
                errors.add("Intended Use / Health Benefit must not exceed 1000 characters");
        }

        // ── Age Group ─────────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_AGE_GROUP), "Age Group", errors);

        // ── Gender ────────────────────────────────────────────────────────
        String gender = getCsvString(r, H_GENDER);
        validateRequired(gender, "Gender", errors);
        if (!isBlank(gender) && !VALID_GENDER_VALUES.contains(gender.toLowerCase()))
            errors.add("Gender must be one of: Male, Female, Unisex");

        // ── Veg / Non-Veg ─────────────────────────────────────────────────
        String vegNonVeg = getCsvString(r, H_VEG_NON_VEG);
        validateRequired(vegNonVeg, "Veg / Non-Veg Indicator", errors);
        if (!isBlank(vegNonVeg) && !VALID_VEG_NON_VEG_VALUES.contains(vegNonVeg.toLowerCase()))
            errors.add("Veg / Non-Veg Indicator must be one of: Veg, Non-Veg");

        // ── Allergen Information ──────────────────────────────────────────
        String allergenInfo = getCsvString(r, H_ALLERGEN_INFO);
        validateRequired(allergenInfo, "Allergen Information", errors);
        if (!isBlank(allergenInfo)) {
            if (allergenInfo.length() < 3) errors.add("Allergen Information must be at least 3 characters");
            if (allergenInfo.length() > 500) errors.add("Allergen Information must not exceed 500 characters");
        }

        // ── Flavour ───────────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_FLAVOUR), "Flavour", errors);

        // ── Product Claims ────────────────────────────────────────────────
        String productClaims = getCsvString(r, H_PRODUCT_CLAIMS);
        validateRequired(productClaims, "Product Claims", errors);
        if (!isBlank(productClaims)) {
            if (!productClaims.matches("[A-Za-z0-9 ,\\-]+"))
                errors.add("Product Claims must contain only alphabets, numbers, spaces, hyphens, and commas");
            if (productClaims.length() > 1000)
                errors.add("Product Claims must not exceed 1000 characters");
        }

        // ── Warnings / Precautions ────────────────────────────────────────
        String warnings = getCsvString(r, H_WARNINGS);
        validateRequired(warnings, "Warnings / Precautions", errors);
        if (!isBlank(warnings) && warnings.length() > 1000)
            errors.add("Warnings / Precautions must not exceed 1000 characters");

        // ── Product Description ───────────────────────────────────────────
        String description = getCsvString(r, H_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (!isBlank(description)) {
            if (description.length() < 10) errors.add("Product Description must be at least 10 characters");
            if (description.length() > 1000) errors.add("Product Description must not exceed 1000 characters");
        }

        // ── Storage Condition ─────────────────────────────────────────────
        validateRequired(getCsvString(r, H_STORAGE_CONDITION), "Storage Condition", errors);

        // ── Manufacturer ──────────────────────────────────────────────────
        String manufacturer = getCsvString(r, H_MANUFACTURER);
        validateRequired(manufacturer, "Manufacturer Name", errors);
        if (!isBlank(manufacturer) && manufacturer.length() > 100)
            errors.add("Manufacturer Name must not exceed 100 characters");

        // ── Country ───────────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_COUNTRY), "Country of Origin", errors);

        // ── Certifications ────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_CERTIFICATIONS), "Certifications / Compliance", errors);

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
        Long numberOfPacks = getCsvLong(r, H_NUMBER_OF_PACKS);
        validateRequired(numberOfPacks, "Number of Packs", errors);
        if (numberOfPacks != null && numberOfPacks <= 0)
            errors.add("Number of Packs must be a positive value");

        // ── Min / Max Order Quantities ────────────────────────────────────
        Long minOrderQty = getCsvLong(r, H_MIN_ORDER_QTY);
        Long maxOrderQty = getCsvLong(r, H_MAX_ORDER_QTY);

        validateRequired(minOrderQty, "Minimum Order Qty", errors);
        if (minOrderQty != null && minOrderQty <= 0)
            errors.add("Minimum Order Qty must be a positive value");

        validateRequired(maxOrderQty, "Max Order Qty", errors);
        if (maxOrderQty != null && maxOrderQty <= 0)
            errors.add("Max Order Qty must be a positive value");

        if (minOrderQty != null && maxOrderQty != null
                && minOrderQty > 0 && maxOrderQty > 0
                && minOrderQty > maxOrderQty)
            errors.add("Minimum Order Qty must be ≤ Maximum Order Qty");

        // ── Batch Number ──────────────────────────────────────────────────
        String batchNumber = getCsvString(r, H_BATCH_NUMBER);
        validateRequired(batchNumber, "Batch Number", errors);
        if (!isBlank(batchNumber)) {
            if (!batchNumber.matches("[A-Za-z0-9]+"))
                errors.add("Batch Number must be alphanumeric only (no special characters)");
            if (batchNumber.length() < 3)
                errors.add("Batch Number must be at least 3 characters");
            if (batchNumber.length() > 20)
                errors.add("Batch Number must not exceed 20 characters");
        }

        // ── Manufacturing Date ────────────────────────────────────────────
        LocalDate mfgDate = parseCsvDate(getCsvString(r, H_MFG_DATE));
        validateRequired(mfgDate, "Manufacturing Date", errors);
        if (mfgDate != null && YearMonth.from(mfgDate).isAfter(YearMonth.now()))
            errors.add("Manufacturing Date cannot be a future month");

        // ── Expiry Date ───────────────────────────────────────────────────
        LocalDate expiryDate = parseCsvDate(getCsvString(r, H_EXPIRY_DATE));
        validateRequired(expiryDate, "Expiry Date", errors);
        if (mfgDate != null && expiryDate != null && expiryDate.isBefore(mfgDate))
            errors.add("Expiry Date cannot be before Manufacturing Date");

        // ── Stock Quantity ────────────────────────────────────────────────
        Long stockQty = getCsvLong(r, H_STOCK_QTY);
        validateRequired(stockQty, "Stock Quantity", errors);
        if (stockQty != null && stockQty <= 0)
            errors.add("Stock Quantity must be a positive value");
        if (stockQty != null && minOrderQty != null && stockQty < minOrderQty)
            errors.add("Stock Quantity (" + stockQty + ") must be ≥ Minimum Order Quantity (" + minOrderQty + ")");

        // ── MRP ───────────────────────────────────────────────────────────
        Long mrp = getCsvLong(r, H_MRP);
        validateRequired(mrp, "MRP", errors);
        if (mrp != null && mrp <= 0)
            errors.add("MRP must be greater than 0");

        // ── Selling Price ─────────────────────────────────────────────────
        Long sellingPrice = getCsvLong(r, H_SELLING_PRICE);
        validateRequired(sellingPrice, "Selling Price", errors);
        if (sellingPrice != null && sellingPrice <= 0)
            errors.add("Selling Price must be greater than 0");
        if (sellingPrice != null && mrp != null && sellingPrice > mrp)
            errors.add("Selling Price cannot be greater than MRP");

        // ── Discount % ────────────────────────────────────────────────────
        Long discountPct = getCsvLong(r, H_DISCOUNT_PCT);
        if (discountPct != null && (discountPct < 0 || discountPct > 100))
            errors.add("Discount % must be in the range 0–100");

        // ── GST % ─────────────────────────────────────────────────────────
        Long gstPct = getCsvLong(r, H_GST_PCT);
        validateRequired(gstPct, "GST %", errors);
        if (gstPct != null && !VALID_GST_VALUES.contains(gstPct))
            errors.add("GST % must be one of: 0, 5, 12, 18");

        // ── HSN Code ──────────────────────────────────────────────────────
        Long hsnCode = getCsvLong(r, H_HSN_CODE);
        validateRequired(hsnCode, "HSN Code", errors);
        if (hsnCode != null) {
            int digits = String.valueOf(hsnCode).length();
            if (digits != 4 && digits != 6 && digits != 8)
                errors.add("HSN Code must be 4, 6, or 8 digits");
        }

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    // =========================================================
    // ================= EXCEL HELPERS =========================
    // =========================================================

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell != null ? cell.toString().trim() : null;
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

    private Long getLong(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case STRING:
                    String s = cell.getStringCellValue().trim();
                    return s.isEmpty() ? null : Long.parseLong(s);
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

    private Double getCsvDouble(CSVRecord r, String header) {
        try {
            String v = getCsvString(r, header);
            return v != null ? Double.parseDouble(v) : null;
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

    private Long getCsvLongByIndex(CSVRecord r, int index) {
        try {
            String v = getCsvStringByIndex(r, index);
            return v != null ? Long.parseLong(v) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Supports: "Sep-25" (MMM-yy), "04-03-2026" (dd-MM-yyyy), "2026-03-01" (ISO), "M/d/yyyy"
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
    // ================= SHARED UTILS ==========================
    // =========================================================

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59) : null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void validateRequired(Object value, String field, List<String> errors) {
        if (value == null) {
            errors.add(field + " is mandatory");
        } else if (value instanceof String && ((String) value).isBlank()) {
            errors.add(field + " is mandatory");
        }
    }
}