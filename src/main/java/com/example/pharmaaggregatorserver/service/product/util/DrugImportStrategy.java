package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
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
@Component("DRUGS")
@RequiredArgsConstructor
public class DrugImportStrategy implements ProductImportStrategy {

    private final MoleculeRepository moleculeRepository;
    private final PackTypeRepository packTypeRepository;
    private final TherapeuticCategoryRepository therapeuticCategoryRepository;
    private final TherapeuticSubcategoryRepository therapeuticSubcategoryRepository;
    private final StorageConditionMasterRepository storageConditionRepository;
    private final PricingDetailsService pricingDetailsService;

    // ── Valid GST percentages ─────────────────────────────────────────────
    private static final Set<Long> VALID_GST_VALUES = Set.of(0L, 5L, 12L, 18L);

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
    private static final int COL_DATE_OF_ENTRY = 20; // ignored — always LocalDate.now()
    private static final int COL_MRP = 21;
    private static final int COL_SELLING_PRICE = 22;
    private static final int COL_DISCOUNT_PCT = 23;
    private static final int COL_GST_PCT = 24;
    private static final int COL_HSN_CODE = 25;
    private static final int COL_SHELF_LIFE_MONTHS = 26; // ignored — always computed
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
    private static final String H_DATE_OF_ENTRY = "Date of Entry*"; // ignored — always LocalDate.now()
    private static final String H_MRP = "MRP (INR)*";
    private static final String H_SELLING_PRICE = "Selling Price(INR)*";
    private static final String H_DISCOUNT_PCT = "Discount %";
    private static final String H_GST_PCT = "GST %";
    private static final String H_HSN_CODE = "HSN Code*";
    private static final String H_SHELF_LIFE_MONTHS = "Shelf Life Months"; // ignored — always computed
    private static final String H_MARKETING_URL = "Product Marketing URL";

    // Additional discount cols accessed by index
    private static final int[] CSV_SLAB_MIN_QTY_COLS = {28, 35, 42, 49};
    private static final int[] CSV_SLAB_DISCOUNT_COLS = {29, 36, 43, 50};
    private static final int[] CSV_SLAB_START_DATE_COLS = {30, 37, 44, 51};
    private static final int[] CSV_SLAB_START_TIME_COLS = {31, 38, 45, 52};
    private static final int[] CSV_SLAB_END_DATE_COLS = {32, 39, 46, 53};
    private static final int[] CSV_SLAB_END_TIME_COLS = {33, 40, 47, 54};

    // ── Excel entry point ─────────────────────────────────────────────────
    @Override
    public ProductDetailsDto mapRow(Row row, Long categoryId, Long userId) {
        log.info("Drugs Excel import Called");

        validateMandatoryExcel(row, categoryId, userId);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getString(row, COL_PRODUCT_NAME));
        dto.setWarningsPrecautions(getString(row, COL_WARNINGS));
        dto.setProductDescription(getString(row, COL_DESCRIPTION));
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));

        Long unitPerPack = getNullSafeLong(row, COL_UNIT_PER_PACK);
        Long numberOfPacks = getNullSafeLong(row, COL_NUMBER_OF_PACKS);
        String packTypeName = getString(row, COL_PACK_TYPE);
        String dosageFormName = getString(row, COL_DOSAGE_FORM);

        dto.setPackagingDetails(Set.of(buildPackaging(
                unitPerPack, numberOfPacks,
                getNullSafeLong(row, COL_MIN_ORDER_QTY),
                getNullSafeLong(row, COL_MAX_ORDER_QTY),
                packTypeName, categoryId, dosageFormName)));

        LocalDate mfgDate = getLocalDate(row, COL_MFG_DATE);
        LocalDate expiryDate = getLocalDate(row, COL_EXPIRY_DATE);
        // Shelf life always computed — file column ignored
        Long shelfLifeMonths = computeShelfLifeMonths(mfgDate, expiryDate);

        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();
        Long mainDiscountPct = getNullSafeLong(row, COL_DISCOUNT_PCT);
        Long mainMinOrderQty = getNullSafeLong(row, COL_MIN_ORDER_QTY);
        Long mainMaxOrderQty = getNullSafeLong(row, COL_MAX_ORDER_QTY);
        Set<Long> seenSlabMpqs = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            int base = COL_ADD_DISCOUNT_START + (slab * ADD_DISCOUNT_SLAB_SIZE);
            Long minQty = getNullSafeLong(row, base + 1);
            Long discountPct = getNullSafeLong(row, base + 2);

            if ((minQty == null || minQty == 0) && (discountPct == null || discountPct == 0)) continue;

            validateAdditionalDiscountSlab(
                    slab + 1, minQty, discountPct,
                    mainMinOrderQty, mainMaxOrderQty, mainDiscountPct,
                    getLocalDate(row, base + 3), getLocalDate(row, base + 5),
                    seenSlabMpqs);

            AdditionalDiscountDto ad = new AdditionalDiscountDto();
            ad.setMinimumPurchaseQuantity(minQty);
            ad.setAdditionalDiscountPercentage(discountPct == null ? null : java.math.BigDecimal.valueOf(discountPct));
            ad.setEffectiveStartDate(getLocalDate(row, base + 3));
            ad.setEffectiveStartTime(getLocalTime(row, base + 4));
            ad.setEffectiveEndDate(getLocalDate(row, base + 5));
            ad.setEffectiveEndTime(getLocalTime(row, base + 6));
            additionalDiscounts.add(ad);
        }

        dto.setPricingDetails(Set.of(buildPricing(
                getString(row, COL_BATCH_NUMBER),
                toStartOfDay(mfgDate),
                toEndOfDay(expiryDate),
                getString(row, COL_STORAGE_CONDITION),
                getNullSafeLong(row, COL_STOCK_QTY),
                LocalDate.now(),                         // Date of Stock Entry — always today
                getNullSafeLong(row, COL_MRP),
                getNullSafeLong(row, COL_SELLING_PRICE),
                mainDiscountPct,
                getNullSafeLong(row, COL_GST_PCT),
                getNullSafeLong(row, COL_HSN_CODE),
                shelfLifeMonths,                         // computed, not from file
                additionalDiscounts)));

        dto.setProductAttributeDrugs(Set.of(buildDrugAttr(
                dosageFormName,
                getString(row, COL_THERAPEUTIC_CAT),
                getString(row, COL_THERAPEUTIC_SUBCAT),
                getString(row, COL_MOLECULES),
                getString(row, COL_STRENGTH),
                getString(row, COL_STORAGE_CONDITION),
                categoryId)));

        return dto;
    }

    // ── CSV entry point ───────────────────────────────────────────────────
    @Override
    public ProductDetailsDto mapCsv(CSVRecord r, Long categoryId, Long userId) {
        log.info("Drugs CSV import Called");

        validateMandatoryCsv(r, categoryId, userId);

        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setProductName(getCsvString(r, H_PRODUCT_NAME));
        dto.setWarningsPrecautions(getCsvString(r, H_WARNINGS));
        dto.setProductDescription(getCsvString(r, H_DESCRIPTION));
        dto.setManufacturerName(getCsvString(r, H_MANUFACTURER));

        Long unitPerPack = getCsvLong(r, H_UNIT_PER_PACK);
        Long numberOfPacks = getCsvLong(r, H_NUMBER_OF_PACKS);
        String packTypeName = getCsvString(r, H_PACK_TYPE);
        String dosageFormName = getCsvString(r, H_DOSAGE_FORM);

        dto.setPackagingDetails(Set.of(buildPackaging(
                unitPerPack, numberOfPacks,
                getCsvLong(r, H_MIN_ORDER_QTY),
                getCsvLong(r, H_MAX_ORDER_QTY),
                packTypeName, categoryId, dosageFormName)));

        LocalDate mfgDate = parseCsvDate(getCsvString(r, H_MFG_DATE));
        LocalDate expiryDate = parseCsvDate(getCsvString(r, H_EXPIRY_DATE));
        // Shelf life always computed — file column ignored
        Long shelfLifeMonths = computeShelfLifeMonths(mfgDate, expiryDate);

        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();
        Long mainDiscountPct = getCsvLong(r, H_DISCOUNT_PCT);
        Long mainMinOrderQty = getCsvLong(r, H_MIN_ORDER_QTY);
        Long mainMaxOrderQty = getCsvLong(r, H_MAX_ORDER_QTY);
        Set<Long> seenSlabMpqs = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            Long minQty = getCsvLongByIndex(r, CSV_SLAB_MIN_QTY_COLS[slab]);
            Long discountPct = getCsvLongByIndex(r, CSV_SLAB_DISCOUNT_COLS[slab]);

            if ((minQty == null || minQty == 0) && (discountPct == null || discountPct == 0)) continue;

            LocalDate slabStartDate = parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_START_DATE_COLS[slab]));
            LocalDate slabEndDate = parseCsvDate(getCsvStringByIndex(r, CSV_SLAB_END_DATE_COLS[slab]));

            validateAdditionalDiscountSlab(
                    slab + 1, minQty, discountPct,
                    mainMinOrderQty, mainMaxOrderQty, mainDiscountPct,
                    slabStartDate, slabEndDate,
                    seenSlabMpqs);

            AdditionalDiscountDto ad = new AdditionalDiscountDto();
            ad.setMinimumPurchaseQuantity(minQty);
            ad.setAdditionalDiscountPercentage(discountPct == null ? null : java.math.BigDecimal.valueOf(discountPct));
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
                getCsvString(r, H_STORAGE_CONDITION),
                getCsvLong(r, H_STOCK_QTY),
                LocalDate.now(),                         // Date of Stock Entry — always today
                getCsvLong(r, H_MRP),
                getCsvLong(r, H_SELLING_PRICE),
                mainDiscountPct,
                getCsvLong(r, H_GST_PCT),
                getCsvLong(r, H_HSN_CODE),
                shelfLifeMonths,                         // computed, not from file
                additionalDiscounts)));

        dto.setProductAttributeDrugs(Set.of(buildDrugAttr(
                dosageFormName,
                getCsvString(r, H_THERAPEUTIC_CAT),
                getCsvString(r, H_THERAPEUTIC_SUBCAT),
                getCsvString(r, H_MOLECULES),
                getCsvStringByIndex(r, COL_STRENGTH),
                getCsvString(r, H_STORAGE_CONDITION),
                categoryId)));

        return dto;
    }

    // ── Shared builders ───────────────────────────────────────────────────
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

        if (packTypeName != null && !packTypeName.isBlank()
                && dosageFormName != null && !dosageFormName.isBlank()) {
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
        pricing.setMrp(BigDecimal.valueOf(mrp));
        pricing.setSellingPrice(BigDecimal.valueOf(sellingPrice));
        pricing.setDiscountPercentage(BigDecimal.valueOf(discountPct));
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
            String moleculeCell, String strengthCell,
            String storage,
            Long categoryId) {

        ProductAttributeDrugDto attr = new ProductAttributeDrugDto();
        attr.setDosageForm(dosageForm);

        if (therapeuticCat != null && !therapeuticCat.isBlank()) {
            attr.setTherapeuticCategoryId(
                    therapeuticCategoryRepository
                            .findByTherapeuticCategoryIgnoreCaseAndCategory_CategoryId(therapeuticCat, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Therapeutic category not found: " + therapeuticCat))
                            .getTherapeuticCategoryId());
        }

        if (therapeuticSubCat != null && !therapeuticSubCat.isBlank()) {
            attr.setTherapeuticSubcategoryId(
                    therapeuticSubcategoryRepository
                            .findByTherapeuticSubcategoryIgnoreCaseAndTherapeuticCategoryMaster_TherapeuticCategoryId(therapeuticSubCat, attr.getTherapeuticCategoryId())
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
                        .findByMoleculeNameIgnoreCase(name)
                        .orElseThrow(() -> new RuntimeException("Molecule not found: " + name));

                ProductMoleculeDto pm = new ProductMoleculeDto();
                pm.setMoleculeId(m.getMoleculeId());
                pm.setStrength(strength);
                molecules.add(pm);
            }
            attr.setMolecules(molecules);
        }

        if (storage != null && !storage.isBlank()) {

            List<Long> storageConditionIds = Arrays.stream(storage.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(condition -> storageConditionRepository
                            .findByConditionNameIgnoreCaseAndCategory_CategoryId(condition, categoryId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Storage condition not found: " + condition))
                            .getStorageConditionId())
                    .distinct()
                    .toList();

            attr.setStorageConditionIds(storageConditionIds);
        }

        return attr;
    }

    // ── Shelf life computation ────────────────────────────────────────────

    /**
     * Computes shelf life in whole months between mfg and expiry.
     * Returns null if either date is absent.
     */
    private Long computeShelfLifeMonths(LocalDate mfgDate, LocalDate expiryDate) {
        if (mfgDate == null || expiryDate == null) return null;
        return ChronoUnit.MONTHS.between(mfgDate, expiryDate);
    }

    // ── Additional-discount slab validation ───────────────────────────────

    /**
     * Validates a single additional discount slab.
     * Throws ValidationException immediately on the first bad slab so the
     * error message clearly identifies which slab (1-based) is at fault.
     *
     * @param slabNumber      1-based slab index, for error messages
     * @param mpq             Minimum Purchase Quantity for this slab
     * @param discountPct     Discount % for this slab
     * @param mainMoq         The product's main Minimum Order Quantity
     * @param mainMaxQty      The product's main Maximum Order Quantity
     * @param mainDiscountPct The product's base Discount %
     * @param startDate       Effective Start Date for this slab
     * @param endDate         Effective End Date for this slab
     * @param seenMpqs        Set of MPQ values already seen (duplicate detection)
     */
    private void validateAdditionalDiscountSlab(
            int slabNumber,
            Long mpq, Long discountPct,
            Long mainMoq, Long mainMaxQty, Long mainDiscountPct,
            LocalDate startDate, LocalDate endDate,
            Set<Long> seenMpqs) {

        List<String> errors = new ArrayList<>();
        String prefix = "Additional Discount Slab " + slabNumber + ": ";

        // MPQ validations
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

        // Discount % validations
        if (discountPct == null) {
            // Discount is mandatory when MPQ is provided
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

        // Effective date validations
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

    // ── Excel validation ──────────────────────────────────────────────────
    private void validateMandatoryExcel(Row row, Long categoryId, Long userId) {
        List<String> errors = new ArrayList<>();

        // ── Product Name ──────────────────────────────────────────────────
        String productName = getString(row, COL_PRODUCT_NAME);
        validateRequired(productName, "Product Name", errors);
        if (productName != null && !productName.isBlank()) {
            if (productName.length() < 3) {
                errors.add("Product Name must be at least 3 characters");
            }
            if (productName.length() > 150) {
                errors.add("Product Name must not exceed 150 characters");
            }
        }

        // ── Product Description ───────────────────────────────────────────
        String description = getString(row, COL_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (description != null && description.length() > 1000) {
            errors.add("Product Description must not exceed 1000 characters");
        }

        // ── Warnings / Precautions ────────────────────────────────────────
        String warnings = getString(row, COL_WARNINGS);
        validateRequired(warnings, "Warnings / Precautions", errors);
        if (warnings != null && warnings.length() > 1000) {
            errors.add("Warnings / Precautions must not exceed 1000 characters");
        }

        // ── Other mandatory text fields ───────────────────────────────────
        validateRequired(getString(row, COL_THERAPEUTIC_CAT), "Therapeutic Category", errors);
        validateRequired(getString(row, COL_THERAPEUTIC_SUBCAT), "Therapeutic Sub Category", errors);
        validateRequired(getString(row, COL_MOLECULES), "Molecule", errors);
        validateRequired(getString(row, COL_DOSAGE_FORM), "Dosage Form", errors);
        validateRequired(getString(row, COL_STORAGE_CONDITION), "Storage Condition", errors);

        // ── Pack Type ─────────────────────────────────────────────────────
        validateRequired(getString(row, COL_PACK_TYPE), "Pack Type", errors);

        // ── Unit Per Pack ─────────────────────────────────────────────────────
        String unitPerPackRaw = getString(row, COL_UNIT_PER_PACK);
        Long unitPerPack = getNullSafeLong(row, COL_UNIT_PER_PACK);
        validateRequired(unitPerPackRaw, "Number of Units per Pack Type", errors);
        if (unitPerPackRaw != null && !unitPerPackRaw.isBlank()) {
            if (unitPerPack == null) {
                errors.add("Number of Units per Pack Type must be numeric");
            } else if (unitPerPack <= 0) {
                errors.add("Number of Units per Pack Type must be a positive value");
            }
        }

        // ── Number of Packs ───────────────────────────────────────────────
        Long numberOfPacks = getNullSafeLong(row, COL_NUMBER_OF_PACKS);
        validateRequired(numberOfPacks, "Number of Packs", errors);
        if (numberOfPacks != null && numberOfPacks <= 0) {
            errors.add("Number of Packs must be a positive value");
        }

        // ── Min / Max Order Quantities ────────────────────────────────────
        Long minOrderQty = getNullSafeLong(row, COL_MIN_ORDER_QTY);
        Long maxOrderQty = getNullSafeLong(row, COL_MAX_ORDER_QTY);

        validateRequired(minOrderQty, "Minimum Order Qty", errors);
        if (minOrderQty != null && minOrderQty <= 0) {
            errors.add("Minimum Order Qty must be a positive value");
        }

        validateRequired(maxOrderQty, "Max Order Qty", errors);
        if (maxOrderQty != null && maxOrderQty <= 0) {
            errors.add("Max Order Qty must be a positive value");
        }

        if (minOrderQty != null && maxOrderQty != null
                && minOrderQty > 0 && maxOrderQty > 0
                && minOrderQty > maxOrderQty) {
            errors.add("Minimum Order Qty must be ≤ Maximum Order Qty");
        }

        // ── Batch Number ──────────────────────────────────────────────────
        String batchNumber = getString(row, COL_BATCH_NUMBER);
        validateRequired(batchNumber, "Batch Number", errors);
        if (batchNumber != null && !batchNumber.isBlank()) {
            if (!batchNumber.matches("[A-Za-z0-9]+")) {
                errors.add("Batch Number must be alphanumeric only (no special characters)");
            }
            if (batchNumber.length() < 3) {
                errors.add("Batch Number must be at least 3 characters");
            }
            if (batchNumber.length() > 20) {
                errors.add("Batch Number must not exceed 20 characters");
            }
            if (pricingDetailsService.isBatchNumberExistsForSeller(batchNumber, userId, categoryId)) {
                errors.add("Batch Number '" + batchNumber + "' already exists for this seller");
            }
        }

        // ── Manufacturing Date ────────────────────────────────────────────
        LocalDate mfgDate = getLocalDate(row, COL_MFG_DATE);
        validateRequired(mfgDate, "Manufacturing Date", errors);
        if (mfgDate != null && YearMonth.from(mfgDate).isAfter(YearMonth.now())) {
            errors.add("Manufacturing Date cannot be a future month");
        }

        // ── Expiry Date ───────────────────────────────────────────────────
        LocalDate expiryDate = getLocalDate(row, COL_EXPIRY_DATE);
        validateRequired(expiryDate, "Expiry Date", errors);

        if (mfgDate != null && expiryDate != null && expiryDate.isBefore(mfgDate)) {
            errors.add("Expiry Date cannot be before Manufacturing Date");
        }

        // ── Stock Quantity ────────────────────────────────────────────────
        Long stockQty = getNullSafeLong(row, COL_STOCK_QTY);
        validateRequired(stockQty, "Stock Quantity", errors);
        if (stockQty != null && stockQty <= 0) {
            errors.add("Stock Quantity must be a positive value");
        }
        if (stockQty != null && minOrderQty != null && stockQty < minOrderQty) {
            errors.add("Stock Quantity (" + stockQty + ") must be ≥ Minimum Order Quantity (" + minOrderQty + ")");
        }

        // ── MRP ───────────────────────────────────────────────────────────
        Long mrp = getNullSafeLong(row, COL_MRP);
        validateRequired(mrp, "MRP", errors);
        if (mrp != null && mrp <= 0) {
            errors.add("MRP must be greater than 0");
        }

        // ── Selling Price ─────────────────────────────────────────────────
        Long sellingPrice = getNullSafeLong(row, COL_SELLING_PRICE);
        validateRequired(sellingPrice, "Selling Price", errors);
        if (sellingPrice != null && sellingPrice <= 0) {
            errors.add("Selling Price must be greater than 0");
        }
        if (sellingPrice != null && mrp != null && sellingPrice > mrp) {
            errors.add("Selling Price cannot be greater than MRP");
        }

        // ── Discount % ────────────────────────────────────────────────────
        Long discountPct = getNullSafeLong(row, COL_DISCOUNT_PCT);
        if (discountPct != null && (discountPct < 0 || discountPct > 100)) {
            errors.add("Discount % must be in the range 0–100");
        }

        // ── GST % ─────────────────────────────────────────────────────────
        Long gstPct = getNullSafeLong(row, COL_GST_PCT);
        validateRequired(gstPct, "GST %", errors);
        if (gstPct != null && !VALID_GST_VALUES.contains(gstPct)) {
            errors.add("GST % must be one of: 0, 5, 12, 18");
        }

        // ── HSN Code ──────────────────────────────────────────────────────
        Long hsnCode = getNullSafeLong(row, COL_HSN_CODE);
        validateRequired(hsnCode, "HSN Code", errors);
        if (hsnCode != null) {
            int digits = String.valueOf(hsnCode).length();
            if (digits != 4 && digits != 6 && digits != 8) {
                errors.add("HSN Code must be 4, 6, or 8 digits");
            }
        }

        // ── Date of Entry — ignored, always today; no validation needed ───

        // ── Molecule + Strength ───────────────────────────────────────────
        String moleculeCell = getString(row, COL_MOLECULES);
        String strengthCell = getString(row, COL_STRENGTH);
        validateMoleculeStrength(moleculeCell, strengthCell, errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    // ── CSV validation ────────────────────────────────────────────────────
    private void validateMandatoryCsv(CSVRecord r, Long categoryId, Long userId) {
        List<String> errors = new ArrayList<>();

        // ── Product Name ──────────────────────────────────────────────────
        String productName = getCsvString(r, H_PRODUCT_NAME);
        validateRequired(productName, "Product Name", errors);
        if (productName != null && !productName.isBlank()) {
            if (productName.length() < 3) {
                errors.add("Product Name must be at least 3 characters");
            }
            if (productName.length() > 150) {
                errors.add("Product Name must not exceed 150 characters");
            }
        }

        // ── Product Description ───────────────────────────────────────────
        String description = getCsvString(r, H_DESCRIPTION);
        validateRequired(description, "Product Description", errors);
        if (description != null && description.length() > 1000) {
            errors.add("Product Description must not exceed 1000 characters");
        }

        // ── Warnings / Precautions ────────────────────────────────────────
        String warnings = getCsvString(r, H_WARNINGS);
        validateRequired(warnings, "Warnings", errors);
        if (warnings != null && warnings.length() > 1000) {
            errors.add("Warnings / Precautions must not exceed 1000 characters");
        }

        // ── Other mandatory text fields ───────────────────────────────────
        validateRequired(getCsvString(r, H_THERAPEUTIC_CAT), "Therapeutic Category", errors);
        validateRequired(getCsvString(r, H_THERAPEUTIC_SUBCAT), "Therapeutic Sub Category", errors);
        validateRequired(getCsvString(r, H_MOLECULES), "Molecule", errors);
        validateRequired(getCsvString(r, H_DOSAGE_FORM), "Dosage Form", errors);
        validateRequired(getCsvString(r, H_STORAGE_CONDITION), "Storage Condition", errors);

        // ── Pack Type ─────────────────────────────────────────────────────
        validateRequired(getCsvString(r, H_PACK_TYPE), "Pack Type", errors);

        // ── Unit Per Pack ─────────────────────────────────────────────────────
        String unitPerPackRaw = getCsvString(r, H_UNIT_PER_PACK);
        Long unitPerPack = getCsvLong(r, H_UNIT_PER_PACK);
        validateRequired(unitPerPackRaw, "Number of Units per Pack Type", errors);
        if (unitPerPackRaw != null && !unitPerPackRaw.isBlank()) {
            if (unitPerPack == null) {
                errors.add("Number of Units per Pack Type must be numeric");
            } else if (unitPerPack <= 0) {
                errors.add("Number of Units per Pack Type must be a positive value");
            }
        }

        // ── Number of Packs ───────────────────────────────────────────────
        Long numberOfPacks = getCsvLong(r, H_NUMBER_OF_PACKS);
        validateRequired(numberOfPacks, "Number of Packs", errors);
        if (numberOfPacks != null && numberOfPacks <= 0) {
            errors.add("Number of Packs must be a positive value");
        }

        // ── Min / Max Order Quantities ────────────────────────────────────
        Long minOrderQty = getCsvLong(r, H_MIN_ORDER_QTY);
        Long maxOrderQty = getCsvLong(r, H_MAX_ORDER_QTY);

        validateRequired(minOrderQty, "Minimum Order Qty", errors);
        if (minOrderQty != null && minOrderQty <= 0) {
            errors.add("Minimum Order Qty must be a positive value");
        }

        validateRequired(maxOrderQty, "Max Order Qty", errors);
        if (maxOrderQty != null && maxOrderQty <= 0) {
            errors.add("Max Order Qty must be a positive value");
        }

        if (minOrderQty != null && maxOrderQty != null
                && minOrderQty > 0 && maxOrderQty > 0
                && minOrderQty > maxOrderQty) {
            errors.add("Minimum Order Qty must be ≤ Maximum Order Qty");
        }

        // ── Batch Number ──────────────────────────────────────────────────
        String batchNumber = getCsvString(r, H_BATCH_NUMBER);
        validateRequired(batchNumber, "Batch Number", errors);
        if (batchNumber != null && !batchNumber.isBlank()) {
            if (!batchNumber.matches("[A-Za-z0-9]+")) {
                errors.add("Batch Number must be alphanumeric only (no special characters)");
            }
            if (batchNumber.length() < 3) {
                errors.add("Batch Number must be at least 3 characters");
            }
            if (batchNumber.length() > 20) {
                errors.add("Batch Number must not exceed 20 characters");
            }
            if (pricingDetailsService.isBatchNumberExistsForSeller(batchNumber, userId, categoryId)) {
                errors.add("Batch Number '" + batchNumber + "' already exists for this seller");
            }
        }

        // ── Manufacturing Date ────────────────────────────────────────────
        LocalDate mfgDate = parseCsvDate(getCsvString(r, H_MFG_DATE));
        validateRequired(mfgDate, "Manufacturing Date", errors);
        if (mfgDate != null && YearMonth.from(mfgDate).isAfter(YearMonth.now())) {
            errors.add("Manufacturing Date cannot be a future month");
        }

        // ── Expiry Date ───────────────────────────────────────────────────
        LocalDate expiryDate = parseCsvDate(getCsvString(r, H_EXPIRY_DATE));
        validateRequired(expiryDate, "Expiry Date", errors);

        if (mfgDate != null && expiryDate != null && expiryDate.isBefore(mfgDate)) {
            errors.add("Expiry Date cannot be before Manufacturing Date");
        }

        // ── Stock Quantity ────────────────────────────────────────────────
        Long stockQty = getCsvLong(r, H_STOCK_QTY);
        validateRequired(stockQty, "Stock Quantity", errors);
        if (stockQty != null && stockQty <= 0) {
            errors.add("Stock Quantity must be a positive value");
        }
        if (stockQty != null && minOrderQty != null && stockQty < minOrderQty) {
            errors.add("Stock Quantity (" + stockQty + ") must be ≥ Minimum Order Quantity (" + minOrderQty + ")");
        }

        // ── MRP ───────────────────────────────────────────────────────────
        Long mrp = getCsvLong(r, H_MRP);
        validateRequired(mrp, "MRP", errors);
        if (mrp != null && mrp <= 0) {
            errors.add("MRP must be greater than 0");
        }

        // ── Selling Price ─────────────────────────────────────────────────
        Long sellingPrice = getCsvLong(r, H_SELLING_PRICE);
        validateRequired(sellingPrice, "Selling Price", errors);
        if (sellingPrice != null && sellingPrice <= 0) {
            errors.add("Selling Price must be greater than 0");
        }
        if (sellingPrice != null && mrp != null && sellingPrice > mrp) {
            errors.add("Selling Price cannot be greater than MRP");
        }

        // ── Discount % ────────────────────────────────────────────────────
        Long discountPct = getCsvLong(r, H_DISCOUNT_PCT);
        if (discountPct != null && (discountPct < 0 || discountPct > 100)) {
            errors.add("Discount % must be in the range 0–100");
        }

        // ── GST % ─────────────────────────────────────────────────────────
        Long gstPct = getCsvLong(r, H_GST_PCT);
        validateRequired(gstPct, "GST %", errors);
        if (gstPct != null && !VALID_GST_VALUES.contains(gstPct)) {
            errors.add("GST % must be one of: 0, 5, 12, 18");
        }

        // ── HSN Code ──────────────────────────────────────────────────────
        Long hsnCode = getCsvLong(r, H_HSN_CODE);
        validateRequired(hsnCode, "HSN Code", errors);
        if (hsnCode != null) {
            int digits = String.valueOf(hsnCode).length();
            if (digits != 4 && digits != 6 && digits != 8) {
                errors.add("HSN Code must be 4, 6, or 8 digits");
            }
        }

        // ── Date of Entry — ignored, always today; no validation needed ───

        // ── Molecule + Strength ───────────────────────────────────────────
        String moleculeCell = getCsvString(r, H_MOLECULES);
        String strengthCell = getCsvStringByIndex(r, COL_STRENGTH);
        validateMoleculeStrength(moleculeCell, strengthCell, errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    // ── Shared molecule+strength validation ───────────────────────────────
    private void validateMoleculeStrength(String moleculeCell, String strengthCell, List<String> errors) {
        if (moleculeCell == null || moleculeCell.isBlank()) return;

        String[] molecules = Arrays.stream(moleculeCell.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        if (strengthCell == null || strengthCell.isBlank()) {
            errors.add("Strength is mandatory for molecules: " + String.join(", ", molecules));
            return;
        }

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

    private void validateRequired(Object value, String field, List<String> errors) {
        if (value == null) {
            errors.add(field + " is mandatory");
        } else if (value instanceof String && ((String) value).isBlank()) {
            errors.add(field + " is mandatory");
        }
    }
}