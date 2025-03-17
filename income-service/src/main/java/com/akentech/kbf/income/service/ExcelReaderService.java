package com.akentech.kbf.income.service;

import com.akentech.kbf.income.util.ExcelUtil;
import com.akentech.shared.models.Income;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelReaderService {


    public List<Income> readIncomeDataFromExcel(InputStream inputStream) {
        List<Income> incomeList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Income income = new Income();

                // Using utility methods
                income.setReason(ExcelUtil.getCellValueAsString(row.getCell(0)));
                income.setIncomeDate(ExcelUtil.getCellValueAsLocalDate(row.getCell(1)));
                income.setQuantity(ExcelUtil.getCellValueAsInt(row.getCell(2)));
                income.setAmountReceived(ExcelUtil.getCellValueAsBigDecimal(row.getCell(3)));
                income.setExpectedAmount(ExcelUtil.getCellValueAsBigDecimal(row.getCell(4)));
                income.setReceipt(ExcelUtil.getCellValueAsString(row.getCell(5)));
                income.setCreatedBy(ExcelUtil.getCellValueAsString(row.getCell(6)));

                incomeList.add(income);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return incomeList;
    }
}
