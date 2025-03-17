package com.akentech.kbf.expense.service;


import com.akentech.kbf.expense.utils.ExcelUtil;
import com.akentech.shared.models.Expense;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelReaderService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ExcelReaderService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Expense> readExpenseDataFromExcel(InputStream inputStream) {
        List<Expense> expenseList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Expense expense = new Expense();

                // Using utility methods
                expense.setReason(ExcelUtil.getCellValueAsString(row.getCell(0)));
                expense.setExpenseDate(ExcelUtil.getCellValueAsLocalDate(row.getCell(1)));
                expense.setQtyPurchased(ExcelUtil.getCellValueAsInt(row.getCell(2)));
                expense.setAmountPaid(ExcelUtil.getCellValueAsBigDecimal(row.getCell(3)));
                expense.setExpectedAmount(ExcelUtil.getCellValueAsBigDecimal(row.getCell(4)));
                expense.setReceipt(ExcelUtil.getCellValueAsString(row.getCell(5)));
                expense.setCreatedBy(ExcelUtil.getCellValueAsString(row.getCell(6)));

                expenseList.add(expense);
                kafkaTemplate.send("expense-topic", expense);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return expenseList;
    }
}
