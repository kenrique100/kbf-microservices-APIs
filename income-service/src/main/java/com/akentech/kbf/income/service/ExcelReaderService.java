package com.akentech.kbf.income.service;

import com.akentech.shared.models.Income;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelReaderService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ExcelReaderService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Reads income data from an Excel file and publishes it to Kafka.
     *
     * @param inputStream The InputStream of the Excel file.
     * @return A list of Income objects.
     */
    public List<Income> readIncomeDataFromExcel(InputStream inputStream) {
        List<Income> incomeList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip the header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Income income = new Income();

                // Read data from each cell
                income.setReason(getCellValueAsString(row.getCell(0))); // Reason (String)
                income.setIncomeDate(getCellValueAsLocalDate(row.getCell(1))); // Income Date (LocalDate)
                income.setQuantity(getCellValueAsInt(row.getCell(2))); // Quantity (int)
                income.setAmountReceived(getCellValueAsBigDecimal(row.getCell(3))); // Amount Received (BigDecimal)
                income.setExpectedAmount(getCellValueAsBigDecimal(row.getCell(4))); // Expected Amount (BigDecimal)
                income.setReceipt(getCellValueAsString(row.getCell(5))); // Receipt (String)
                income.setCreatedBy(getCellValueAsString(row.getCell(6))); // Created By (String)

                incomeList.add(income);

                // Publish the income data to Kafka
                kafkaTemplate.send("income-topic", income);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return incomeList;
    }

    /**
     * Helper method to get cell value as String.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        cell.setCellType(CellType.STRING); // Ensure the cell is treated as a string
        return cell.getStringCellValue();
    }

    /**
     * Helper method to get cell value as LocalDate.
     */
    private LocalDate getCellValueAsLocalDate(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            return LocalDate.parse(cell.getStringCellValue(), DateTimeFormatter.ISO_LOCAL_DATE);
        }
        throw new IllegalArgumentException("Cell is not a valid date: " + cell);
    }

    /**
     * Helper method to get cell value as int.
     */
    private int getCellValueAsInt(Cell cell) {
        if (cell == null) {
            return 0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            return Integer.parseInt(cell.getStringCellValue());
        }
        throw new IllegalArgumentException("Cell is not a valid integer: " + cell);
    }

    /**
     * Helper method to get cell value as BigDecimal.
     */
    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            return new BigDecimal(cell.getStringCellValue());
        }
        throw new IllegalArgumentException("Cell is not a valid number: " + cell);
    }
}