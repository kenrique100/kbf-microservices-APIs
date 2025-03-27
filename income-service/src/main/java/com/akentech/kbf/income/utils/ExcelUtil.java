package com.akentech.kbf.income.utils;

import com.akentech.kbf.income.exception.ExcelProcessingException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public final class ExcelUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ExcelUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String getCellValueAsString(Cell cell) {
        Objects.requireNonNull(cell, "Cell cannot be null");

        try {
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().trim();
            } else if (cell.getCellType() == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMATTER);
                }
                return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            } else if (cell.getCellType() == CellType.BOOLEAN) {
                return String.valueOf(cell.getBooleanCellValue());
            }
            return null;
        } catch (Exception e) {
            throw new ExcelProcessingException("Error reading string value from cell", e);
        }
    }

    public static LocalDate getCellValueAsLocalDate(Cell cell) {
        Objects.requireNonNull(cell, "Cell cannot be null");

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                String dateString = cell.getStringCellValue().trim();
                return LocalDate.parse(dateString, DATE_FORMATTER);
            }
            throw new ExcelProcessingException("Cell is not a valid date");
        } catch (DateTimeParseException e) {
            throw new ExcelProcessingException("Invalid date format. Expected format: yyyy-MM-dd", e);
        } catch (Exception e) {
            throw new ExcelProcessingException("Error reading date value from cell", e);
        }
    }

    public static int getCellValueAsInt(Cell cell) {
        Objects.requireNonNull(cell, "Cell cannot be null");

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double value = cell.getNumericCellValue();
                if (value % 1 != 0) {
                    throw new ExcelProcessingException("Value is not an integer");
                }
                return (int) value;
            } else if (cell.getCellType() == CellType.STRING) {
                return Integer.parseInt(cell.getStringCellValue().trim());
            }
            throw new ExcelProcessingException("Cell is not a valid integer");
        } catch (NumberFormatException e) {
            throw new ExcelProcessingException("Invalid integer value", e);
        } catch (Exception e) {
            throw new ExcelProcessingException("Error reading integer value from cell", e);
        }
    }

    public static BigDecimal getCellValueAsBigDecimal(Cell cell) {
        Objects.requireNonNull(cell, "Cell cannot be null");

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                return new BigDecimal(cell.getStringCellValue().trim());
            }
            throw new ExcelProcessingException("Cell is not a valid number");
        } catch (NumberFormatException e) {
            throw new ExcelProcessingException("Invalid numeric value", e);
        } catch (Exception e) {
            throw new ExcelProcessingException("Error reading numeric value from cell", e);
        }
    }
}