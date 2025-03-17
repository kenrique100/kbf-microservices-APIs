package com.akentech.kbf.expense.service;

import com.akentech.kbf.expense.utils.ExcelUtil;
import com.akentech.shared.models.Expense;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service for reading expense data from an Excel file.
 */
@Service
public class ExcelReaderService {

    /**
     * Reads expense data from an Excel (.xlsx) file input stream.
     *
     * @param inputStream The input stream of the uploaded Excel file.
     * @return A {@link List} of {@link Expense} objects extracted from the Excel file.
     * @throws RuntimeException if there is an error reading the file.
     */
    public List<Expense> readExpenseDataFromExcel(InputStream inputStream) {
        List<Expense> expenseList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip the header row
            }

            // Iterate over each row and extract expense data
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Expense expense = new Expense();

                // Extract values using utility functions to handle various data types
                expense.setReason(ExcelUtil.getCellValueAsString(row.getCell(0)));         // Expense reason
                expense.setExpenseDate(ExcelUtil.getCellValueAsLocalDate(row.getCell(1))); // Date of expense
                expense.setQtyPurchased(ExcelUtil.getCellValueAsInt(row.getCell(2)));      // Quantity purchased
                expense.setAmountPaid(ExcelUtil.getCellValueAsBigDecimal(row.getCell(3))); // Amount paid
                expense.setExpectedAmount(ExcelUtil.getCellValueAsBigDecimal(row.getCell(4))); // Expected amount
                expense.setReceipt(ExcelUtil.getCellValueAsString(row.getCell(5)));        // Receipt details
                expense.setCreatedBy(ExcelUtil.getCellValueAsString(row.getCell(6)));      // Creator of expense

                expenseList.add(expense);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return expenseList;
    }
}
