package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers;

import java.io.File;
import java.io.IOException;
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

import com.mobinets.nps.customer.transmission.common.CommonConfig;
import com.mobinets.nps.customer.transmission.common.TransmissionCommon;

public class ParentMscSgsnMatcher {
	
	private static final Log log = LogFactory.getLog(ParentMscSgsnMatcher.class);
	private CommonConfig neMatchingParserConfig;
	
	private Map<String, String> parentMscsSgsnsByAlias;
	
	public void setNeMatchingParserConfig(CommonConfig neMatchingParserConfig) {
		this.neMatchingParserConfig = neMatchingParserConfig;
	}
	
	public String getParentMscByAlias(String mscAlias) {
		if(mscAlias == null)
			return null;
		if(parentMscsSgsnsByAlias == null)
			parseParentMscSgsnMatching();
		return parentMscsSgsnsByAlias.get("MSC" + TransmissionCommon.SEPERATOR + mscAlias.toUpperCase());
	}
	
	public String getParentSgsnByAlias(String sgsnAlias) {
		if(sgsnAlias == null)
			return null;
		if(parentMscsSgsnsByAlias == null)
			parseParentMscSgsnMatching();
		return parentMscsSgsnsByAlias.get("SGSN" + TransmissionCommon.SEPERATOR + sgsnAlias.toUpperCase());
	}
	
	private void parseParentMscSgsnMatching() {
		parentMscsSgsnsByAlias = new HashMap<String, String>();
		
		log.debug("Begin Parsing of MSC and SGSN Matching files.");
		String methodName = "parseMprFiles";
		long begTime = System.currentTimeMillis();
		log.info("Start calling " + methodName);
		
		try
		{
			String pathFile = neMatchingParserConfig.getProperty("huawei.m2v2r0.ran.logical.parentMscSgsnMatching");
			if(null == pathFile)
			{
				log.error("Missing Attribute (huawei.m2v2r0.ran.logical.parentMscSgsnMatching) in manufacture-config.xml file.");
				return;
			}
			
			File xlsFile = new File(pathFile);
			if(!xlsFile.exists())
			{
				log.error("Folder (" + pathFile + ") not found");
				return;
			}
			Workbook workBook = WorkbookFactory.create(xlsFile);
			Sheet sheet = workBook.getSheetAt(0);
			Iterator<Row> rowIter = sheet.rowIterator();
			while ((rowIter.hasNext()))
			{
				try
				{
					Row row = rowIter.next();
					
					Cell cellA = row.getCell(0);
					Cell cellB = row.getCell(1);
					
					String mscSgsnAlias = TransmissionCommon.getCellStringValue(cellA).split("\\.")[0];
					String parentMscSgsn = TransmissionCommon.getCellStringValue(cellB).split("\\.")[0];
					
					if(mscSgsnAlias.isEmpty() || parentMscSgsn.isEmpty())
					{
						continue;
					}
					parentMscsSgsnsByAlias.put("MSC" + TransmissionCommon.SEPERATOR + mscSgsnAlias.toUpperCase(), parentMscSgsn);
				}
				catch(Exception e)
				{
					log.error(e);
				}
			}
			sheet = workBook.getSheetAt(1);
			rowIter = sheet.rowIterator();
			while ((rowIter.hasNext()))
			{
				try
				{
					Row row = rowIter.next();
					
					Cell cellA = row.getCell(0);
					Cell cellB = row.getCell(1);
					
					String mscSgsnAlias = TransmissionCommon.getCellStringValue(cellA).split("\\.")[0];
					String parentMscSgsn = TransmissionCommon.getCellStringValue(cellB).split("\\.")[0];
					
					if(mscSgsnAlias.isEmpty() || parentMscSgsn.isEmpty())
					{
						continue;
					}
					parentMscsSgsnsByAlias.put("SGSN" + TransmissionCommon.SEPERATOR + mscSgsnAlias.toUpperCase(), parentMscSgsn);
				}
				catch(Exception e)
				{
					log.error(e);
				}
			}
		}
		catch(Exception se)
		{
			log.error(se);
		}
		log.info("End of calling " + methodName + " executionTime=\"" + (System.currentTimeMillis() - begTime) + "\" milliseconds ");
	}
	
	public static void main(String[] args) throws IOException {
		
	}
}
