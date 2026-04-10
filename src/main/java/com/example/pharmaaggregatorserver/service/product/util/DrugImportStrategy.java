package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.*;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import com.example.pharmaaggregatorserver.repository.product.PackTypeRepository;
import com.example.pharmaaggregatorserver.repository.product.TherapeuticCategoryRepository;
import com.example.pharmaaggregatorserver.repository.product.TherapeuticSubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Override
    public ProductDetailsDto mapRow(Row row) {

        ProductDetailsDto dto = new ProductDetailsDto();

        // ── Product core ──────────────────────────────────────────────────
        dto.setProductName(getString(row, COL_PRODUCT_NAME));
        dto.setWarningsPrecautions(getString(row, COL_WARNINGS));
        dto.setProductDescription(getString(row, COL_DESCRIPTION));
        dto.setManufacturerName(getString(row, COL_MANUFACTURER));
        dto.setProductMarketingUrl(getString(row, COL_MARKETING_URL));

        // ── Packaging ────────────────────────────────────────────────────
        Long unitPerPack = getNullSafeLong(row, COL_UNIT_PER_PACK);
        Long numberOfPacks = getNullSafeLong(row, COL_NUMBER_OF_PACKS);

        PackagingDetailsDto packaging = new PackagingDetailsDto();
        packaging.setUnitPerPack(unitPerPack);
        packaging.setNumberOfPacks(numberOfPacks);
        // Pack size is auto-calculated: unitPerPack * numberOfPacks
        if (unitPerPack != null && numberOfPacks != null) {
            packaging.setPackSize(unitPerPack * numberOfPacks);
        }
        packaging.setMinimumOrderQuantity(getNullSafeLong(row, COL_MIN_ORDER_QTY));
        packaging.setMaximumOrderQuantity(getNullSafeLong(row, COL_MAX_ORDER_QTY));

        String packTypeName = getString(row, COL_PACK_TYPE);
        String dosageFormName = getString(row, COL_DOSAGE_FORM);

        if (packTypeName != null && !packTypeName.isBlank()
                && dosageFormName != null && !dosageFormName.isBlank()) {
            Long packId = packTypeRepository
                    .findByPackTypeAndDosageForm_DosageName(packTypeName, dosageFormName)
                    .orElseThrow(() -> new RuntimeException(
                            "Pack type '" + packTypeName + "' not found for dosage form '" + dosageFormName + "'"))
                    .getPackId();
            packaging.setPackId(packId);
        }

        dto.setPackagingDetails(packaging);

        // ── Pricing ───────────────────────────────────────────────────────
        PricingDetailsDto pricing = new PricingDetailsDto();
        pricing.setBatchLotNumber(getString(row, COL_BATCH_NUMBER));
        pricing.setManufacturingDate(toStartOfDay(getLocalDate(row, COL_MFG_DATE)));
        pricing.setExpiryDate(toEndOfDay(getLocalDate(row, COL_EXPIRY_DATE)));
        pricing.setStorageCondition(getString(row, COL_STORAGE_CONDITION));
        pricing.setStockQuantity(getNullSafeLong(row, COL_STOCK_QTY));
        pricing.setDateOfStockEntry(getLocalDate(row, COL_DATE_OF_ENTRY));
        pricing.setMrp(getNullSafeLong(row, COL_MRP));
        pricing.setSellingPrice(getNullSafeLong(row, COL_SELLING_PRICE));
        pricing.setDiscountPercentage(getNullSafeLong(row, COL_DISCOUNT_PCT));
        pricing.setGstPercentage(getNullSafeLong(row, COL_GST_PCT));
        pricing.setHsnCode(getNullSafeLong(row, COL_HSN_CODE));
        pricing.setShelfLifeMonths(getNullSafeLong(row, COL_SHELF_LIFE_MONTHS));

        // ── Additional discount slabs ─────────────────────────────────────
        // 4 slabs × 7 cols each starting at col 27:
        // base+0 = Slab (no field in DTO — skip)
        // base+1 = Minimum Purchase Quantity
        // base+2 = Discount percentage
        // base+3 = Effective Start Date
        // base+4 = Effective Start Time
        // base+5 = Effective End Date
        // base+6 = Effective End Time
        Set<AdditionalDiscountDto> additionalDiscounts = new HashSet<>();

        for (int slab = 0; slab < ADD_DISCOUNT_SLAB_COUNT; slab++) {
            int base = COL_ADD_DISCOUNT_START + (slab * ADD_DISCOUNT_SLAB_SIZE);

            // Check cell type directly — blank Excel cells return 0.0 from getNumericCellValue
            // which would create ghost rows with zero values
            Long minQty = getNullSafeLong(row, base + 1);
            Long discountPct = getNullSafeLong(row, base + 2);

            // Skip slab if both key fields are absent or zero (empty template row)
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

        if (!additionalDiscounts.isEmpty()) {
            pricing.setAdditionalDiscounts(additionalDiscounts);
        }

        dto.setPricingDetails(Set.of(pricing));

        // ── Drug attribute ────────────────────────────────────────────────
        ProductAttributeDrugDto attr = new ProductAttributeDrugDto();
        attr.setDosageForm(getString(row, COL_DOSAGE_FORM));

        // Resolve therapeutic category and subcategory by name → ID
        String therapeuticCategoryName = getString(row, COL_THERAPEUTIC_CAT);
        String therapeuticSubcategoryName = getString(row, COL_THERAPEUTIC_SUBCAT);

        if (therapeuticCategoryName != null && !therapeuticCategoryName.isBlank()) {
            String catId = therapeuticCategoryRepository
                    .findByTherapeuticCategory(therapeuticCategoryName)
                    .orElseThrow(() -> new RuntimeException(
                            "Therapeutic category not found: " + therapeuticCategoryName))
                    .getTherapeuticCategoryId();
            attr.setTherapeuticCategoryId(catId);
        }

        if (therapeuticSubcategoryName != null && !therapeuticSubcategoryName.isBlank()) {
            String subCatId = therapeuticSubcategoryRepository
                    .findByTherapeuticSubcategory(therapeuticSubcategoryName)
                    .orElseThrow(() -> new RuntimeException(
                            "Therapeutic subcategory not found: " + therapeuticSubcategoryName))
                    .getTherapeuticSubcategoryId();
            attr.setTherapeuticSubcategoryId(subCatId);
        }

        // ── Molecules + per-molecule strength ─────────────────────────────
        String moleculeCell = getString(row, COL_MOLECULES);
        String strengthCell = getString(row, COL_STRENGTH);

        if (moleculeCell != null && !moleculeCell.isBlank()) {
            String[] moleculeNames = moleculeCell.split(",");
            String[] strengths = (strengthCell != null && !strengthCell.isBlank())
                    ? strengthCell.split(",")
                    : new String[0];

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

        dto.setProductAttributeDrugs(Set.of(attr));

        return dto;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
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

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59) : null;
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
                // Cell is genuinely numeric — but could still be 0 from an empty template cell
                // that Excel pre-formats as numeric. Check if the raw value is 0 with no
                // other slab data present — handled at the caller level.
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
}