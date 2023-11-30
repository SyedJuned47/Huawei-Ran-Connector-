package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mobinets.nps.customer.transmission.common.CommonConfig;

public class LicenseNodeMatcher{
	
	private static final Log log = LogFactory.getLog(LicenseNodeMatcher.class);
	private CommonConfig config;
	
	private Map<String, String> map;
	
	public void setConfig(CommonConfig config) {
		this.config = config;
	}
	
	public void init() {
		
		map = new HashMap<String, String>();
		
		log.debug("Begin Parsing of License Node Matching file.");
		
		try
		{
			String pathFile = config.getProperty("huawei.m2v2r0.ran.static.file.license.node.matcher");
			if(null == pathFile)
			{
				log.error("Missing Attribute (huawei.m2v2r0.ran.static.file.license.node.matcherr) in context file.");
				return;
			}
			
			File xlsFile = new File(pathFile);
			if(!xlsFile.exists())
			{
				log.error("File (" + pathFile + ") not found");
				return;
			}
			
			Workbook workbook = null;

			if (xlsFile.getAbsolutePath().endsWith(".xls")) {
				workbook = new HSSFWorkbook(new FileInputStream(xlsFile));
			} else if (xlsFile.getAbsolutePath().endsWith(".xlsx")) {
				workbook = new XSSFWorkbook(new FileInputStream(xlsFile));
			}
			
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
			
				Cell cell0 = row.getCell(0);
				if(cell0 != null)
					cell0.setCellType(Cell.CELL_TYPE_STRING);
				Cell cell1 = row.getCell(1);
				if(cell1 != null)
					cell1.setCellType(Cell.CELL_TYPE_STRING);
				
				String logical = cell0 != null ? cell0.getStringCellValue().trim() : null;
				String physical = cell1 != null ? cell1.getStringCellValue().trim() : null;
				if(logical == null || physical == null)
					continue;

				map.put(logical, physical);
			}
		}
		catch(Exception se)
		{
			log.error(se);
		}
		log.info("End of Parsing of License Node Matching file.");
	}
	
	public String getMatch(String logical){
		if(map.containsKey(logical))
			return map.get(logical);
		return logical;
	}
	
}
