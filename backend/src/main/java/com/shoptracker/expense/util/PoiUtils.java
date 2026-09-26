package com.shoptracker.expense.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/** Small helpers to read values from POI cells defensively. */
public final class PoiUtils {

    private PoiUtils() {}

    public static String getString(Row row, int col) {
        if (row == null) return "";
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) yield String.valueOf((long) d);
                yield String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    public static double getNumber(Row row, int col) {
        if (row == null) return 0d;
        Cell cell = row.getCell(col);
        if (cell == null) return 0d;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return 0d;
            }
        }
        return 0d;
    }

    public static LocalDate getDate(Row row, int col) {
        if (row == null) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            Date d = cell.getDateCellValue();
            return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String s = getString(row, col);
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    /** Excel worksheet names cannot contain : \ / ? * [ ] and max 31 chars. */
    public static String sanitizeSheetName(String name) {
        String cleaned = name.replaceAll("[:\\\\/?*\\[\\]]", "-").trim();
        if (cleaned.length() > 31) cleaned = cleaned.substring(0, 31);
        if (cleaned.isEmpty()) cleaned = "Buyer";
        return cleaned;
    }
}
