package com.app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.app.dto.UserDTO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class FileConverter {

	public static void saveFile(File file, List<UserDTO> users) throws Exception {
		
		FileOutputStream fos = null;
		Workbook workbook = null;
		
		if (file.getName().endsWith("xlsx")) {
			workbook = new SXSSFWorkbook();
			
		} else if (file.getName().endsWith("xls")) {
			workbook = new HSSFWorkbook();
			
		} else {
			throw new Exception("invalid file name, should be xls or xlsx");
		}
		
		Sheet sheet = workbook.createSheet();
		
		for (int i = 0; i < users.size() + 1; i++) {
			Row row = sheet.createRow(i);
			
			if (i == 0) {
				Cell cell0 = row.createCell(0);
				cell0.setCellValue("이메일");
				Cell cell1 = row.createCell(1);
				cell1.setCellValue("키워드1");
				Cell cell2 = row.createCell(2);
				cell2.setCellValue("키워드2");
				
			} else {
				UserDTO user = users.get(i-1);
				
				Cell cell0 = row.createCell(0);
				cell0.setCellValue(user.getEmail());
				Cell cell1 = row.createCell(1);
				cell1.setCellValue(user.getFirstKeyword());
				Cell cell2 = row.createCell(2);
				cell2.setCellValue(user.getSecondKeyword());
				
			}

		}
		
		try {
			fos = new FileOutputStream(file);
			workbook.write(fos);
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (workbook != null) {
				workbook.close();
			}
		}
		
		System.out.println("저장완료");
	}
	
	public static ObservableList<UserDTO> loadFile(File file) throws Exception {
		
		ObservableList<UserDTO> users = FXCollections.observableArrayList();
		
		FileInputStream fis = null;
		Workbook workbook = null;
		
		try {
			fis = new FileInputStream(file);
			
			if (file.getName().endsWith("xlsx")) {
				workbook = new XSSFWorkbook(fis);
				
			} else if (file.getName().endsWith("xls")) {
				workbook = new HSSFWorkbook(fis);
				
			} else {
				throw new Exception("invalid file name, should be xls or xlsx");
			}
			
			Sheet sheet = workbook.getSheetAt(0);
			int rows = sheet.getPhysicalNumberOfRows();
			
			for (int i = 1; i < rows; i++) {
				Row row = sheet.getRow(i);
				
				if (row != null) {
					UserDTO user = new UserDTO(i, row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue(), row.getCell(2).getStringCellValue());
					users.add(user);
				}
			}
			
			return users;
			
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (workbook != null) {
				workbook.close();
			}
		}
		
	}

}
