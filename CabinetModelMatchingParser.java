package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.mobinets.nps.customer.transmission.common.TransmissionCommon;

public class CabinetModelMatchingParser {
	
	private static final Log log = LogFactory.getLog(CabinetModelMatchingParser.class);
		
	private Map<String, String> cabinetModelMatchedWithRackTypeMap;
	private Map<String, String> cabinetModelMatchedWithoutRackTypeMap;
	
	private static final String SEP = System.getProperty("file.separator");
	
	/**
	 * @return
	 */
	public void init() {
		log.debug("Starting Parsing cabinet model Matching File ...");
		
		InputStream inputStream = null;
		try
		{			
			if(null == cabinetModelMatchedWithRackTypeMap)
				cabinetModelMatchedWithRackTypeMap = new HashMap<String, String>();
			
			if(null == cabinetModelMatchedWithoutRackTypeMap)
				cabinetModelMatchedWithoutRackTypeMap = new HashMap<String, String>();
			
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("manufactures" + SEP
					 + "huawei" + SEP + "m2000v2r0en0sp2ran" + SEP + "HW_MatchingFile.xlsx");
			
			if(null == inputStream) {
				log.error("Can't find file \"HW_MatchingFile.xlsx\" !!");
				return;
			}
			
			Workbook workbook = WorkbookFactory.create(inputStream);
			
			Sheet sheet = workbook.getSheetAt(1);
			Iterator<Row> rowIter = sheet.rowIterator();
			rowIter.next(); // skip first row
			
			while ((rowIter.hasNext()))
			{
				Row row = rowIter.next();
				
				String neModel = TransmissionCommon.getCellStringValue(row.getCell(0)).trim();
				String rackType = TransmissionCommon.getCellStringValue(row.getCell(1)).trim();
				
				String matchedRackTypeStr = TransmissionCommon.getCellStringValue(row.getCell(2)).trim();
				
				if(rackType == null || rackType.isEmpty())
					cabinetModelMatchedWithoutRackTypeMap.put(neModel, matchedRackTypeStr);
				else
					cabinetModelMatchedWithRackTypeMap.put(neModel + "_" + rackType, matchedRackTypeStr);
			}
		}
		catch(Exception e)
		{
			log.error("Error Exception", e);
		}
		finally
		{
			try
			{
    			if(inputStream != null)
    				inputStream.close(); // close file
			} catch (Exception e)
			{
				log.error("Error closing file !!", e);
			}
		}
		
		log.debug("Finish Parsing HW_MatchingFile sheet cabinetModelMatching !");
	}
	
	/**
	 * @param neName
	 * @return
	 */
	public String getCabinetMatchedModelWithoutRackType(String neModel) {
		
		if(neModel == null)
			return null;
		
		return cabinetModelMatchedWithoutRackTypeMap.get(neModel);
	}
	
	/**
	 * @param neName
	 * @return
	 */
	public String getCabinetMatchedModelWithRackType(String neModel, String rackType) {
				
		return cabinetModelMatchedWithRackTypeMap.get(neModel + "_" + rackType);
	}
}
