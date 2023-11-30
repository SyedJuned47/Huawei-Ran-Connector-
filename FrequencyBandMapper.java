package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.mobinets.nep.npt.service.versionscomparison.NetworkElement;
import com.mobinets.nps.customer.transmission.common.CommonConfig;
import com.mobinets.nps.daemon.csv.AbstractFileCsvParser;

public class FrequencyBandMapper extends AbstractFileCsvParser<NetworkElement> {

	private static final Log log = LogFactory.getLog(FrequencyBandMapper.class);
	
	private CommonConfig config;
	private String bandHeader;
	private String matchedBandHeader;
	
	public void setConfig(CommonConfig config) {
		this.config = config;
	}

	public void setBandHeader(String bandHeader) {
		this.bandHeader = bandHeader;
	}

	public void setMatchedBandHeader(String matchedBandHeader) {
		this.matchedBandHeader = matchedBandHeader;
	}

	private Map<String, String> freqBandMap;
	
	/**
	 * 
	 */
	void init() {
		
		try
		{
			log.info("Begin Reading the Frequency Band aurfcn Mapper file ...");
			
			freqBandMap = new HashMap<String, String>();
			
			String pathfile = config.getProperty("huawei.m2v2r0.ran.uarfcn.freqBand.file");
			
			if(null == pathfile)
			{
				log.error("Missing attribute (huawei.m2v2r0.ran.uarfcn.freqBand.file) in manufacture-config.xml.");
				return;
			}
			
			File file = new File(pathfile);
			if(!file.exists())
			{
				log.error("Folder (" + pathfile + ") not found");
				return;
			}
			
			addHeaderToParse(bandHeader);
			addHeaderToParse(matchedBandHeader);
			
			ICsvListReader inFile = new CsvListReader(new FileReader(file), CsvPreference.EXCEL_PREFERENCE);
			
			String[] header = null;
			header = inFile.getCSVHeader(true);
			
			boolean isOK = fillHeaderIndex(header);
			
			if(!isOK)
			{
				log.error("Error with parsing header of file " + file.getPath());
				return;
			}
			
			String band = null;
			String matchedBand = null;
			
			List<String> row = new ArrayList<String>();
			while ((row = inFile.read()) != null)
			{
				band = row.get(headerIndexOf(bandHeader)).trim();
				matchedBand = row.get(headerIndexOf(matchedBandHeader)).trim();
				
				if(band == null || band.isEmpty() || matchedBand == null || matchedBand.isEmpty())
					continue;
				
				freqBandMap.put(band, matchedBand);
			}
		}
		catch(Exception e)
		{
			log.error("Error", e);
		}
	}
	
	/**
	 * 
	 * @param band
	 * @return
	 */
	public String getMatchedFreqBand(String band) {
		if(band == null)
			return null;
		
		return freqBandMap.get(band);
	}
}
