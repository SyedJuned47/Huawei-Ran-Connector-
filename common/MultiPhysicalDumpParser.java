package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.common;

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
import com.mobinets.nps.customer.transmission.externalC.zainkw.common.ZainKwSiteNameMatcher;

public class MultiPhysicalDumpParser {

	private static final Log log = LogFactory.getLog(ZainKwSiteNameMatcher.class);

	private Map<String, String> dumpsMap;

	private CommonConfig zainKwConfig;

	public void setZainKwConfig(CommonConfig zainKwConfig) {
		this.zainKwConfig = zainKwConfig;
	}

	public CommonConfig getZainKwConfig() {
		return zainKwConfig;
	}

	public void init() {

		log.debug("Get Map Of Site Name Matching ...");
		try {

			if (null == dumpsMap)
				dumpsMap = new HashMap<String, String>();

			String pathFolder = zainKwConfig.getProperty("static.file.multiDump");

			if (null == pathFolder) {
				log.error("Missing Attribute (static.file.multiDump) in config.properties file.");
				return;
			}

			File xlsSrcfile = new File(pathFolder);
			if (!xlsSrcfile.exists()) {
				log.error("File " + xlsSrcfile.getName() + " not found");
				return;
			}

			FileInputStream fileInput = new FileInputStream(xlsSrcfile);
			Workbook workbook = null;
			if (xlsSrcfile.getName().toLowerCase().endsWith(".xls")) {
				workbook = new HSSFWorkbook(fileInput);
			} else if (xlsSrcfile.getName().toLowerCase().endsWith(".xlsx")) {
				workbook = new XSSFWorkbook(fileInput);
			}

			Sheet sheet = workbook.getSheetAt(0);

			Iterator<Row> rowIterator = sheet.iterator();
			int i = 0;
			while (rowIterator.hasNext()) {

				Row row = rowIterator.next();
				if (i == 0) {
					i++;
					continue;
				}

				Cell cellA = row.getCell(0);
				Cell cellB = row.getCell(1);
				cellA.setCellType(Cell.CELL_TYPE_STRING);
				cellB.setCellType(Cell.CELL_TYPE_STRING);

				String name = cellA.getStringCellValue().trim();
				String matchedSite = cellB.getStringCellValue().trim();

				i++;

				if (null == name || null == matchedSite)
					continue;

				name = name.trim();
				matchedSite = matchedSite.trim();

				dumpsMap.put(matchedSite, name);

			}
		} catch (Exception e) {
			log.error("Error : ", e);
		}
		log.debug("Finish Getting Map Of correspandant dumps with size : " + dumpsMap.size());

	}

	public String getCorrespandantDump(String dump) {
		if (dumpsMap == null)
			init();
		if (null == dump)
			return null;

		String newSite = dumpsMap.get(dump);
		if (newSite != null)
			return newSite;

		return dump;
	}

}
