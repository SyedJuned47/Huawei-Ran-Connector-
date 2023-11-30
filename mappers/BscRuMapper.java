package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mobinets.nps.customer.transmission.common.ExcelFileHeaderIndexFinder;

public class BscRuMapper extends ExcelFileHeaderIndexFinder  {
	
	private static final Log log = LogFactory.getLog(BscRuMapper.class);
	
	// flexi radio file headers
	private String shelfNameHeader;
	private String frameIndexHeader;
	private String ruIndexHeader;
	
	private static final String SEP = System.getProperty("file.separator");
	private Map<String, Integer> bscRuMap;
	
	/**
	 * 
	 */
	void init() {
		
		log.info("Begin Reading the bsc Huawei bsc Ru Mapper file ...");
		bscRuMap = new HashMap<String, Integer>();
		
		InputStream inputStream = null;
		
		try {
			
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("manufactures" + SEP
					 + "huawei" + SEP + "m2000v2r0en0sp2ran" + SEP + "HW_MatchingFile.xlsx");
			
			if(null == inputStream) {
				log.error("Can't find file \"HW_MatchingFile.xlsx\" !!");
				return;
			}
			
			Workbook workbook = WorkbookFactory.create(inputStream);
			
			Sheet sheet = workbook.getSheetAt(4);
			
			Iterator<Row> rowsIte = sheet.rowIterator();
			if(!rowsIte.hasNext())
				return;
			
			addExcelHeaders(shelfNameHeader, frameIndexHeader, ruIndexHeader);
			
			boolean isOk = checkHeaderInHeaderRow(rowsIte.next()); // first row
			if(!isOk) {
				log.error("Error in the following bsc Ru Mapper File headers: " + shelfNameHeader + ", " + frameIndexHeader + ", " + ruIndexHeader + " !!");
				return;
			}
			
			StringBuffer strBuffer = new StringBuffer();
			
			while(rowsIte.hasNext()) {
				Row row = rowsIte.next();
				
				Cell shelfNameCell = row.getCell(headerIndexOf(shelfNameHeader));
				Cell frameIndexCell = row.getCell(headerIndexOf(frameIndexHeader));
				Cell ruIndexCell = row.getCell(headerIndexOf(ruIndexHeader));
				
				String shelfNameVal = getCellValue(shelfNameCell).trim();
				
				String shelfIndexVal = getCellValue(frameIndexCell);
				try {shelfIndexVal = Double.valueOf(shelfIndexVal).intValue() + "";} catch (Exception e) {continue;}
				
				
				String ruIndexVal = getCellValue(ruIndexCell).toUpperCase().trim();
				try {ruIndexVal = Double.valueOf(ruIndexVal).intValue() + "";} catch (Exception e) {continue;}
				
				boolean isFilledValues = checkIfAllValuesFilled(shelfNameVal, shelfIndexVal, ruIndexVal);
				if(!isFilledValues)
					continue;
				
				int ruIndexInt = -1;
				try {
					ruIndexInt = Integer.valueOf(ruIndexVal);
				} catch (NumberFormatException e) {
					continue;
				}
					
				// append shelfType|shelfIndex
				strBuffer.append(shelfNameVal);
				strBuffer.append("|");
				strBuffer.append(shelfIndexVal);
				
				bscRuMap.put(strBuffer.toString().toUpperCase(), ruIndexInt);
				
				strBuffer.setLength(0); // clear buffer for next ite
				
			}
			
			strBuffer = null;
			log.info("Finish Reading the Bsc Ru Mapper file with list size: " + bscRuMap.size());
		} catch (Exception e) {
			log.error("Error when reading HW_MatchingFile sheet bsc_RuMatching !!", e);
		} finally {
			try {
    			if(inputStream != null)
    				inputStream.close(); // close file
			} catch (Exception e) {
				log.error("Error closing file !!", e);
			}
		}
	}
	
	/***
	 * 
	 * @param neType
	 * @param shelfType
	 * @param shelfIndex
	 * @return
	 */
	public Integer getRuNumberForBsc(String shelfType, String shelfIndex) {
		
		if(shelfType == null || shelfIndex == null)
			return null;
		
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(shelfType);
		strBuffer.append("|");
		strBuffer.append(shelfIndex);
		
		String keyStr = strBuffer.toString().toUpperCase();
		strBuffer = null;
		return bscRuMap.get(keyStr);
	}
	
	/**  
	  * **************
	  * Setters
	  * **************
	*/
	
	public void setShelfNameHeader(String shelfNameHeader) {
		this.shelfNameHeader = shelfNameHeader;
	}

	public void setFrameIndexHeader(String shelfTypeHeader) {
		this.frameIndexHeader = shelfTypeHeader;
	}

	public void setRuIndexHeader(String ruIndexHeader) {
		this.ruIndexHeader = ruIndexHeader;
	}
	
	// main
	@SuppressWarnings("resource")
    public static void main(String[] args) {
		ApplicationContext classw = new ClassPathXmlApplicationContext("manufactures/huawei/m2000v2r0en0sp2ran/huawei-m2000v2r0en0sp2ran-spring-config.xml");
		BscRuMapper bscMapper = (BscRuMapper) classw.getBean(BscRuMapper.class);
		System.out.println(bscMapper.getRuNumberForBsc("EPS", "1"));
	}

}
