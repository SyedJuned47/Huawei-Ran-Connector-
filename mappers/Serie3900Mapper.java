package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers;

import java.io.FileInputStream;
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

import com.mobinets.nps.customer.transmission.common.CommonConfig;
import com.mobinets.nps.customer.transmission.common.ExcelFileHeaderIndexFinder;
import com.mobinets.nps.customer.transmission.common.ManufactureFixer;

public class Serie3900Mapper extends ExcelFileHeaderIndexFinder {
	
	private static final Log log = LogFactory.getLog(Serie3900Mapper.class);
	
	// flexi radio file headers
	private String rackNameHeader;
	private String frameNameHeader;
	private String frameIndexHeader;
	private String ruIndexHeader;
	private CommonConfig m2v2r0Config;
	private static final String SEP = System.getProperty("file.separator");
	
	private Map<String, Integer> serie3900RuMap;
	

	

	public void setM2v2r0Config(CommonConfig m2v2r0Config) {
		this.m2v2r0Config = m2v2r0Config;
	}

	/**
	 * 
	 */
	void init() {
		
		log.info("Begin Reading the serie 3900 Ru Mapper file ...");
		serie3900RuMap = new HashMap<String, Integer>();
		
		InputStream inputStream = null;
		
		try {
			String path = m2v2r0Config.getProperty("static.file.HwNatching");
			 inputStream = new FileInputStream(path);
			
			Workbook workbook = WorkbookFactory.create(inputStream);
			
			Sheet sheet = workbook.getSheetAt(3);
			
			Iterator<Row> rowsIte = sheet.rowIterator();
			if(!rowsIte.hasNext())
				return;
			
			addExcelHeaders(rackNameHeader, frameNameHeader, frameIndexHeader, ruIndexHeader);
			boolean isOk = checkHeaderInHeaderRow(rowsIte.next()); // first row
			if(!isOk) {
				log.error("Error in the following serie 3900 Ru Mapper File headers: " + rackNameHeader + ", " + frameNameHeader + ", " + frameIndexHeader + ", " + ruIndexHeader + " !!");
				return;
			}
			
			StringBuffer strBuffer = new StringBuffer();
			
			while(rowsIte.hasNext()) {
				Row row = rowsIte.next();
				
				Cell rackNameCell = row.getCell(headerIndexOf(rackNameHeader));
				Cell frameNameCell = row.getCell(headerIndexOf(frameNameHeader));
				Cell frameIndexCell = row.getCell(headerIndexOf(frameIndexHeader));
				Cell ruIndexCell = row.getCell(headerIndexOf(ruIndexHeader));
				
				String rackNameVal = getCellValue(rackNameCell).trim();
				String frameNameVal = getCellValue(frameNameCell).trim();
				
				String frameIndexVal = getCellValue(frameIndexCell).trim();				
				try {frameIndexVal = Double.valueOf(frameIndexVal).intValue() + "";} catch (Exception e) {continue;}
				
				
				String ruIndexVal = getCellValue(ruIndexCell).toUpperCase().trim();				
				try {ruIndexVal = Double.valueOf(ruIndexVal).intValue() + "";} catch (Exception e) {continue;}
				
				boolean isFilledValues = checkIfAllValuesFilled(rackNameVal, frameIndexVal, frameNameVal, ruIndexVal);
				if(!isFilledValues)
					continue;
				
				int ruIndexInt = -1;
				try {
					ruIndexInt = Integer.valueOf(ruIndexVal.trim());
				} catch (NumberFormatException e) {
					continue;
				}
					
				// append neType|shelfType|shelfIndex
				strBuffer.append(rackNameVal);
				strBuffer.append("|");
				strBuffer.append(frameNameVal);
				strBuffer.append("|");
				strBuffer.append(frameIndexVal);
				
				serie3900RuMap.put(strBuffer.toString().toUpperCase(), ruIndexInt);
				
				strBuffer.setLength(0); // clear buffer for next ite
			}
			
			strBuffer = null;
			log.info("Finish Reading the HW_MatchingFile sheet series3900_RuMatching with list size: " + serie3900RuMap.size());
		} catch (Exception e) {
			log.error("Error when reading HW_MatchingFile sheet series3900_RuMatching !!", e);
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
	public Integer getRuNumberForSerie3900(String rackType, String shelfType, String shelfIndex) {
		
		if(rackType == null || shelfType ==null || shelfIndex == null)
			return null;
		
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(rackType);
		strBuffer.append("|");
		strBuffer.append(shelfType);
		strBuffer.append("|");		
		strBuffer.append(shelfIndex);
		
		String keyStr = strBuffer.toString().toUpperCase();
		strBuffer = null;
		
		Integer ruIndex = serie3900RuMap.get(keyStr);
		
		return ruIndex;
	}
	
	/**  
	  * **************
	  * Setters
	  * **************
	*/
	
	public void setRackNameHeader(String rackNameHeader) {
		this.rackNameHeader = rackNameHeader;
	}

	public void setFrameNameHeader(String frameNameHeader) {
		this.frameNameHeader = frameNameHeader;
	}

	public void setFrameIndexHeader(String frameIndexHeader) {
		this.frameIndexHeader = frameIndexHeader;
	}

	public void setRuIndexHeader(String ruIndexHeader) {
		this.ruIndexHeader = ruIndexHeader;
	}
	
	/**
	 * Main
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {		
        ApplicationContext classw = new ClassPathXmlApplicationContext("manufactures/huawei/m2000v2r0en0sp2ran/huawei-m2000v2r0en0sp2ran-spring-config.xml");
		Serie3900Mapper serie3900 = (Serie3900Mapper) classw.getBean(Serie3900Mapper.class);
		System.out.println(serie3900.getRuNumberForSerie3900("APM30", "EMU", "41"));
		System.out.println(serie3900.getRuNumberForSerie3900("APM30", "FAN", "11"));
	}

}
