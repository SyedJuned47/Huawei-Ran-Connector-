package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

public class ModelByRackTypeRuleParser extends ExcelFileHeaderIndexFinder {
	
	private static final Log log = LogFactory.getLog(ModelByRackTypeRuleParser.class);
	
	private String rackTypeHeader;
	private String frameTypeHeader;
	private String neTypeHeader;
	private String requiredRackTypeHeader;
	private String noRequiredRackTypeHeader;
	private String requiredFramTypeHeader;
	private String noRequiredFramTypeHeader;
	private String neModelHeader;
	
	private static final String SEP = System.getProperty("file.separator");
	
	private Map<String, String> mapWithFrameType;
	private Map<String, String> mapWithoutFrameType;
	private Map<String, String> mapWithoutRackType;
	private Map<String, String> mapWithRequiredRackType;
	private Map<String, String> mapWithNoRequiredRackTypeAndNoRequiredFrameType;
	
	/**
	 * 
	 */
	void init() {
		
		log.info("Begin Reading the RackType Rule Mapper file ...");
		mapWithFrameType = new HashMap<String, String>();
		mapWithoutFrameType = new HashMap<String, String>();
		mapWithoutRackType = new HashMap<String, String>();
		mapWithRequiredRackType = new HashMap<String, String>();
		mapWithNoRequiredRackTypeAndNoRequiredFrameType = new HashMap<String, String>();
		
		InputStream inputStream = null;
		
		try {
			
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("manufactures" + SEP
					 + "huawei" + SEP + "m2000v2r0en0sp2ran" + SEP + "HW_MatchingFile.xlsx");
			
			if(null == inputStream) {
				log.error("Can't find file \"HW_MatchingFile.xlsx\" !!");
				return;
			}
			
			Workbook workbook = WorkbookFactory.create(inputStream);
			
			Sheet sheet = workbook.getSheetAt(5);
			
			Iterator<Row> rowsIte = sheet.rowIterator();
			if(!rowsIte.hasNext())
				return;
			
			addExcelHeaders(rackTypeHeader, frameTypeHeader, neTypeHeader, requiredRackTypeHeader, noRequiredRackTypeHeader, requiredFramTypeHeader, noRequiredFramTypeHeader, neModelHeader);
			boolean isOk = checkHeaderInHeaderRow(rowsIte.next()); // first row
			if(!isOk) {
				log.error("Error in the following serie 3900 Ru Mapper File headers: " + rackTypeHeader + ", " + frameTypeHeader + ", " + neTypeHeader + ", " + neModelHeader + " !!");
				return;
			}
			
			StringBuffer strBuffer = new StringBuffer();
			
			while(rowsIte.hasNext()) {
				Row row = rowsIte.next();
				
				Cell rackTypeCell = row.getCell(headerIndexOf(rackTypeHeader));
				Cell frameTypeCell = row.getCell(headerIndexOf(frameTypeHeader));
				Cell neTypeCell = row.getCell(headerIndexOf(neTypeHeader));
				Cell requiredRackTypeCell = row.getCell(headerIndexOf(requiredRackTypeHeader));
				Cell noRequiredRackTypeCell = row.getCell(headerIndexOf(noRequiredRackTypeHeader));
				//Cell requiredFramTypeCell = row.getCell(headerIndexOf(requiredFramTypeHeader));
				Cell noRequiredFramTypeCell = row.getCell(headerIndexOf(noRequiredFramTypeHeader));
				Cell neModelCell = row.getCell(headerIndexOf(neModelHeader));
				
				String rackTypeVal = getCellValue(rackTypeCell).trim();
				String frameTypeVal = getCellValue(frameTypeCell).trim();				
				String neTypeVal = getCellValue(neTypeCell).trim();
				String requiredRackTypeVal = getCellValue(requiredRackTypeCell).trim();
				String noRequiredRackTypeVal = getCellValue(noRequiredRackTypeCell).trim();				
				//String requiredFramTypeVal = getCellValue(requiredFramTypeCell).trim();				
				String noRequiredFramTypeVal = getCellValue(noRequiredFramTypeCell).toUpperCase().trim();
				String neModelVal = getCellValue(neModelCell).trim();
				
				if(rackTypeVal.isEmpty() && !frameTypeVal.isEmpty() && !neTypeVal.isEmpty() && !neModelVal.isEmpty())
				{
					strBuffer.append(frameTypeVal);
					strBuffer.append("|");
					strBuffer.append(neTypeVal);

					mapWithoutRackType.put(strBuffer.toString().toUpperCase() , neModelVal);					
				}
				else if(!noRequiredRackTypeVal.isEmpty() && !noRequiredFramTypeVal.isEmpty() && !rackTypeVal.isEmpty() && !neTypeVal.isEmpty() && !neModelVal.isEmpty())
				{
					strBuffer.append(rackTypeVal);
					strBuffer.append("|");
					strBuffer.append(neTypeVal);
					strBuffer.append("|");
					strBuffer.append(noRequiredRackTypeVal);
					strBuffer.append("|");
					strBuffer.append(noRequiredFramTypeVal);

					mapWithNoRequiredRackTypeAndNoRequiredFrameType.put(strBuffer.toString().toUpperCase() , neModelVal);					
				}
				else if(!requiredRackTypeVal.isEmpty() && !rackTypeVal.isEmpty() && !neTypeVal.isEmpty() && !neModelVal.isEmpty())
				{
					strBuffer.append(rackTypeVal);
					strBuffer.append("|");
					strBuffer.append(neTypeVal);
					strBuffer.append("|");
					strBuffer.append(requiredRackTypeVal);

					mapWithRequiredRackType.put(strBuffer.toString().toUpperCase() , neModelVal);
				}
				else if(!frameTypeVal.isEmpty() && !rackTypeVal.isEmpty() && !neTypeVal.isEmpty() && !neModelVal.isEmpty())
				{
					strBuffer.append(rackTypeVal);
					strBuffer.append("|");
					strBuffer.append(frameTypeVal);
					strBuffer.append("|");
					strBuffer.append(neTypeVal);
					
					mapWithFrameType.put(strBuffer.toString().toUpperCase() , neModelVal);
				}
				else if(!rackTypeVal.isEmpty() && !neTypeVal.isEmpty() && !neModelVal.isEmpty())
				{
					strBuffer.append(rackTypeVal);
					strBuffer.append("|");
					strBuffer.append(neTypeVal);
					
					mapWithoutFrameType.put(strBuffer.toString().toUpperCase() , neModelVal);
				}
				
				strBuffer.setLength(0); // clear buffer for next ite
			}
			
			strBuffer = null;
			log.info("Finish Reading the HW_MatchingFile sheet neModel_byRackTypeRule with list size: " + mapWithFrameType.size() + mapWithoutFrameType.size());
		} catch (Exception e) {
			log.error("Error when reading HW_MatchingFile sheet neModel_byRackTypeRule !!", e);
		} finally {
			try {
    			if(inputStream != null)
    				inputStream.close(); // close file
			} catch (Exception e) {
				log.error("Error closing file !!", e);
			}
		}
	}
	
	/**
	 * 
	 * @param rackTypeList
	 * @param frameTypeList
	 * @param neType
	 * @return
	 */
	public String getNeModel(List<String> rackTypeList, List<String> frameTypeList, String neType) {
		
		if(rackTypeList == null || frameTypeList ==null || neType == null)
			return null;

		if(rackTypeList.contains("BTS3900") || rackTypeList.contains("BTS5900") || rackTypeList.contains("BTS59005G") || rackTypeList.contains("PICOBTS3900"))
		{
			for(String key : mapWithoutFrameType.keySet())
			{
				String rackTypeFromMap = key.split("\\|")[0];
				String neTypeFromMap = key.split("\\|")[1];
				
				if(rackTypeList.contains(rackTypeFromMap) && neType.equalsIgnoreCase(neTypeFromMap) && (
						rackTypeFromMap.equalsIgnoreCase("BTS3900") || rackTypeFromMap.equalsIgnoreCase("BTS5900") || rackTypeFromMap.equalsIgnoreCase("BTS59005G")))
					return mapWithoutFrameType.get(key);
			}
		}
		for(String key : mapWithRequiredRackType.keySet())
		{
			String rackTypeFromMap = key.split("\\|")[0];
			String neTypeFromMap = key.split("\\|")[1];
			String requiredRackTypeFromMap = key.split("\\|")[2];
			
			if(rackTypeList.contains(rackTypeFromMap) && neType.equalsIgnoreCase(neTypeFromMap) && frameTypeList.contains(requiredRackTypeFromMap))
				return mapWithRequiredRackType.get(key);
		}	
		for(String key : mapWithoutRackType.keySet())
		{
			String frameTypeFromMap = key.split("\\|")[0];
			String neTypeFromMap = key.split("\\|")[1];
			
			if(frameTypeList.contains(frameTypeFromMap) && neType.equalsIgnoreCase(neTypeFromMap))
				return mapWithoutRackType.get(key);
		}
		
		for(String key : mapWithFrameType.keySet())
		{
			String rackTypeFromMap = key.split("\\|")[0];
			String frameTypeFromMap = key.split("\\|")[1];
			String neTypeFromMap = key.split("\\|")[2];
			
			if(rackTypeList.contains(rackTypeFromMap) && frameTypeList.contains(frameTypeFromMap) && neType.equalsIgnoreCase(neTypeFromMap))
				return mapWithFrameType.get(key);
		}
		for(String key : mapWithoutFrameType.keySet())
		{
			String rackTypeFromMap = key.split("\\|")[0];
			String neTypeFromMap = key.split("\\|")[1];
			
			if(rackTypeList.contains(rackTypeFromMap) && neType.equalsIgnoreCase(neTypeFromMap))
				return mapWithoutFrameType.get(key);
		}
		for(String key : mapWithNoRequiredRackTypeAndNoRequiredFrameType.keySet())
		{
			String rackTypeFromMap = key.split("\\|")[0];
			String neTypeFromMap = key.split("\\|")[1];
			String noRequiredRackTypeFromMap = key.split("\\|")[2];
			
			String noRequiredFrameTypeFromMap = key.split("\\|")[3];
			boolean check = false;
			int count = 0;
			for(String s : noRequiredFrameTypeFromMap.split(","))
			{
				if(rackTypeList.contains(rackTypeFromMap) && neType.equalsIgnoreCase(neTypeFromMap) && !rackTypeList.contains(noRequiredRackTypeFromMap)
						&& !frameTypeList.contains(s))
				{
					if(check == false && count > 0)
						check = false;
					else
						check = true;
				}
				count++;
			}
			
			if(check)
				return mapWithNoRequiredRackTypeAndNoRequiredFrameType.get(key);
		}
		return null;
	}
	
	/**  
	  * **************
	  * Setters
	  * **************
	*/
	
	public void setRackTypeHeader(String rackNameHeader) {
		this.rackTypeHeader = rackNameHeader;
	}

	public void setFrameTypeHeader(String frameNameHeader) {
		this.frameTypeHeader = frameNameHeader;
	}

	public void setNeTypeHeader(String frameIndexHeader) {
		this.neTypeHeader = frameIndexHeader;
	}

	public void setNeModelHeader(String ruIndexHeader) {
		this.neModelHeader = ruIndexHeader;
	}
	
	public void setRequiredRackTypeHeader(String requiredRackTypeHeader) {
		this.requiredRackTypeHeader = requiredRackTypeHeader;
	}

	public void setNoRequiredRackTypeHeader(String noRequiredRackTypeHeader) {
		this.noRequiredRackTypeHeader = noRequiredRackTypeHeader;
	}

	public void setRequiredFramTypeHeader(String requiredFramTypeHeader) {
		this.requiredFramTypeHeader = requiredFramTypeHeader;
	}

	public void setNoRequiredFramTypeHeader(String noRequiredFramTypeHeader) {
		this.noRequiredFramTypeHeader = noRequiredFramTypeHeader;
	}
	
	/**
	 * Main
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {		
        ApplicationContext classw = new ClassPathXmlApplicationContext("manufactures/huawei/m2000v2r0en0sp2ran/huawei-m2000v2r0en0sp2ran-spring-config.xml");
        ModelByRackTypeRuleParser modelByRackTypeRule = (ModelByRackTypeRuleParser) classw.getBean(ModelByRackTypeRuleParser.class);
        
        List<String> rackList = new ArrayList<String>();
        rackList.add("PS4890".toUpperCase());
        rackList.add("RFC".toUpperCase());
        rackList.add("BTS3900".toUpperCase());
        
        List<String> frameList = new ArrayList<String>();
        frameList.add("BTS3900E".toUpperCase());
		System.out.println(modelByRackTypeRule.getNeModel(rackList, frameList, "GSMBTS"));
	}

}
