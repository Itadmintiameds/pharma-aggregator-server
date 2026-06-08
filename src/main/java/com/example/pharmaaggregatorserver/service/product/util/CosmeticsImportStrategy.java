package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.exception.ValidationException;
import com.example.pharmaaggregatorserver.repository.product.*;
import com.example.pharmaaggregatorserver.service.product.PricingDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component("COSMETIC_AND_PERSONAL_USE")
@RequiredArgsConstructor
public class CosmeticsImportStrategy implements ProductImportStrategy {

    private final AgeGroupMasterRepository ageGroupMasterRepository;
    private final CountryMasterRepository countryMasterRepository;
    private final StorageConditionMasterRepository storageConditionRepository;
    private final PackTypeRepository packTypeRepository;
    private final CertificationRepository certificationRepository;
    private final intendedUseAreaRepository intendedUseAreaRepository;
    private final skinTypeRepository skinTypeRepository;
    private final hairTypeRepository hairTypeRepository;
    private final ProductCategoryMasterRepository productCategoryMasterRepository;
    private final ProductSubcategoryMasterRepository productSubCategoryMasterRepository;
    private final NetQuantityUnitRepository netQuantityUnitRepository;
    private final ProductsFormMasterRepository productsFormMasterRepository;
    private final PricingDetailsService pricingDetailsService;

    // ── Valid GST percentages ─────────────────────────────────────────────
    private static final Set<Long> VALID_GST_VALUES = Set.of(0L, 5L, 12L, 18L);

    // ── Column indices (0-based, data starts at row index 2) ──────────────
    private static final int COL_PRODUCT_CATEGORY = 0;
    private static final int COL_PRODUCT_SUBCATEGORY = 1;
    private static final int COL_PRODUCT_NAME = 2;
    private static final int COL_BRAND_NAME = 3;
    private static final int COL_VARIANT_NAME = 4;
    private static final int COL_NET_QUANTITY = 5;
    private static final int COL_ACTIVE_INGREDIENTS = 6;
    private static final int COL_GENDER = 7;
    private static final int COL_AGE_GROUP = 8;
    private static final int COL_PRODUCT_CLAIMS = 9;
    private static final int COL_WARNINGS = 10;
    private static final int COL_DESCRIPTION = 11;
    private static final int COL_STORAGE_CONDITION = 12;
    private static final int COL_MANUFACTURER = 13;
    private static final int COL_COUNTRY = 14;
    private static final int COL_CERTIFICATIONS = 15;
    private static final int COL_PACK_TYPE = 16;
    private static final int COL_UNIT_PER_PACK = 17;
    private static final int COL_NUMBER_OF_PACKS = 18;
    private static final int COL_MIN_ORDER_QTY = 19;
    private static final int COL_MAX_ORDER_QTY = 20;
    private static final int COL_BATCH_NUMBER = 21;
    private static final int COL_MFG_DATE = 22;
    private static final int COL_EXPIRY_DATE = 23;
    private static final int COL_STOCK_QTY = 24;
    private static final int COL_MRP = 25;
    private static final int COL_SELLING_PRICE = 26;
    private static final int COL_DISCOUNT_PCT = 27;
    private static final int COL_GST_PCT = 28;
    private static final int COL_HSN_CODE = 29;

    // Cosmetic-specific columns
    private static final int COL_INTENDED_USE_AREA = 30;
    private static final int COL_HAIR_TYPE = 31;
    private static final int COL_SKIN_TYPE = 32;

    // ── Additional discount slabs ─────────────────────────────────────────
    // 4 slabs × 7 cols each, starting at col 33
    // Per slab: [Slab label | MinQty | Discount% | StartDate | StartTime | EndDate | EndTime]
    private static final int COL_NET_QUANTITY_UNIT = 33;
    private static final int COL_PRODUCT_FORM = 34;
    private static final int COL_ADD_DISCOUNT_START = 35;
    private static final int ADD_DISCOUNT_SLAB_SIZE = 7;
    private static final int ADD_DISCOUNT_SLAB_COUNT = 4;

    // ── CSV header names ──────────────────────────────────────────────────
    private static final String H_PRODUCT_CATEGORY = "Product Category*";
    private static final String H_PRODUCT_SUBCATEGORY = "Product Sub Category*";
    private static final String H_PRODUCT_NAME = "Product Name*";
    private static final String H_BRAND_NAME = "Brand Name*";
    private static final String H_VARIANT_NAME = "Variant Name";
    private static final String H_NET_QUANTITY = "Net Quantity*";
    private static final String H_ACTIVE_INGREDIENTS = "Active Ingredients*";
    private static final String H_GENDER = "Gender*";
    private static final String H_AGE_GROUP = "Age Group*";
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
    private static final String H_MRP = "MRP (INR)*";
    private static final String H_SELLING_PRICE = "Selling Price(INR)*";
    private static final String H_DISCOUNT_PCT = "Discount %";
    private static final String H_GST_PCT = "GST %";
    private static final String H_HSN_CODE = "HSN Code*";
    private static final String H_INTENDED_USE_AREA = "Intended Use Area*";
    private static final String H_HAIR_TYPE = "Hair Type";
    private static final String H_SKIN_TYPE = "Skin Type";
    private static final String H_NET_QUANTITY_UNIT = "Net Quantity Unit*";
    private static final String H_PRODUCT_FORM = "Product Form*";

    // Additional discount slabs — duplicate headers in CSV; use index-based access.
    // Slab 0: base 33, Slab 1: base 40, Slab 2: base 47, Slab 3: base 54
    private static final int[] CSV_SLAB_MIN_QTY_COLS = {36, 43, 50, 57}; // was {35,42,49,56}
    private static final int[] CSV_SLAB_DISCOUNT_COLS = {37, 44, 51, 58}; // was {36,43,50,57}
    private static final int[] CSV_SLAB_START_DATE_COLS = {38, 45, 52, 59}; // was {37,44,51,58}
    private static final int[] CSV_SLAB_START_TIME_COLS = {39, 46, 53, 60}; // was {38,45,52,59}
    private static final int[] CSV_SLAB_END_DATE_COLS = {40, 47, 54, 61}; // was {39,46,53,60}
    private static final int[] CSV_SLAB_END_TIME_COLS = {41, 48, 55, 62}; // was {40,47,54,61}

    // =========================================================
    // ================= EXCEL ENTRY POINT =====================
    // =========================================================

    @Override
    public ProductDetailsDto mapRow(Row row, Long categoryId, Long userId) {
        log.info("Cosmetics Excel import Called");

        validateMandatoryExcel(row, categoryId, userId);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getString(row, COL_PRODUCT_NAME));
        dto.setWarningsPrecautions(getString(row, COL_WARNINGS));
        dto.setProductDescription(getString(row, COL_DESCRIPTION));
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));
        dto.setCategoryId(categoryId);

        // ── Packaging ─────────────────────────────────────────────────────
        Long unitPerPack = getLong(row, COL_UNIT_PER_PACK);
        Long numberOfPacks = getLong(row, COL_NUMBER_OF_PACKS);
        String packTypeName = getString(row, COL_PACK_TYPE);

        dto.setPackagingDetails(Set.of(buildPackaging(
                unitPerPack, numberOfPacks,
                getLong(row, COL_MIN_ORDER_QTY),
                getLong(row, COL_MAX_ORDER_QTY),
                packTypeName, categoryId)));

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

        // ── Cosmetic Attributes ───────────────────────────────────────────
        dto.setProductAttributeCosmeticAndPersonalUse(
                Set.of(buildCosmeticAttr(row, categoryId)));

        return dto;
    }

    // =========================================================
    // ================= CSV ENTRY POINT =======================
    // =========================================================

    @Override
    public ProductDetailsDto mapCsv(CSVRecord r, Long categoryId, Long userId) {
        log.info("Cosmetics CSV import Called");

        validateMandatoryCsv(r, categoryId, userId);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getCsvString(r, H_PRODUCT_NAME));
        dto.setWarningsPrecautions(getCsvString(r, H_WARNINGS));
        dto.setProductDescription(getCsvString(r, H_DESCRIPTION));
        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));
        dto.setCategoryId(categoryId);

        // ── Packaging ─────────────────────────────────────────────────────
        Long unitPerPack = getCsvLong(r, H_UNIT_PER_PACK);
        Long numberOfPacks = getCsvLong(r, H_NUMBER_OF_PACKS);
        String packTypeName = getCsvString(r, H_PACK_TYPE);

        dto.setPackagingDetails(Set.of(buildPackaging(
                unitPerPack, numberOfPacks,
                getCsvLong(r, H_MIN_ORDER_QTY),
                getCsvLong(r, H_MAX_ORDER_QTY),
                packTypeName, categoryId)));

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

        // ── Cosmetic Attributes ───────────────────────────────────────────
        dto.setProductAttributeCosmeticAndPersonalUse(
                Set.of(buildCosmeticAttrFromCsv(r, categoryId)));

        return dto;
    }

    // =========================================================
    // ================= COSMETIC ATTR (Excel) =================
    // =========================================================

    private CosmeticAndPersonalUseProductAttributeDTO buildCosmeticAttr(Row row, Long categoryId) {
        CosmeticAndPersonalUseProductAttributeDTO attr = new CosmeticAndPersonalUseProductAttributeDTO();

        // ── Product Category ──────────────────────────────────────────────
        String categoryName = getString(row, COL_PRODUCT_CATEGORY);
        if (!isBlank(categoryName)) {
            productCategoryMasterRepository.findByCategory_CategoryId(categoryId)
                    .stream()
                    .filter(cat -> cat.getProductCategory().equalsIgnoreCase(categoryName))
                    .findFirst()
                    .ifPresent(cat -> attr.setProductCategoryId(cat.getProductCategoryId()));
        }

        // ── Product Subcategory ───────────────────────────────────────────
        String subcategoryName = getString(row, COL_PRODUCT_SUBCATEGORY);
        if (!isBlank(subcategoryName) && attr.getProductCategoryId() != null) {
            productSubCategoryMasterRepository
                    .findByProductCategoryMaster_ProductCategoryId(attr.getProductCategoryId())
                    .stream()
                    .filter(sub -> sub.getProductSubcategory().equalsIgnoreCase(subcategoryName))
                    .findFirst()
                    .ifPresent(sub -> attr.setProductSubcategoryId(sub.getProductSubcategoryId()));
        }

        // ── Basic fields ──────────────────────────────────────────────────
        attr.setBrandName(getString(row, COL_BRAND_NAME));
        attr.setVariantName(getString(row, COL_VARIANT_NAME));
        attr.setGender(getString(row, COL_GENDER));
        attr.setActiveIngredients(getString(row, COL_ACTIVE_INGREDIENTS));
        String netQtyExcel = getString(row, COL_NET_QUANTITY);
        attr.setNetQuantityStrength(
                netQtyExcel != null && !netQtyExcel.isBlank()
                        ? new BigDecimal(netQtyExcel.trim())
                        : null
        );
        // ── Net Quantity Unit ─────────────────────────────────────────────────
        String unitName = getString(row, COL_NET_QUANTITY_UNIT);
        if (!isBlank(unitName)) {
            netQuantityUnitRepository.findByUnitNameIgnoreCaseAndCategory_CategoryId(unitName, categoryId)
                    .ifPresent(u -> attr.setUnitId(u.getUnitId()));
        }

        // ── Product Form ──────────────────────────────────────────────────────
        String formName = getString(row, COL_PRODUCT_FORM);
        if (!isBlank(formName)) {
            productsFormMasterRepository.findByFormNameIgnoreCase(formName)
                    .ifPresent(f -> attr.setFormId(f.getFormId()));
        }

        attr.setProductClaims(getString(row, COL_PRODUCT_CLAIMS));
        attr.setManufacturerName(getString(row, COL_MANUFACTURER));
        attr.setBrochurePath("NOT_UPLOADED");

        // ── Age Group ─────────────────────────────────────────────────────
        String ageGroupName = getString(row, COL_AGE_GROUP);
        if (!isBlank(ageGroupName)) {
            ageGroupMasterRepository.findByAgeGroupIgnoreCase(ageGroupName)
                    .ifPresent(age -> attr.setAgeGroupIds(Collections.singletonList(age.getAgeGroupId())));
        }

        // ── Storage Condition ─────────────────────────────────────────────
        String storage = getString(row, COL_STORAGE_CONDITION);
        if (!isBlank(storage)) {
            storageConditionRepository.findByConditionNameIgnoreCaseAndCategory_CategoryId(storage, categoryId)
                    .ifPresent(sc -> attr.setStorageConditionId(sc.getStorageConditionId()));
        }

        // ── Country ───────────────────────────────────────────────────────
        String country = getString(row, COL_COUNTRY);
        if (!isBlank(country)) {
            countryMasterRepository.findByCountryNameIgnoreCase(country)
                    .ifPresent(c -> attr.setCountryId(c.getCountryId()));
        }

        // ── Intended Use Areas (comma-separated) ──────────────────────────
        String useAreasStr = getString(row, COL_INTENDED_USE_AREA);
        if (!isBlank(useAreasStr)) {
            List<Long> useAreaIds = new ArrayList<>();
            for (String name : useAreasStr.split(",")) {
                String trimmed = name.trim();
                intendedUseAreaRepository.findByAreaNameIgnoreCase(trimmed)
                        .ifPresent(area -> useAreaIds.add(area.getUseAreaId()));
            }
            attr.setUseAreaId(useAreaIds);
        }

        // ── Hair Types (comma-separated) ──────────────────────────────────
        String hairTypesStr = getString(row, COL_HAIR_TYPE);
        if (!isBlank(hairTypesStr)) {
            List<Long> hairTypeIds = new ArrayList<>();
            for (String name : hairTypesStr.split(",")) {
                String trimmed = name.trim();
                hairTypeRepository.findByTypeNameIgnoreCase(trimmed)
                        .ifPresent(hair -> hairTypeIds.add(hair.getTypeId()));
            }
            attr.setTypeId(hairTypeIds);
        }

        // ── Skin Types (comma-separated) ──────────────────────────────────
        String skinTypesStr = getString(row, COL_SKIN_TYPE);
        if (!isBlank(skinTypesStr)) {
            List<Long> skinTypeIds = new ArrayList<>();
            for (String name : skinTypesStr.split(",")) {
                String trimmed = name.trim();
                skinTypeRepository.findByTypeNameIgnoreCase(trimmed)
                        .ifPresent(skin -> skinTypeIds.add(skin.getSkintypeId()));
            }
            attr.setSkintypeId(skinTypeIds);
        }

        // ── Certifications ────────────────────────────────────────────────
        String certCell = getString(row, COL_CERTIFICATIONS);
        if (!isBlank(certCell)) {
            List<ProductCertificateDocumentDto> docs = new ArrayList<>();
            for (String name : certCell.split(",")) {
                String trimmed = name.trim();
                certificationRepository.findByCertificationNameIgnoreCaseAndCategory_CategoryId(trimmed, categoryId)
                        .ifPresent(cert -> {
                            ProductCertificateDocumentDto d = new ProductCertificateDocumentDto();
                            d.setCertificationId(cert.getCertificationId());
                            d.setCertificateUrl("NOT_UPLOADED");
                            docs.add(d);
                        });
            }
            attr.setCertificateDocuments(docs);
        }

        return attr;
    }

    // =========================================================
    // ================= COSMETIC ATTR (CSV) ===================
    // =========================================================

    private CosmeticAndPersonalUseProductAttributeDTO buildCosmeticAttrFromCsv(CSVRecord r, Long categoryId) {
        CosmeticAndPersonalUseProductAttributeDTO attr = new CosmeticAndPersonalUseProductAttributeDTO();

        // ── Product Category ──────────────────────────────────────────────
        String categoryName = getCsvString(r, H_PRODUCT_CATEGORY);
        if (!isBlank(categoryName)) {
            attr.setProductCategoryId(
                    productCategoryMasterRepository
                            .findByProductCategoryIgnoreCaseAndCategory_CategoryId(categoryName, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Product Category not found: " + categoryName))
                            .getProductCategoryId());
        }

        // ── Product Subcategory ───────────────────────────────────────────
        String subcategoryName = getCsvString(r, H_PRODUCT_SUBCATEGORY);
        if (!isBlank(subcategoryName) && attr.getProductCategoryId() != null) {
            attr.setProductSubcategoryId(
                    productSubCategoryMasterRepository
                            .findByProductSubcategoryIgnoreCaseAndProductCategoryMaster_ProductCategoryId(
                                    subcategoryName, attr.getProductCategoryId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Product Subcategory not found: " + subcategoryName))
                            .getProductSubcategoryId());
        }

        // ── Basic fields ──────────────────────────────────────────────────
        attr.setBrandName(getCsvString(r, H_BRAND_NAME));
        attr.setVariantName(getCsvString(r, H_VARIANT_NAME));
        attr.setGender(getCsvString(r, H_GENDER));
        attr.setActiveIngredients(getCsvString(r, H_ACTIVE_INGREDIENTS));
        //attr.setNetQuantityStrength(getCsvString(r, H_NET_QUANTITY));
        String netQtyCsv = getCsvString(r, H_NET_QUANTITY);
        attr.setNetQuantityStrength(
                netQtyCsv != null && !netQtyCsv.isBlank()
                        ? new BigDecimal(netQtyCsv.trim())
                        : null
        );
        // ── Net Quantity Unit ─────────────────────────────────────────────────
        String unitName = getCsvString(r, H_NET_QUANTITY_UNIT);
        if (!isBlank(unitName)) {
            attr.setUnitId(
                    netQuantityUnitRepository.findByUnitNameIgnoreCaseAndCategory_CategoryId(unitName, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Net Quantity Unit not found: " + unitName))
                            .getUnitId());
        }

        // ── Product Form ──────────────────────────────────────────────────────
        String formName = getCsvString(r, H_PRODUCT_FORM);
        if (!isBlank(formName)) {
            attr.setFormId(
                    productsFormMasterRepository.findByFormNameIgnoreCase(formName)
                            .orElseThrow(() -> new RuntimeException(
                                    "Product Form not found: " + formName))
                            .getFormId());
        }

        attr.setProductClaims(getCsvString(r, H_PRODUCT_CLAIMS));
        attr.setManufacturerName(getCsvString(r, H_MANUFACTURER));
        attr.setBrochurePath("NOT_UPLOADED");

        // ── Age Group ─────────────────────────────────────────────────────
        String ageGroupName = getCsvString(r, H_AGE_GROUP);
        if (!isBlank(ageGroupName)) {
            String normalizedCsv = ageGroupName.replaceAll("[^a-zA-Z0-9() ]", "").trim().toLowerCase();
            attr.setAgeGroupIds(
                    Collections.singletonList(
                            ageGroupMasterRepository.findAll().stream()
                                    .filter(ag -> ag.getAgeGroup()
                                            .replaceAll("[^a-zA-Z0-9() ]", "")
                                            .trim()
                                            .toLowerCase()
                                            .equals(normalizedCsv))
                                    .findFirst()
                                    .orElseThrow(() -> new RuntimeException("Age group not found: " + ageGroupName))
                                    .getAgeGroupId()
                    )
            );
        }

        // ── Storage Condition ─────────────────────────────────────────────
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

        // ── Intended Use Areas ────────────────────────────────────────────
        String useAreasStr = getCsvString(r, H_INTENDED_USE_AREA);
        if (!isBlank(useAreasStr)) {
            List<Long> useAreaIds = new ArrayList<>();
            for (String name : useAreasStr.split(",")) {
                String trimmed = name.trim();
                useAreaIds.add(
                        intendedUseAreaRepository.findByAreaNameIgnoreCase(trimmed)
                                .orElseThrow(() -> new RuntimeException(
                                        "Intended Use Area not found: " + trimmed))
                                .getUseAreaId());
            }
            attr.setUseAreaId(useAreaIds);
        }

        // ── Hair Types ────────────────────────────────────────────────────
        String hairTypesStr = getCsvString(r, H_HAIR_TYPE);
        if (!isBlank(hairTypesStr)) {
            List<Long> hairTypeIds = new ArrayList<>();
            for (String name : hairTypesStr.split(",")) {
                String trimmed = name.trim();
                hairTypeIds.add(
                        hairTypeRepository.findByTypeNameIgnoreCase(trimmed)
                                .orElseThrow(() -> new RuntimeException(
                                        "Hair Type not found: " + trimmed))
                                .getTypeId());
            }
            attr.setTypeId(hairTypeIds);
        }

        // ── Skin Types ────────────────────────────────────────────────────
        String skinTypesStr = getCsvString(r, H_SKIN_TYPE);
        if (!isBlank(skinTypesStr)) {
            List<Long> skinTypeIds = new ArrayList<>();
            for (String name : skinTypesStr.split(",")) {
                String trimmed = name.trim();
                skinTypeIds.add(
                        skinTypeRepository.findByTypeNameIgnoreCase(trimmed)
                                .orElseThrow(() -> new RuntimeException(
                                        "Skin Type not found: " + trimmed))
                                .getSkintypeId());
            }
            attr.setSkintypeId(skinTypeIds);
        }

        // ── Certifications ────────────────────────────────────────────────
        String certCell = getCsvString(r, H_CERTIFICATIONS);
        if (!isBlank(certCell)) {
            List<ProductCertificateDocumentDto> docs = new ArrayList<>();
            for (String name : certCell.split(",")) {
                String trimmed = name.trim();
                certificationRepository.findByCertificationNameIgnoreCaseAndCategory_CategoryId(trimmed, categoryId)
                        .ifPresent(cert -> {
                            ProductCertificateDocumentDto d = new ProductCertificateDocumentDto();
                            d.setCertificationId(cert.getCertificationId());
                            d.setCertificateUrl("NOT_UPLOADED");
                            docs.add(d);
                        });
            }
            attr.setCertificateDocuments(docs);
        }

        return attr;
    }

    // =========================================================
    // ================= SHARED BUILDERS =======================
    // =========================================================

    private PackagingDetailsDto buildPackaging(
            Long unitPerPack, Long numberOfPacks,
            Long minOrderQty, Long maxOrderQty,
            String packTypeName, Long categoryId) {

        PackagingDetailsDto packaging = new PackagingDetailsDto();
        packaging.setUnitPerPack(unitPerPack);
        packaging.setNumberOfPacks(numberOfPacks);
        if (unitPerPack != null && numberOfPacks != null) {
            packaging.setPackSize(unitPerPack * numberOfPacks);
        }
        packaging.setMinimumOrderQuantity(minOrderQty);
        packaging.setMaximumOrderQuantity(maxOrderQty);

        if (!isBlank(packTypeName)) {
            packTypeRepository.findByPackTypeIgnoreCaseAndCategory_CategoryId(packTypeName, categoryId)
                    .ifPresent(pack -> packaging.setPackId(pack.getPackId()));
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
        pricing.setMrp(BigDecimal.valueOf(mrp));
        pricing.setSellingPrice(BigDecimal.valueOf(sellingPrice));
        pricing.setDiscountPercentage(BigDecimal.valueOf(discountPct));
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

    private void validateMandatoryExcel(Row row, Long categoryId, Long userId) {
        List<String> errors = new ArrayList<>();

        validateRequired(getString(row, COL_PRODUCT_NAME), "Product Name", errors);
        validateRequired(getString(row, COL_PRODUCT_CATEGORY), "Product Category", errors);
        validateRequired(getString(row, COL_PRODUCT_SUBCATEGORY), "Product Sub Category", errors);
        validateRequired(getString(row, COL_BRAND_NAME), "Brand Name", errors);
        validateRequired(getString(row, COL_NET_QUANTITY), "Net Quantity", errors);
        validateRequired(getString(row, COL_ACTIVE_INGREDIENTS), "Active Ingredients", errors);
        validateRequired(getString(row, COL_GENDER), "Gender", errors);
        validateRequired(getString(row, COL_AGE_GROUP), "Age Group", errors);
        validateRequired(getString(row, COL_PRODUCT_CLAIMS), "Product Claims", errors);
        validateRequired(getString(row, COL_WARNINGS), "Warnings / Precautions", errors);
        validateRequired(getString(row, COL_DESCRIPTION), "Product Description", errors);
        validateRequired(getString(row, COL_STORAGE_CONDITION), "Storage Condition", errors);
        validateRequired(getString(row, COL_MANUFACTURER), "Manufacturer Name", errors);
        validateRequired(getString(row, COL_COUNTRY), "Country of Origin", errors);
        validateRequired(getString(row, COL_CERTIFICATIONS), "Certifications / Compliance", errors);
        validateRequired(getString(row, COL_PACK_TYPE), "Pack Type", errors);
        validateRequired(getString(row, COL_INTENDED_USE_AREA), "Intended Use Area", errors);
        validateRequired(getString(row, COL_NET_QUANTITY_UNIT), "Net Quantity Unit", errors);
        validateRequired(getString(row, COL_PRODUCT_FORM), "Product Form", errors);

        // ── 1. Product Name ───────────────────────────────────────────────────
        String productName = getString(row, COL_PRODUCT_NAME);
        validateRequired(productName, "Product Name", errors);
        if (!isBlank(productName)) {
            if (!productName.matches("[A-Za-z0-9 \\p{Punct}]+"))
                errors.add("Product Name must contain only alphanumeric characters and special characters");
            if (productName.length() < 3)
                errors.add("Product Name must be at least 3 characters");
            if (productName.length() > 150)
                errors.add("Product Name must not exceed 150 characters");
        }

        // ── 2. Product Category (Product Type) ────────────────────────────────
        validateRequired(getString(row, COL_PRODUCT_CATEGORY), "Product Category", errors);

        // ── 3. Product Subcategory (Product Subtype) ──────────────────────────
        validateRequired(getString(row, COL_PRODUCT_SUBCATEGORY), "Product Sub Category", errors);

        // ── 4. Brand Name ─────────────────────────────────────────────────────
        String brandName = getString(row, COL_BRAND_NAME);
        validateRequired(brandName, "Brand Name", errors);
        if (!isBlank(brandName)) {
            if (!brandName.matches("[A-Za-z0-9 \\-]+"))
                errors.add("Brand Name must contain only alphabets, numbers, spaces, or hyphens");
            if (brandName.length() > 60)
                errors.add("Brand Name must not exceed 60 characters");
        }

        // ── 5. Variant Name (optional) ────────────────────────────────────────
        String variantName = getString(row, COL_VARIANT_NAME);
        if (!isBlank(variantName) && variantName.length() > 60)
            errors.add("Variant Name must not exceed 60 characters");

        // ── 6. Gender ─────────────────────────────────────────────────────────
        String gender = getString(row, COL_GENDER);
        validateRequired(gender, "Gender", errors);
        if (!isBlank(gender) && !List.of("Male", "Female", "Unisex").contains(gender))
            errors.add("Gender must be one of: Male, Female, Unisex");

        // ── 7. Intended Use Area ──────────────────────────────────────────────
        validateRequired(getString(row, COL_INTENDED_USE_AREA), "Intended Use Area", errors);

        // ── 8. Skin Type (optional — no mandatory check) ──────────────────────
        // No mandatory validation; value presence handled during attribute mapping.

        // ── 9. Hair Type (optional — no mandatory check) ──────────────────────
        // No mandatory validation; value presence handled during attribute mapping.

        // ── 10. Active Ingredients ────────────────────────────────────────────
        String activeIngredients = getString(row, COL_ACTIVE_INGREDIENTS);
        validateRequired(activeIngredients, "Active Ingredients", errors);
        if (!isBlank(activeIngredients) && activeIngredients.length() > 1000)
            errors.add("Active Ingredients must not exceed 1000 characters");

        // ── 11. Net Quantity / Strength ───────────────────────────────────────
        String netQuantity = getString(row, COL_NET_QUANTITY);
        validateRequired(netQuantity, "Net Quantity / Strength", errors);
        if (!netQuantity.matches("\\d+(\\.\\d+)?"))
            errors.add("Net Quantity / Strength must be a valid number (e.g., 55.66, 1245.256, 200)");

        // ── 11b. Net Quantity Unit ────────────────────────────────────────────
        validateRequired(getString(row, COL_NET_QUANTITY_UNIT), "Net Quantity Unit", errors);

        // ── 12. Age Group ─────────────────────────────────────────────────────
        validateRequired(getString(row, COL_AGE_GROUP), "Age Group", errors);

        // ── 13. Product Claims ────────────────────────────────────────────────
        String productClaims = getString(row, COL_PRODUCT_CLAIMS);
        validateRequired(productClaims, "Product Claims", errors);
        if (!isBlank(productClaims)) {
            if (!productClaims.matches("[A-Za-z0-9 ,./\\-]+"))
                errors.add("Product Claims must contain only alphabets, numbers, spaces, or hyphens");
            if (productClaims.length() > 1000)
                errors.add("Product Claims must not exceed 1000 characters");
        }

        // ── 14. Warnings / Precautions ────────────────────────────────────────
        String warnings = getString(row, COL_WARNINGS);
        validateRequired(warnings, "Warnings / Precautions", errors);
        if (!isBlank(warnings) && warnings.length() > 1000)
            errors.add("Warnings / Precautions must not exceed 1000 characters");

        // ── 15. Product Description ───────────────────────────────────────────
        String description = getString(row, COL_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (!isBlank(description)) {
            if (description.length() < 10)
                errors.add("Product Description must be at least 10 characters");
            if (description.length() > 1000)
                errors.add("Product Description must not exceed 1000 characters");
        }

        // ── 16. Storage Condition ─────────────────────────────────────────────
        validateRequired(getString(row, COL_STORAGE_CONDITION), "Storage Condition", errors);

        // ── 17. Manufacturer Name ─────────────────────────────────────────────
        String manufacturer = getString(row, COL_MANUFACTURER);
        validateRequired(manufacturer, "Manufacturer Name", errors);
        if (!isBlank(manufacturer) && manufacturer.length() > 100)
            errors.add("Manufacturer Name must not exceed 100 characters");

        // ── 18. Country of Origin ─────────────────────────────────────────────
        validateRequired(getString(row, COL_COUNTRY), "Country of Origin", errors);

        // ── 19. Certifications / Compliance ──────────────────────────────────
        validateRequired(getString(row, COL_CERTIFICATIONS), "Certifications / Compliance", errors);

        // ── 20 & 21. Certificate uploads & Product Images ─────────────────────
        // File uploads are not present in Excel rows; validated at the API/controller layer.

        // ── Pack Type ─────────────────────────────────────────────────────────
        validateRequired(getString(row, COL_PACK_TYPE), "Pack Type", errors);

        // ── Units per Pack ────────────────────────────────────────────────────
        Long unitPerPack = getLong(row, COL_UNIT_PER_PACK);
        validateRequired(unitPerPack, "Number of Units per Pack Type", errors);
        if (unitPerPack != null && unitPerPack <= 0)
            errors.add("Number of Units per Pack Type must be a positive value");

        // ── Number of Packs ───────────────────────────────────────────────────
        Long numberOfPacks = getLong(row, COL_NUMBER_OF_PACKS);
        validateRequired(numberOfPacks, "Number of Packs", errors);
        if (numberOfPacks != null && numberOfPacks <= 0)
            errors.add("Number of Packs must be a positive value");

        // ── Min / Max Order Qty ───────────────────────────────────────────────
        Long minOrderQty = getLong(row, COL_MIN_ORDER_QTY);
        Long maxOrderQty = getLong(row, COL_MAX_ORDER_QTY);
        validateRequired(minOrderQty, "Minimum Order Qty", errors);
        validateRequired(maxOrderQty, "Max Order Qty", errors);
        if (minOrderQty != null && minOrderQty <= 0)
            errors.add("Minimum Order Qty must be a positive value");
        if (maxOrderQty != null && maxOrderQty <= 0)
            errors.add("Max Order Qty must be a positive value");
        if (minOrderQty != null && maxOrderQty != null && minOrderQty > maxOrderQty)
            errors.add("Minimum Order Qty must be ≤ Maximum Order Qty");

        // ── Batch Number ──────────────────────────────────────────────────────
        String batchNumber = getString(row, COL_BATCH_NUMBER);
        validateRequired(batchNumber, "Batch Number", errors);
        if (!isBlank(batchNumber) && !batchNumber.matches("[A-Za-z0-9]+"))
            errors.add("Batch Number must be alphanumeric only");
        if (pricingDetailsService.isBatchNumberExistsForSeller(batchNumber, userId, categoryId))
            errors.add("Batch Number '" + batchNumber + "' already exists for this seller");

        // ── Manufacturing Date / Expiry Date ──────────────────────────────────
        LocalDate mfgDate = getDate(row, COL_MFG_DATE);
        LocalDate expiryDate = getDate(row, COL_EXPIRY_DATE);
        validateRequired(mfgDate, "Manufacturing Date", errors);
        validateRequired(expiryDate, "Expiry Date", errors);
        if (mfgDate != null && expiryDate != null && expiryDate.isBefore(mfgDate))
            errors.add("Expiry Date cannot be before Manufacturing Date");

        // ── Stock Quantity ────────────────────────────────────────────────────
        Long stockQty = getLong(row, COL_STOCK_QTY);
        validateRequired(stockQty, "Stock Quantity", errors);
        if (stockQty != null && stockQty <= 0)
            errors.add("Stock Quantity must be a positive value");

        // ── MRP / Selling Price ───────────────────────────────────────────────
        Long mrp = getLong(row, COL_MRP);
        Long sellingPrice = getLong(row, COL_SELLING_PRICE);
        validateRequired(mrp, "MRP", errors);
        validateRequired(sellingPrice, "Selling Price", errors);
        if (mrp != null && mrp <= 0)
            errors.add("MRP must be greater than 0");
        if (sellingPrice != null && sellingPrice <= 0)
            errors.add("Selling Price must be greater than 0");
        if (mrp != null && sellingPrice != null && sellingPrice > mrp)
            errors.add("Selling Price cannot be greater than MRP");

        // ── GST % ─────────────────────────────────────────────────────────────
        Long gstPct = getLong(row, COL_GST_PCT);
        validateRequired(gstPct, "GST %", errors);
        if (gstPct != null && !VALID_GST_VALUES.contains(gstPct))
            errors.add("GST % must be one of: 0, 5, 12, 18");

        // ── HSN Code ──────────────────────────────────────────────────────────
        Long hsnCode = getLong(row, COL_HSN_CODE);
        validateRequired(hsnCode, "HSN Code", errors);

        if (!errors.isEmpty()) throw new ValidationException(errors);
    }

    // =========================================================
    // ================= CSV VALIDATION ========================
    // =========================================================

    private void validateMandatoryCsv(CSVRecord r, Long categoryId, Long userId) {
        List<String> errors = new ArrayList<>();

        validateRequired(getCsvString(r, H_PRODUCT_NAME), "Product Name", errors);
        validateRequired(getCsvString(r, H_PRODUCT_CATEGORY), "Product Category", errors);
        validateRequired(getCsvString(r, H_PRODUCT_SUBCATEGORY), "Product Sub Category", errors);
        validateRequired(getCsvString(r, H_BRAND_NAME), "Brand Name", errors);
        validateRequired(getCsvString(r, H_NET_QUANTITY), "Net Quantity", errors);
        validateRequired(getCsvString(r, H_ACTIVE_INGREDIENTS), "Active Ingredients", errors);
        validateRequired(getCsvString(r, H_GENDER), "Gender", errors);
        validateRequired(getCsvString(r, H_AGE_GROUP), "Age Group", errors);
        validateRequired(getCsvString(r, H_PRODUCT_CLAIMS), "Product Claims", errors);
        validateRequired(getCsvString(r, H_WARNINGS), "Warnings / Precautions", errors);
        validateRequired(getCsvString(r, H_DESCRIPTION), "Product Description", errors);
        validateRequired(getCsvString(r, H_STORAGE_CONDITION), "Storage Condition", errors);
        validateRequired(getCsvString(r, H_MANUFACTURER), "Manufacturer Name", errors);
        validateRequired(getCsvString(r, H_COUNTRY), "Country of Origin", errors);
        validateRequired(getCsvString(r, H_CERTIFICATIONS), "Certifications / Compliance", errors);
        validateRequired(getCsvString(r, H_PACK_TYPE), "Pack Type", errors);
        validateRequired(getCsvString(r, H_INTENDED_USE_AREA), "Intended Use Area", errors);
        validateRequired(getCsvString(r, H_NET_QUANTITY_UNIT), "Net Quantity Unit", errors);
        validateRequired(getCsvString(r, H_PRODUCT_FORM), "Product Form", errors);

        // ── 1. Product Name ───────────────────────────────────────────────────
        String productName = getCsvString(r, H_PRODUCT_NAME);
        validateRequired(productName, "Product Name", errors);
        if (!isBlank(productName)) {
            if (!productName.matches("[A-Za-z0-9 \\p{Punct}]+"))
                errors.add("Product Name must contain only alphanumeric characters and special characters");
            if (productName.length() < 3)
                errors.add("Product Name must be at least 3 characters");
            if (productName.length() > 150)
                errors.add("Product Name must not exceed 150 characters");
        }

        // ── 2. Product Category (Product Type) ───────────────────────────────
        validateRequired(getCsvString(r, H_PRODUCT_CATEGORY), "Product Category", errors);

        // ── 3. Product Subcategory (Product Subtype) ──────────────────────────
        validateRequired(getCsvString(r, H_PRODUCT_SUBCATEGORY), "Product Sub Category", errors);

        // ── 4. Brand Name ─────────────────────────────────────────────────────
        String brandName = getCsvString(r, H_BRAND_NAME);
        validateRequired(brandName, "Brand Name", errors);
        if (!isBlank(brandName)) {
            if (!brandName.matches("[A-Za-z0-9 \\-]+"))
                errors.add("Brand Name must contain only alphabets, numbers, spaces, or hyphens");
            if (brandName.length() > 60)
                errors.add("Brand Name must not exceed 60 characters");
        }

        // ── 5. Variant Name (optional) ────────────────────────────────────────
        String variantName = getCsvString(r, H_VARIANT_NAME);
        if (!isBlank(variantName) && variantName.length() > 60)
            errors.add("Variant Name must not exceed 60 characters");

        // ── 6. Gender ─────────────────────────────────────────────────────────
        String gender = getCsvString(r, H_GENDER);
        validateRequired(gender, "Gender", errors);
        if (!isBlank(gender) && !List.of("Male", "Female", "Unisex").contains(gender))
            errors.add("Gender must be one of: Male, Female, Unisex");

        // ── 7. Intended Use Area ──────────────────────────────────────────────
        validateRequired(getCsvString(r, H_INTENDED_USE_AREA), "Intended Use Area", errors);

        // ── 8. Skin Type (optional) ───────────────────────────────────────────
        // No mandatory validation; value presence handled during attribute mapping.

        // ── 9. Hair Type (optional) ───────────────────────────────────────────
        // No mandatory validation; value presence handled during attribute mapping.

        // ── 10. Active Ingredients ────────────────────────────────────────────
        String activeIngredients = getCsvString(r, H_ACTIVE_INGREDIENTS);
        validateRequired(activeIngredients, "Active Ingredients", errors);
        if (!isBlank(activeIngredients) && activeIngredients.length() > 1000)
            errors.add("Active Ingredients must not exceed 1000 characters");

        // ── 11. Net Quantity / Strength ───────────────────────────────────────
        String netQuantity = getCsvString(r, H_NET_QUANTITY);
        validateRequired(netQuantity, "Net Quantity / Strength", errors);
        if (!netQuantity.matches("\\d+(\\.\\d+)?"))
            errors.add("Net Quantity / Strength must be a valid number (e.g., 55.66, 1245.256, 200)");

        validateRequired(getCsvString(r, H_NET_QUANTITY_UNIT), "Net Quantity Unit", errors);

        // ── 12. Age Group ─────────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_AGE_GROUP), "Age Group", errors);

        // ── 13. Product Claims ────────────────────────────────────────────────
        String productClaims = getCsvString(r, H_PRODUCT_CLAIMS);
        validateRequired(productClaims, "Product Claims", errors);
        if (!isBlank(productClaims)) {
            if (!productClaims.matches("[A-Za-z0-9 ,./\\-]+"))
                errors.add("Product Claims must contain only alphabets, numbers, spaces, or hyphens");
            if (productClaims.length() > 1000)
                errors.add("Product Claims must not exceed 1000 characters");
        }

        // ── 14. Warnings / Precautions ────────────────────────────────────────
        String warnings = getCsvString(r, H_WARNINGS);
        validateRequired(warnings, "Warnings / Precautions", errors);
        if (!isBlank(warnings) && warnings.length() > 1000)
            errors.add("Warnings / Precautions must not exceed 1000 characters");

        // ── 15. Product Description ───────────────────────────────────────────
        String description = getCsvString(r, H_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (!isBlank(description)) {
            if (description.length() < 10)
                errors.add("Product Description must be at least 10 characters");
            if (description.length() > 1000)
                errors.add("Product Description must not exceed 1000 characters");
        }

        // ── 16. Storage Condition ─────────────────────────────────────────────
        validateRequired(getCsvString(r, H_STORAGE_CONDITION), "Storage Condition", errors);

        // ── 17. Manufacturer Name ─────────────────────────────────────────────
        String manufacturer = getCsvString(r, H_MANUFACTURER);
        validateRequired(manufacturer, "Manufacturer Name", errors);
        if (!isBlank(manufacturer) && manufacturer.length() > 100)
            errors.add("Manufacturer Name must not exceed 100 characters");

        // ── 18. Country of Origin ─────────────────────────────────────────────
        validateRequired(getCsvString(r, H_COUNTRY), "Country of Origin", errors);

        // ── 19. Certifications / Compliance ──────────────────────────────────
        validateRequired(getCsvString(r, H_CERTIFICATIONS), "Certifications / Compliance", errors);

        // ── 20 & 21. Certificate uploads & Product Images ─────────────────────
        // File uploads are not present in CSV rows; validated at the API/controller layer.

        // ── Pack Type ─────────────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_PACK_TYPE), "Pack Type", errors);

        // ── Units per Pack ────────────────────────────────────────────────────
        Long unitPerPack = getCsvLong(r, H_UNIT_PER_PACK);
        validateRequired(unitPerPack, "Number of Units per Pack Type", errors);
        if (unitPerPack != null && unitPerPack <= 0)
            errors.add("Number of Units per Pack Type must be a positive value");

        // ── Number of Packs ───────────────────────────────────────────────────
        Long numberOfPacks = getCsvLong(r, H_NUMBER_OF_PACKS);
        validateRequired(numberOfPacks, "Number of Packs", errors);
        if (numberOfPacks != null && numberOfPacks <= 0)
            errors.add("Number of Packs must be a positive value");

        // ── Min / Max Order Qty ───────────────────────────────────────────────
        Long minOrderQty = getCsvLong(r, H_MIN_ORDER_QTY);
        Long maxOrderQty = getCsvLong(r, H_MAX_ORDER_QTY);
        validateRequired(minOrderQty, "Minimum Order Qty", errors);
        validateRequired(maxOrderQty, "Max Order Qty", errors);
        if (minOrderQty != null && minOrderQty <= 0)
            errors.add("Minimum Order Qty must be a positive value");
        if (maxOrderQty != null && maxOrderQty <= 0)
            errors.add("Max Order Qty must be a positive value");
        if (minOrderQty != null && maxOrderQty != null && minOrderQty > maxOrderQty)
            errors.add("Minimum Order Qty must be ≤ Maximum Order Qty");

        // ── Batch Number ──────────────────────────────────────────────────────
        String batchNumber = getCsvString(r, H_BATCH_NUMBER);
        validateRequired(batchNumber, "Batch Number", errors);
        if (!isBlank(batchNumber) && !batchNumber.matches("[A-Za-z0-9]+"))
            errors.add("Batch Number must be alphanumeric only");
        if (pricingDetailsService.isBatchNumberExistsForSeller(batchNumber, userId, categoryId))
            errors.add("Batch Number '" + batchNumber + "' already exists for this seller");

        // ── Manufacturing Date / Expiry Date ──────────────────────────────────
        LocalDate mfgDate = parseCsvDate(getCsvString(r, H_MFG_DATE));
        LocalDate expiryDate = parseCsvDate(getCsvString(r, H_EXPIRY_DATE));
        validateRequired(mfgDate, "Manufacturing Date", errors);
        validateRequired(expiryDate, "Expiry Date", errors);
        if (mfgDate != null && expiryDate != null && expiryDate.isBefore(mfgDate))
            errors.add("Expiry Date cannot be before Manufacturing Date");

        // ── Stock Quantity ────────────────────────────────────────────────────
        Long stockQty = getCsvLong(r, H_STOCK_QTY);
        validateRequired(stockQty, "Stock Quantity", errors);
        if (stockQty != null && stockQty <= 0)
            errors.add("Stock Quantity must be a positive value");

        // ── MRP / Selling Price ───────────────────────────────────────────────
        Long mrp = getCsvLong(r, H_MRP);
        Long sellingPrice = getCsvLong(r, H_SELLING_PRICE);
        validateRequired(mrp, "MRP", errors);
        validateRequired(sellingPrice, "Selling Price", errors);
        if (mrp != null && mrp <= 0)
            errors.add("MRP must be greater than 0");
        if (sellingPrice != null && sellingPrice <= 0)
            errors.add("Selling Price must be greater than 0");
        if (mrp != null && sellingPrice != null && sellingPrice > mrp)
            errors.add("Selling Price cannot be greater than MRP");

        // ── GST % ─────────────────────────────────────────────────────────────
        Long gstPct = getCsvLong(r, H_GST_PCT);
        validateRequired(gstPct, "GST %", errors);
        if (gstPct != null && !VALID_GST_VALUES.contains(gstPct))
            errors.add("GST % must be one of: 0, 5, 12, 18");

        // ── HSN Code ──────────────────────────────────────────────────────────
        Long hsnCode = getCsvLong(r, H_HSN_CODE);
        validateRequired(hsnCode, "HSN Code", errors);

        if (!errors.isEmpty()) throw new ValidationException(errors);
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
        Cell cell = row.getCell(col);
        if (cell == null) return null;

        // Case 1: proper numeric date cell
        try {
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                    || cell.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (Exception ignored) {
        }

        // Case 2: date stored as string e.g. "01-Aug-24", "Sep-25", "2024-08-01"
        try {
            String raw = cell.toString().trim();
            if (!raw.isEmpty()) {
                return parseCsvDate(raw);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private LocalTime getTime(Row row, int col) {
        try {
            Cell cell = row.getCell(col);
            if (cell == null) return null;
            return cell.getLocalDateTimeCellValue().toLocalTime();
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

        // yyyy-MM-dd  e.g. 2024-08-01
        try {
            return LocalDate.parse(raw);
        } catch (Exception ignored) {
        }

        // dd-MMM-yy  e.g. 01-Aug-24  ← ADD THIS
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH));
        } catch (Exception ignored) {
        }

        // dd-MMM-yyyy  e.g. 01-Aug-2024  ← ADD THIS TOO (future-proof)
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));
        } catch (Exception ignored) {
        }

        // MMM-yy  e.g. Aug-24
        try {
            return YearMonth.parse(raw, DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH)).atDay(1);
        } catch (Exception ignored) {
        }

        // dd-MM-yyyy  e.g. 01-08-2024
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception ignored) {
        }

        // M/d/yyyy  e.g. 8/1/2024
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