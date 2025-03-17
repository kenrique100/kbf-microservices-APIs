package com.akentech.kbf.expense.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for extracting cell values from an Excel sheet.
 * <p>
 * This class provides helper methods to safely retrieve different data types (String, LocalDate, Integer, BigDecimal)
 * from an Apache POI {@link Cell}, handling null values and different cell formats.
 */
public class ExcelUtil {

    // Private constructor to prevent instantiation of utility class
    private ExcelUtil() {}

    /**
     * Retrieves the value of an Excel cell as a String.
     *
     * @param cell The {@link Cell} object from which the value is extracted.
     * @return The cell value as a {@link String}, or {@code null} if the cell is empty or not of type String.
     */
    public static String getCellValueAsString(Cell cell) {
        return (cell == null || cell.getCellType() != CellType.STRING) ? null : cell.getStringCellValue();
    }

    /**
     * Retrieves the value of an Excel cell as a {@link LocalDate}.
     * <p>
     * Supports two formats:
     * <ul>
     *   <li>If the cell contains a numeric value, it is treated as an Excel date and converted to {@link LocalDate}.</li>
     *   <li>If the cell contains a String, it attempts to parse it using the ISO-8601 format.</li>
     * </ul>
     *
     * @param cell The {@link Cell} object containing the date value.
     * @return The cell value as a {@link LocalDate}, or {@code null} if the format is invalid.
     */
    public static LocalDate getCellValueAsLocalDate(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getLocalDateTimeCellValue().toLocalDate(); // Excel-stored date
            case STRING -> LocalDate.parse(cell.getStringCellValue(), DateTimeFormatter.ISO_LOCAL_DATE); // String-formatted date
            default -> null;
        };
    }

    /**
     * Retrieves the value of an Excel cell as an integer.
     * <p>
     * Supports:
     * <ul>
     *   <li>Numeric cells - Extracts and converts the value to an integer.</li>
     *   <li>String cells - Parses the string as an integer.</li>
     * </ul>
     * If the cell is null or contains an invalid format, it returns {@code 0}.
     *
     * @param cell The {@link Cell} object containing the integer value.
     * @return The cell value as an {@code int}, or {@code 0} if the format is invalid.
     * @throws NumberFormatException if the string value cannot be converted to an integer.
     */
    public static int getCellValueAsInt(Cell cell) {
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue(); // Extract number
            case STRING -> {
                try {
                    yield Integer.parseInt(cell.getStringCellValue()); // Parse string
                } catch (NumberFormatException e) {
                    yield 0; // Return 0 for invalid numbers
                }
            }
            default -> 0;
        };
    }

    /**
     * Retrieves the value of an Excel cell as a {@link BigDecimal}.
     * <p>
     * Supports:
     * <ul>
     *   <li>Numeric cells - Converts the numeric value to {@link BigDecimal}.</li>
     *   <li>String cells - Parses the string as a {@link BigDecimal}.</li>
     * </ul>
     *
     * @param cell The {@link Cell} object containing the decimal value.
     * @return The cell value as a {@link BigDecimal}, or {@code null} if the format is invalid.
     * @throws NumberFormatException if the string value cannot be converted to a BigDecimal.
     */
    public static BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()); // Extract number
            case STRING -> {
                try {
                    yield new BigDecimal(cell.getStringCellValue()); // Parse string
                } catch (NumberFormatException e) {
                    yield null; // Return null for invalid numbers
                }
            }
            default -> null;
        };
    }
}
