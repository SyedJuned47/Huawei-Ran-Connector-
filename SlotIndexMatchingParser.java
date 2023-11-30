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

public class SlotIndexMatchingParser {
	
	private static final Log log = LogFactory.getLog(SlotIndexMatchingParser.class);
	
	private Map<String, String> slotIndexMatchedMap;
	private Map<String, String> slotIndexMatchedMapWithRackType;
	
	private static final String SEP = System.getProperty("file.separator");
	
	/**
	 * @return
	 */
	public void init() {
		log.debug("Starting Parsing slotIndex Matching File ...");
		
		InputStream inputStream = null;
		try
		{			
			if(null == slotIndexMatchedMap)
				slotIndexMatchedMap = new HashMap<String, String>();

			if(null == slotIndexMatchedMapWithRackType)
				slotIndexMatchedMapWithRackType = new HashMap<String, String>();
			
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("manufactures" + SEP
					 + "huawei" + SEP + "m2000v2r0en0sp2ran" + SEP + "HW_MatchingFile.xlsx");
			
			if(null == inputStream) {
				log.error("Can't find file \"HW_MatchingFile.xlsx\" !!");
				return;
			}
			
			Workbook workbook = WorkbookFactory.create(inputStream);
			
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIter = sheet.rowIterator();
			rowIter.next(); // skip first row
			
			while ((rowIter.hasNext()))
			{
				Row row = rowIter.next();
				
				String rackTypeStr = TransmissionCommon.getCellStringValue(row.getCell(0)).trim();
				String boardTypeStr = TransmissionCommon.getCellStringValue(row.getCell(1)).trim();
				String slotNoStr = TransmissionCommon.getCellStringValue(row.getCell(2)).trim();
				String matchedSlotIndex = TransmissionCommon.getCellStringValue(row.getCell(3)).trim();
				
				rackTypeStr = getIntValue(rackTypeStr);
				boardTypeStr = getIntValue(boardTypeStr);
				slotNoStr = getIntValue(slotNoStr);
				matchedSlotIndex = getIntValue(matchedSlotIndex);
				
				if(rackTypeStr == null || rackTypeStr.isEmpty())
					slotIndexMatchedMap.put(boardTypeStr.toUpperCase() + "_" + slotNoStr, matchedSlotIndex);
				else
					slotIndexMatchedMapWithRackType.put(rackTypeStr.toUpperCase() + "_" + boardTypeStr.toUpperCase() + "_" + slotNoStr, matchedSlotIndex);
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
		log.debug("Finish Parsing HW_MatchingFile sheet slotIndexMatching !");
	}
	
	public String getIntValue(String value) {
		String res = value;
		if(TransmissionCommon.isDouble(value))
			res = Double.valueOf(value).intValue() + "";
		
		return res;
	}
	
	/**
	 * @param neName
	 * @return
	 */
	public String getMatchedSlotIndex(String key) {
		
		if(null == key)
			return null;
		
		return slotIndexMatchedMap.get(key.toUpperCase());
	}
	
	/**
	 * @param neName
	 * @return
	 */
	public String getMatchedSlotIndexWithRackType(String key) {
		
		if(null == key)
			return null;
		
		return slotIndexMatchedMapWithRackType.get(key.toUpperCase());
	}
}
