package com.example.pharmaaggregatorserver.service.product.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.commons.csv.CSVRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Shared cell-reader utilities for all product Excel / CSV import services.
 * <p>
 * All methods are stateless and package-private so they can be used via
 * {@code import static} inside any concrete import service.
 */
public final class ExcelImportUtils {

    private ExcelImportUtils() {}

    // ── Excel cell readers ────────────────────────────────────────────────

    /**
     * Reads a cell as a trimmed String regardless of its underlying type.
     * Returns {@code null} for blank / missing cells.
     */
    public static String getString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:  return emptyToNull(cell.getStringCellValue().trim());
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return emptyToNull(cell.getCellFormula());
            default:      return null;
        }
    }

    /**
     * Reads a cell as a {@code Long}.
     * Strips any non-digit characters from string cells before parsing.
     * Returns {@code null} for blank / unparseable cells.
     */
    public static Long getLong(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case NUMERIC: return (long) cell.getNumericCellValue();
                case STRING: {
                    String s = cell.getStringCellValue().trim();
                    return s.isEmpty() ? null : Long.parseLong(s.replaceAll("[^0-9]", ""));
                }
                default: return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Reads a date/datetime cell as {@code LocalDateTime}.
     * Supports:
     * <ul>
     *   <li>Native Excel date-formatted numeric cells</li>
     *   <li>String cells in {@code YYYY-MM-DD} format</li>
     *   <li>String cells in {@code DD/MM/YYYY} format</li>
     * </ul>
     */
    public static LocalDateTime getDateTime(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }
            if (cell.getCellType() == CellType.STRING) {
                return parseDateTime(cell.getStringCellValue().trim());
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Returns {@code true} when the row is considered empty for import purposes –
     * i.e. the cell at {@code keyCol} (the mandatory product-name column) is blank.
     */
    public static boolean isRowEmpty(Row row, int keyCol) {
        if (row == null) return true;
        Cell nameCell = row.getCell(keyCol, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (nameCell == null) return true;
        String name = getString(row, keyCol);
        return name == null || name.isBlank();
    }

    // ── CSV cell readers ──────────────────────────────────────────────────

    /** Reads a CSV field as a trimmed String; returns {@code null} when blank or missing. */
    public static String getCsvValue(CSVRecord record, int col) {
        if (col >= record.size()) return null;
        String v = record.get(col).trim();
        return v.isEmpty() ? null : v;
    }

    /** Reads a CSV field as a {@code Long}; returns {@code null} when blank or unparseable. */
    public static Long csvLong(CSVRecord record, int col) {
        String v = getCsvValue(record, col);
        if (v == null) return null;
        try {
            return Long.parseLong(v.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Parses a date/datetime string in {@code YYYY-MM-DD} or {@code DD/MM/YYYY} format. */
    public static LocalDateTime csvDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        return parseDateTime(s.trim());
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            if (s.matches("\\d{4}-\\d{2}-\\d{2}"))
                return LocalDate.parse(s).atStartOfDay();
            if (s.matches("\\d{2}/\\d{2}/\\d{4}")) {
                String[] p = s.split("/");
                return LocalDate.of(
                        Integer.parseInt(p[2]),
                        Integer.parseInt(p[1]),
                        Integer.parseInt(p[0])
                ).atStartOfDay();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}