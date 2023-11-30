package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobinets.nps.customer.transmission.common.CommonConfig;
import com.mobinets.nps.customer.transmission.common.utilties.ExcelHandler;

public class Node3GMapper implements ExcelHandler{

	private static final Log log = LogFactory.getLog(Node3GMapper.class);
	private CommonConfig m2v2r0Config;
	
	public void setM2v2r0Config(CommonConfig m2v2r0Config) {
		this.m2v2r0Config = m2v2r0Config;
	}
	
	private Map<String, String> threegMap;
	private Map<String, String> reversedThreegMap;
	
	public void init(){
		
		log.debug("Begin Parsing file for  node3G Mapper...");
		threegMap = new HashMap<>();
		reversedThreegMap = new HashMap<>();
		
		try {
			String pathFile = m2v2r0Config.getProperty("huawei.m2v2r0.ran.static.file.3g.mapper");

			if (null == pathFile) {
				log.error("Missing Attribute (huawei.m2v2r0.ran.static.file.3g.mapper) in context file.");
				return;
			}

			File file = new File(pathFile);
			if (!file.exists()) {
				log.error("File " + file.getPath() + " not found");
				return;
			}
			
			List<Map<String, String>> ranContent = getContentWhthoutHeaders(file, "RAN");
			for(Map<String, String> map : ranContent ){
				String siteName3g = map.get("3G Site Name");
				String oldSiteName = map.get("Old Site Name");
				threegMap.put(siteName3g, oldSiteName);
				reversedThreegMap.put(oldSiteName, siteName3g);
			}
			

		} catch (Exception e) {
			log.error("Error : ", e);
		}
		log.debug("Finish Parsing file for  node3G Mapper !");
	}
	
	public Map<String, String> getThreegMap() {
		return threegMap;
	}
	
	public Map<String, String> getReversedThreegMap() {
		return reversedThreegMap;
	}
}
