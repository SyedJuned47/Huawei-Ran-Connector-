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

public class ModelMatchingParser {
	
	private static final Log log = LogFactory.getLog(ModelMatchingParser.class);
		
	private Map<String, String> shelfTypeMatchedMap;
	private Map<String, String> boardTypeMatchedMap;
	
	private static final String SEP = System.getProperty("file.separator");
	
	/**
	 * @return
	 */
	public void init() {
		log.debug("Starting Parsing model Matching File ...");
		
		InputStream inputStream = null;
		try
		{	
			
			if(null == shelfTypeMatchedMap)
				shelfTypeMatchedMap = new HashMap<String, String>();
			
			if(null == boardTypeMatchedMap)
				boardTypeMatchedMap = new HashMap<String, String>();
			
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("manufactures" + SEP
					 + "huawei" + SEP + "m2000v2r0en0sp2ran" + SEP + "HW_MatchingFile.xlsx");
			
			if(null == inputStream) {
				log.error("Can't find file \"HW_MatchingFile.xlsx\" !!");
				return;
			}
			
			Workbook workbook = WorkbookFactory.create(inputStream);
			
			Sheet sheet = workbook.getSheetAt(2);
			Iterator<Row> rowIter = sheet.rowIterator();
			rowIter.next(); // skip first row
			
			while ((rowIter.hasNext()))
			{
				Row row = rowIter.next();
				
				String rackTypeStr = TransmissionCommon.getCellStringValue(row.getCell(0)).trim();
				String frameTypeStr = TransmissionCommon.getCellStringValue(row.getCell(1)).trim();
				String boardTypeStr = TransmissionCommon.getCellStringValue(row.getCell(2)).trim();
				
				String matchedFrameTypeStr = TransmissionCommon.getCellStringValue(row.getCell(4)).trim();
				String matchedBoardTypeStr = TransmissionCommon.getCellStringValue(row.getCell(5)).trim();
				
				shelfTypeMatchedMap.put(rackTypeStr + "_" + frameTypeStr, matchedFrameTypeStr);
				boardTypeMatchedMap.put(rackTypeStr + "_" + boardTypeStr, matchedBoardTypeStr);
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
		
		log.debug("Finish Parsing HW_MatchingFile sheet shelfBoardModelMatching !");
	}
	
	/**
	 * @param neName
	 * @return
	 */
	public String getShelfMatchedModel(String key) {
		
		if(null == key)
			return null;
		
		return shelfTypeMatchedMap.get(key);
	}
	
	/**
	 * @param neName
	 * @return
	 */
	public String getBoardMatchedModel(String key) {
		
		if(null == key)
			return null;
		
		return boardTypeMatchedMap.get(key);
	}
}
