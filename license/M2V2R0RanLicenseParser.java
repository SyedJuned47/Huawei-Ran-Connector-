package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobinets.nps.customer.transmission.common.CommonConfig;
import com.mobinets.nps.customer.transmission.common.TransmissionCommon;
import com.mobinets.nps.customer.transmission.manufacture.common.ConnectorUtility;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.M2V2R0RanPhysicalParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers.LicenseNodeMatcher;
import com.mobinets.nps.model.UserTypes.PossibleNodeType;
import com.mobinets.nps.model.customer.data.element.ElementBs;
import com.mobinets.nps.model.customer.data.element.ElementBsc;
import com.mobinets.nps.model.customer.data.element.ElementCoreElement;
import com.mobinets.nps.model.customer.data.element.ElementENodeB;
import com.mobinets.nps.model.customer.data.element.ElementHss;
import com.mobinets.nps.model.customer.data.element.ElementLicense;
import com.mobinets.nps.model.customer.data.element.ElementMgw;
import com.mobinets.nps.model.customer.data.element.ElementMsc;
import com.mobinets.nps.model.customer.data.element.ElementMss;
import com.mobinets.nps.model.customer.data.element.ElementNb;
import com.mobinets.nps.model.customer.data.element.ElementNode;
import com.mobinets.nps.model.customer.data.element.ElementRanBs;
import com.mobinets.nps.model.customer.data.element.ElementRnc;

public class M2V2R0RanLicenseParser {
	
	private static final Log log = LogFactory.getLog(M2V2R0RanLicenseParser.class);
	
	private List<ElementLicense> licenses;
	private Map<String, ElementLicense> licensesMap;

	private CommonConfig m2v2r0Config;
	private LicenseNodeMatcher licenseNodeMatcher;
	private M2V2R0RanPhysicalParser m2v2r0RanPhysicalParser;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private Map<String, ElementBsc> bscsByName;
	private Map<String, ElementRnc> rncsByName;
	private Map<String, ElementRanBs> ranBssByName;
	private Map<String, ElementBs> btssByName;
	private Map<String, ElementNb> nodeBsByName;
	private Map<String, ElementENodeB> enodeBsByName;
	private Map<String, ElementMss> msssByName;
	private Map<String, ElementMgw> mgwsByName;
	private Map<String, ElementHss> hsssByName;
	private Map<String, ElementMsc> mscsByName;
	private Map<String, ElementCoreElement> coreElementsByName;
	
	public void setM2v2r0Config(CommonConfig m2v2r0Config) {
		this.m2v2r0Config = m2v2r0Config;
	}
	
	public void setLicenseNodeMatcher(LicenseNodeMatcher licenseNodeMatcher) {
		this.licenseNodeMatcher = licenseNodeMatcher;
	}
	
	public void setM2v2r0RanPhysicalParser(M2V2R0RanPhysicalParser m2v2r0RanPhysicalParser) {
		this.m2v2r0RanPhysicalParser = m2v2r0RanPhysicalParser;
	}
	
	public List<ElementLicense> getLicenses() {
		if(licenses == null)
			parseFiles();
		return licenses;
	}
	
	
	private void parseFiles() {
		init();
		
		String pathFolder = m2v2r0Config.getProperty("huawei.m2v2r0.license.dumps");
		
		if(null == pathFolder){
			log.error("Missing Attribute (huawei.m2v2r0.license.dumps) in properties file.");
			return;
		}
		
		File folderDumps = new File(pathFolder);
		if(!folderDumps.exists()){
			log.error("Folder (" + pathFolder + ") not found");
			return;
		}
		
		if(!folderDumps.isDirectory()) {
			log.error("File (" + pathFolder + ") should be a directory");
			return;
		}
		
		List<File> files = new ArrayList<>();
		ConnectorUtility.listf(pathFolder, files);
		
		for(File file : files) {
			try {
				log.debug("Start parsing file : " + file.getAbsolutePath());
				parseRanFile(file);
				parseCoreFile(file);
				parseUgwFile(file);
				parseMgwFile(file);
			} catch (Exception e) {
				log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
			}
		}
		
		licenses.addAll(licensesMap.values());
		
	}

	private void parseMgwFile(File file) {
		
		List<LicenseData> licensedatalist = new ArrayList<>();
		String commentInformation = null;
		String deadlineBasicLicense = null;
		
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
			
			boolean isDpsLicense = false;
			boolean isLstLicense = false;
			boolean isBasicinformation = false;
			boolean isAuthorizationinformationBasicFeatureLicense = false;
			boolean isDynamicResourceInfo = false;
			boolean isStaticResourceInfo = false;
			
			boolean headerfound = false;
			Map<String, Entry<Integer, Integer>> headerIndexes = new HashMap<>();
			
			
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				try {
					
					if(StringUtils.isBlank(line))
						continue;
					
					line = line.replaceAll("\\p{C}", "");
					
					if(isLstLicense) {
						if(StringUtils.containsIgnoreCase(line, "Comment Information")) {
							commentInformation = StringUtils.substringAfter(line, "=").trim();
							isLstLicense = false;
						}
					}
					
					if(isDpsLicense) {
						
						if(StringUtils.containsIgnoreCase(line, "---------------") || StringUtils.containsIgnoreCase(line, "Number of results")) {
							headerfound = false;
							headerIndexes.clear();
							continue;
						}
						
						if(isBasicinformation) {
							if(StringUtils.contains(line, " = ")) {
								
								LicenseData licensedata = new LicenseData();
								String part1 = StringUtils.substringBefore(line, "=").trim();
								
								String unit = null;
								String details = part1;
								
								if(StringUtils.contains(part1, "(")) {
									String[] values = StringUtils.substringsBetween(part1, "(", ")");
									try { unit = values[values.length - 1]; } catch (Exception e) {}
									details = StringUtils.substringBeforeLast(part1, "(");
								}
								
								licensedata.detailsvalue = StringUtils.substringAfter(line, "=").trim();
								licensedata.unit = unit;
								licensedata.details = details;
								
								if(StringUtils.equalsIgnoreCase(details, "Deadline in Basic License"))
									deadlineBasicLicense = licensedata.detailsvalue;
								
								licensedatalist.add(licensedata);
							}
						}
						
						if(isAuthorizationinformationBasicFeatureLicense) {
							
							if(headerfound) {
								line = StringUtils.stripStart(line, null);
								LicenseData licensedata = new LicenseData();
								
								licensedata.detailsvalue = getColumnValue("Authorized", line, headerIndexes);
								licensedata.unit = getColumnValue("Dimension", line, headerIndexes);
								licensedata.details = getColumnValue("Resource name", line, headerIndexes);
								licensedata.identifier = getColumnValue("Resource name", line, headerIndexes);
								licensedata.details = StringUtils.substringBeforeLast(licensedata.details, "(");
								
								licensedatalist.add(licensedata);
								
							}
							
							if(StringUtils.containsIgnoreCase(line, "Resource name") && StringUtils.containsIgnoreCase(line, "Authorized")) {
								headerfound = true;
								String separator = "\\s{2,}";
								
								headerIndexes = deduceColumnsIndexesFromLine(StringUtils.stripStart(line, null), separator);
								
							}
						}
						
						if(isDynamicResourceInfo) {
							
							if(headerfound) {
								line = StringUtils.stripStart(line, null);
								LicenseData licensedata = new LicenseData();
								
								licensedata.detailsvalue = getColumnValue("Authorized", line, headerIndexes);
								licensedata.unit = getColumnValue("Dimension", line, headerIndexes);
								licensedata.details = getColumnValue("Resource name", line, headerIndexes);
								licensedata.identifier = getColumnValue("Resource name", line, headerIndexes);
								licensedata.details = StringUtils.substringBeforeLast(licensedata.details, "(");
								licensedata.usage = getColumnValue("Used num", line, headerIndexes);
								
								licensedatalist.add(licensedata);
								
							}
							
							if(StringUtils.containsIgnoreCase(line, "Resource name") && StringUtils.containsIgnoreCase(line, "Authorized")) {
								headerfound = true;
								String separator = "\\s{2,}";
								
								headerIndexes = deduceColumnsIndexesFromLine(StringUtils.stripStart(line, null), separator);
								
							}
						}	
						
						if(isStaticResourceInfo) {
							
							if(headerfound) {
								line = StringUtils.stripStart(line, null);
								LicenseData licensedata = new LicenseData();
								
								licensedata.detailsvalue = getColumnValue("Authorized", line, headerIndexes);
								licensedata.unit = getColumnValue("Dimension", line, headerIndexes);
								licensedata.details = getColumnValue("Resource name", line, headerIndexes);
								licensedata.identifier = getColumnValue("Resource name", line, headerIndexes);
								licensedata.details = StringUtils.substringBeforeLast(licensedata.details, "(");
								licensedata.usage = getColumnValue("Used num", line, headerIndexes);
								
								licensedatalist.add(licensedata);
								
							}
							
							if(StringUtils.containsIgnoreCase(line, "Resource name") && StringUtils.containsIgnoreCase(line, "Authorized")) {
								headerfound = true;
								String separator = "\\s{2,}";
								
								headerIndexes = deduceColumnsIndexesFromLine(StringUtils.stripStart(line, null), separator);
								
							}
						}	
						
						
						if(StringUtils.containsIgnoreCase(line, "Basic information in license")) {
							isBasicinformation = true;
							isAuthorizationinformationBasicFeatureLicense = false;
							isDynamicResourceInfo = false;
							isStaticResourceInfo = false;
						}
						
						if(StringUtils.containsIgnoreCase(line, "Authorization information in basic feature license")) {
							isAuthorizationinformationBasicFeatureLicense = true;
							isBasicinformation = false;
							isDynamicResourceInfo = false;
							isStaticResourceInfo = false;
						}
						
						if(StringUtils.containsIgnoreCase(line, "Dynamic resource info")) {
							isDynamicResourceInfo = true;
							isBasicinformation = false;
							isAuthorizationinformationBasicFeatureLicense = false;
							isStaticResourceInfo = false;
						}
						
						if(StringUtils.containsIgnoreCase(line, "Static resource info")) {
							isStaticResourceInfo = true;
							isDynamicResourceInfo = false;
							isBasicinformation = false;
							isAuthorizationinformationBasicFeatureLicense = false;
						}
					}
					
					if(StringUtils.containsIgnoreCase(line, "LST LICENSE:")) {
						isLstLicense = true;
						isDpsLicense = false;
					}
					
					if(StringUtils.containsIgnoreCase(line, "DSP LICENSE:;")) {
						isDpsLicense = true;
						isLstLicense = false;
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
		}
		
		String matchednodenmae = deduceMgwNodeName(commentInformation);
		
		ElementNode elementNode = deduceNodeFromNodeName(matchednodenmae);
		if(elementNode != null) {
			for(LicenseData licensedata : licensedatalist) {
				
				if(StringUtils.isBlank(licensedata.identifier))
					continue;
				
				ElementLicense elementLicense = new ElementLicense();
				elementLicense.setNodeId(elementNode.getId());
				elementLicense.setNodeObjectId(elementNode.getId());
				elementLicense.setNodeName(elementNode.getName());
				elementLicense.setNodeType(deduceType(elementNode));
				
				elementLicense.setIdentifier(licensedata.identifier);
				// Details
				elementLicense.setDetails(licensedata.details);
				// Allocated
				if(StringUtils.isNotBlank(licensedata.detailsvalue)) {
					if(NumberUtils.isDigits(licensedata.detailsvalue))
						elementLicense.setAllocated(Integer.parseInt(licensedata.detailsvalue));
					else
						elementLicense.setDetailsValue(licensedata.detailsvalue);
				}
				
				// Unit
				elementLicense.setUnit(licensedata.unit);
				try { elementLicense.setUsage(Integer.parseInt(licensedata.usage)); } catch (Exception e) {}
				
				// Expiration Date
				elementLicense.setExpirationDate(deadlineBasicLicense);

				licenses.add(elementLicense);
			}
		}
		
	}

	private String deduceMgwNodeName(String commentInformation) {
		if(StringUtils.containsIgnoreCase(commentInformation, "HQB1"))
			return "HQB_MGW";
		
		else if(StringUtils.containsIgnoreCase(commentInformation, "HQB2"))
			return "HQB_MGW2";
		
		else if(StringUtils.containsIgnoreCase(commentInformation, "SSB1"))
			return "SSB_MGW";
		
		else if(StringUtils.containsIgnoreCase(commentInformation, "SSB2"))
			return "SSB_MGW2";
		return null;
	}

	private void parseUgwFile(File file) {
		
		List<LicenseData> licensedatalist = new ArrayList<>();
		
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
			
			String nodename = null;
			String licensedeadline = null;
			String expirationdate = null;
			
			ElementNode elementNode = null;
			boolean isDisplayLicenseConfig = false;
			boolean isDisplayLicensefile = false;
			
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				
				try {
					if(StringUtils.isBlank(line))
						continue;
					
					if(StringUtils.containsIgnoreCase(line, " UMG8900"))
						break;
					
					line = line.replaceAll("\\p{C}", "");
					
					if(isDisplayLicenseConfig) {
						
						if(StringUtils.containsIgnoreCase(line, "displ")) {
							
							String matchednodenmae = licenseNodeMatcher.getMatch(nodename);
							
							elementNode = deduceNodeFromNodeName(matchednodenmae);
							if(elementNode != null) {
								for(LicenseData licensedata : licensedatalist) {
									
									if(StringUtils.isBlank(licensedata.identifier))
										continue;
									
									ElementLicense elementLicense = new ElementLicense();
									elementLicense.setNodeId(elementNode.getId());
									elementLicense.setNodeObjectId(elementNode.getId());
									elementLicense.setNodeName(elementNode.getName());
									elementLicense.setNodeType(deduceType(elementNode));
									
									elementLicense.setIdentifier(licensedata.identifier);
									// Details
									elementLicense.setDetails(licensedata.details);
									
									// Allocated
									if(StringUtils.isNotBlank(licensedata.detailsvalue)) {
										if(NumberUtils.isDigits(licensedata.detailsvalue))
											elementLicense.setAllocated(Integer.parseInt(licensedata.detailsvalue));
										else
											elementLicense.setDetailsValue(licensedata.detailsvalue);
									}
									
									// Unit
									elementLicense.setUnit(licensedata.unit);
									
									if(StringUtils.isBlank(expirationdate)) {
										String year = StringUtils.substringBetween(licensedeadline, "year", "month").trim();
										String month = StringUtils.substringBetween(licensedeadline, "month", "day").trim();
										String day = StringUtils.substringAfter(licensedeadline, "day").trim();
										
										expirationdate = String.join("-", year, month, day);
									}
									
									// Expiration Date
									elementLicense.setExpirationDate(expirationdate);

									licenses.add(elementLicense);
								}
							}
							
							
							isDisplayLicenseConfig = false;
							licensedatalist.clear();
							nodename = null;
							continue;
						}
						
						if(StringUtils.containsIgnoreCase(line, "=")) {
							
							LicenseData licensedata = new LicenseData();
							String part1 = StringUtils.substringBefore(line, "=").trim();
							
							String unit = null;
							String details = part1;
							
							if(StringUtils.contains(part1, "(")) {
								String[] values = StringUtils.substringsBetween(part1, "(", ")");
								try { unit = values[values.length - 1]; } catch (Exception e) {}
								details = StringUtils.substringBeforeLast(part1, "(");
							}
							
							licensedata.detailsvalue = StringUtils.substringAfter(line, "=").trim();
							licensedata.unit = unit;
							licensedata.details = details;
							licensedata.identifier = part1;
							
							if(StringUtils.equalsIgnoreCase(details, "LICENSE deadline"))
								licensedeadline = licensedata.detailsvalue;
					
							licensedatalist.add(licensedata);
						}
					}
					
					if(isDisplayLicensefile) {
						if(StringUtils.containsIgnoreCase(line, "Valid file area ID = 0")) {
							if(elementNode != null) {
								ElementLicense elementLicense = new ElementLicense();
								elementLicense.setNodeId(elementNode.getId());
								elementLicense.setNodeObjectId(elementNode.getId());
								elementLicense.setNodeName(elementNode.getName());
								elementLicense.setNodeType(deduceType(elementNode));
								
								// Details
								elementLicense.setDetails("File name");
								elementLicense.setIdentifier("File name");
								// Allocated
								elementLicense.setDetailsValue(StringUtils.substringAfterLast(line, "=").trim());
								
								// Expiration Date
								elementLicense.setExpirationDate(expirationdate);

								licenses.add(elementLicense);
							}
							
							isDisplayLicensefile = false;
						}
					}
					
					if(StringUtils.containsIgnoreCase(line, "display license config")) {
						nodename = StringUtils.substringBetween(line, "[", "]");
						isDisplayLicenseConfig = true;
					}
					
					if(StringUtils.containsIgnoreCase(line, "display license file")) {
						isDisplayLicensefile = true;
					}

					
				} catch (Exception e) {
					e.printStackTrace();
					log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
		}
		
	}

	private void parseCoreFile(File file) {
		
		List<LicenseData> licensedatalist = new ArrayList<>();
		
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
			String line = null;
			String nodename = null;
			String nodetype = null;
			String collectiondate = null;
			boolean isDpsLicense = false;
			boolean isLicenseBlock = false;
			String licenseremaindays = null;
			String feature = null;
			
			while ((line = bufferedReader.readLine()) != null) {
				try {
					
					if(StringUtils.isBlank(line))
						continue;
					
					if(StringUtils.containsIgnoreCase(line, " UMG8900"))
						break;
					
					
					line = line.replaceAll("\\p{C}", "");
					
					if(StringUtils.isNotBlank(nodename) && StringUtils.isNotBlank(nodetype) && StringUtils.isNotBlank(collectiondate)) {
						
						if(isDpsLicense) {
							
							if(isLicenseBlock) {
								
								if(StringUtils.contains(line, " = ") && !StringUtils.containsIgnoreCase(line, "Number of results")) {
										
									LicenseData licensedata = new LicenseData();
									String part1 = StringUtils.substringBefore(line, "=").trim();
									if(StringUtils.equalsIgnoreCase(part1, "Feature"))
										feature = StringUtils.substringAfter(line, "=").trim();
									
									
									String unit = null;
									String details = part1;
									
									if(StringUtils.contains(part1, "(")) {
										String[] values = StringUtils.substringsBetween(part1, "(", ")");
										try { unit = values[values.length - 1]; } catch (Exception e) {}
										details = StringUtils.substringBeforeLast(part1, "(");
									}
									
									licensedata.identifier = part1;
									if(StringUtils.isNotBlank(feature) && !StringUtils.equalsIgnoreCase(part1, "Feature"))
										licensedata.identifier = String.join("_", part1, feature);
									
									licensedata.detailsvalue = StringUtils.substringAfter(line, "=").trim();
									licensedata.unit = unit;
									licensedata.details = details;
									
									if(StringUtils.equalsIgnoreCase(details, "License remain days"))
										licenseremaindays = licensedata.detailsvalue;
									
									licensedatalist.add(licensedata);
								}
								
								if(StringUtils.containsIgnoreCase(line, "Number of results = 1")) {
									String matchednodenmae = licenseNodeMatcher.getMatch(nodename);
									
									ElementNode elementNode = deduceNodeFromNodeName(matchednodenmae);
									if(elementNode != null) {
										for(LicenseData licensedata : licensedatalist) {
											
											ElementLicense elementLicense = new ElementLicense();
											elementLicense.setNodeId(elementNode.getId());
											elementLicense.setNodeObjectId(elementNode.getId());
											elementLicense.setNodeName(elementNode.getName());
											elementLicense.setNodeType(deduceType(elementNode));
											
											elementLicense.setIdentifier(licensedata.identifier);
											// Details
											elementLicense.setDetails(licensedata.details);
											
											// Allocated
											if(StringUtils.isNotBlank(licensedata.detailsvalue)) {
												if(NumberUtils.isDigits(licensedata.detailsvalue))
													elementLicense.setAllocated(Integer.parseInt(licensedata.detailsvalue));
												else
													elementLicense.setDetailsValue(licensedata.detailsvalue);
											}
											
											// Unit
											elementLicense.setUnit(licensedata.unit);
											
											if(TransmissionCommon.isInteger(licenseremaindays)) {
												// Expiration Date
												elementLicense.setExpirationDate(getExpirationDate(collectiondate, licenseremaindays));

											}
											
											licenses.add(elementLicense);
										}
									}
									
									isDpsLicense = false;
									isLicenseBlock = false;
								}
								
							}
							
							if(line.contains("------------")) {
								isLicenseBlock = true;
							}
							
						}
						
						if(StringUtils.containsIgnoreCase(line, "DSP LICENSE:;") || StringUtils.containsIgnoreCase(line, "DSP LICENSE: DT=DEV;")) {
							isDpsLicense = true;
						}
					}
					
					if(StringUtils.containsIgnoreCase(line, "MEID:") && StringUtils.containsIgnoreCase(line, "MENAME:")) {
						nodename = StringUtils.substringBetween(line, "MENAME:", "*").trim();
						nodetype = StringUtils.substringBetween(line, "+++", "/*").trim();
						collectiondate = StringUtils.substringAfterLast(line, "*/").trim();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
				}
					
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
		}
		
	}

	private void parseRanFile(File file) {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
			
			String line = null;
			
			String nodename = null;
			String technology = null;
			boolean isDspLicusage = false;
			boolean headerfound = false;
			boolean iscomma = false;
			Map<String, Entry<Integer, Integer>> tabheaderIndexes = new HashMap<>();
			Map<String, Integer> commaheaderIndexes = new HashMap<>();
			
			while ((line = bufferedReader.readLine()) != null) {
				try {
					if(StringUtils.isBlank(line))
						continue;
					
					if(StringUtils.containsIgnoreCase(line, " UMG8900"))
						break;
					
					line = StringUtils.trim(line.replaceAll("\\p{C}", ""));
					
					if(headerfound && (StringUtils.containsIgnoreCase(line, "To be continue") || StringUtils.containsIgnoreCase(line, " END") || StringUtils.containsIgnoreCase(line, "Number of results"))) {
						headerfound = false;
						iscomma = false;
						tabheaderIndexes.clear();
						commaheaderIndexes.clear();
					}
					
					if(headerfound && StringUtils.isNotEmpty(nodename)) {
						
						ElementNode elementNode = deduceNodeFromNodeName(nodename);
						
						if((elementNode instanceof ElementBsc) || (elementNode instanceof ElementBs))
							technology = "2G";
						
						else if((elementNode instanceof ElementRnc) || (elementNode instanceof ElementNb))
							technology = "3G";
						
						else if(elementNode instanceof ElementENodeB)
							technology = "4G";
						
						if(elementNode != null) {
							ElementLicense elementLicense = new ElementLicense();
							elementLicense.setNodeId(elementNode.getId());
							elementLicense.setNodeObjectId(elementNode.getId());
							elementLicense.setNodeName(elementNode.getName());
							elementLicense.setNodeType(deduceType(elementNode));
							elementLicense.setTechnology(technology);
							
							
							String identifier = null;
							String details = null;
							String allocated = null;
							String config = null;
							String usage = null;
							String unit = null;
							String expirationdate = null;
							
							
							if(iscomma) {
								String[] values = line.split(",");
								
								try { identifier = values[commaheaderIndexes.get("License Identifier".toUpperCase())]; } catch (Exception e) {}
								if(StringUtils.isBlank(identifier))
									try { identifier = values[commaheaderIndexes.get("Model".toUpperCase())]; } catch (Exception e) {}
								
								try { details = values[commaheaderIndexes.get("Description".toUpperCase())]; } catch (Exception e) {}
								if(StringUtils.isBlank(details))
									try { details = values[commaheaderIndexes.get("License Item".toUpperCase())]; } catch (Exception e) {}
								
								try { allocated = values[commaheaderIndexes.get("Allocated".toUpperCase())]; } catch (Exception e) {}
								
								try { config = values[commaheaderIndexes.get("Config".toUpperCase())]; } catch (Exception e) {}
								
								try { usage = values[commaheaderIndexes.get("Actual Used".toUpperCase())]; } catch (Exception e) {}
								if(StringUtils.isBlank(usage))
									try { usage = values[commaheaderIndexes.get("Usage".toUpperCase())]; } catch (Exception e) {}
								
								try { unit = values[commaheaderIndexes.get("Unit".toUpperCase())]; } catch (Exception e) {}
								
								try { expirationdate = values[commaheaderIndexes.get("Expiration Date".toUpperCase())]; } catch (Exception e) {}
								
								
							}
							else {
								identifier = getColumnValue("License Identifier", line, tabheaderIndexes);
								if(StringUtils.isBlank(identifier))
									identifier = getColumnValue("Model", line, tabheaderIndexes);
								
								details = getColumnValue("Description", line, tabheaderIndexes);
								if(StringUtils.isBlank(details))
									details = getColumnValue("License Item", line, tabheaderIndexes);
								
								allocated = getColumnValue("Allocated", line, tabheaderIndexes);
								
								config = getColumnValue("Config", line, tabheaderIndexes);
								
								usage = getColumnValue("Actual Used", line, tabheaderIndexes);
								if(StringUtils.isBlank(usage))
									usage = getColumnValue("Usage", line, tabheaderIndexes);
								
								unit = getColumnValue("Unit", line, tabheaderIndexes);
								
								expirationdate = getColumnValue("Expiration Date", line, tabheaderIndexes);
								
							
							}

							// Identifier
							elementLicense.setIdentifier(StringUtils.trim(identifier));
							
							// detail
							elementLicense.setDetails(StringUtils.trim(details));
							
							// Allocated
							try { elementLicense.setAllocated(Integer.parseInt(StringUtils.trim(StringUtils.substringBefore(allocated, ".")))); } catch (Exception e) {}

							// config
							try { elementLicense.setConfig(Integer.parseInt(StringUtils.trim(StringUtils.substringBefore(config, ".")))); } catch (Exception e) {}
							
							// usage
							try { elementLicense.setUsage(Integer.parseInt(StringUtils.trim(StringUtils.substringBefore(usage, ".")))); } catch (Exception e) {}
							
							// unit
							elementLicense.setUnit(StringUtils.trim(unit));
							
							// Expiration Date
							elementLicense.setExpirationDate(StringUtils.trim(expirationdate));
							
							// Mandatory fields
							if(StringUtils.isNotBlank(elementLicense.getIdentifier()) && StringUtils.isNotBlank(elementLicense.getDetails())) {
								
								String key = String.join("_", elementLicense.getNodeId(), elementLicense.getIdentifier(), elementLicense.getDetails()).toUpperCase();
//								String key = String.join("_", elementLicense.getNodeId(), elementLicense.getIdentifier()).toUpperCase();
								ElementLicense oldelementLicense = licensesMap.get(key.toLowerCase());
								if(oldelementLicense == null) {
									licensesMap.put(key, elementLicense);
								} else {
									if(StringUtils.isBlank(oldelementLicense.getExpirationDate()) && StringUtils.isNotBlank(elementLicense.getExpirationDate()))
										licensesMap.put(key, elementLicense);
									else if(StringUtils.isNotBlank(oldelementLicense.getExpirationDate()) && StringUtils.isBlank(elementLicense.getExpirationDate()))
										licensesMap.put(key, oldelementLicense);
									else {
										String oldExpirationDate = oldelementLicense.getExpirationDate();
										String expirationDate = elementLicense.getExpirationDate();
										
										if(StringUtils.equalsIgnoreCase(oldExpirationDate, "PERMANENT"))
											licensesMap.put(key, oldelementLicense);
										else if(StringUtils.equalsIgnoreCase(expirationDate, "PERMANENT"))
											licensesMap.put(key, elementLicense);
										else if(StringUtils.contains(oldExpirationDate, "-") && StringUtils.contains(expirationDate, "-")) {
											try {
												Date oldDate = sdf.parse(oldExpirationDate);
										        Date newDate = sdf.parse(expirationDate);
										        
										        if (newDate.after(oldDate)) 
										        	licensesMap.put(key, elementLicense);
											} catch (Exception e) {
											}
											
										}
									}
								}
								
							}
							
						}
					}
					
					if(StringUtils.containsIgnoreCase(line, "Operator Index") && StringUtils.containsIgnoreCase(line, "Operator Name")) {
						headerfound = true;
						
						if(line.contains(",")) {
							iscomma = true;
							
							commaheaderIndexes = deduceColumnsIndexesFromLine(line);
						}
						else {
							tabheaderIndexes = deduceColumnsIndexesFromLine(line, "\\s{2,}");
						}
						
					}
					
					
					if(isDspLicusage && StringUtils.isBlank(nodename)) {
						if(StringUtils.contains(line, ":"))
							nodename = StringUtils.trim(StringUtils.substringAfter(line, ":"));
						else
							nodename = StringUtils.trim(line);
						
						if(StringUtils.contains(nodename, "@"))
							nodename = StringUtils.trim(StringUtils.substringAfter(line, "@"));
						
						if(StringUtils.containsIgnoreCase(nodename, "5G"))
							technology = "5G";
						isDspLicusage = false;
					}
					
					if(!line.contains("%") && (StringUtils.containsIgnoreCase(line, "DSP LICUSAGE:") || StringUtils.containsIgnoreCase(line, "DSP LICINFO:") || StringUtils.containsIgnoreCase(line, "DSP LICENSE:"))) {
						technology = null;
						if(StringUtils.containsIgnoreCase(line, "FUNCTIONTYPE")) {
							String functiontype = StringUtils.substringBetween(line.toUpperCase(), "FUNCTIONTYPE=", ";");
							if(StringUtils.equalsIgnoreCase(functiontype, "GBTS"))
								technology = "2G";
							else if(StringUtils.equalsIgnoreCase(functiontype, "NodeB"))
								technology = "3G";
						}
						
						else if(StringUtils.containsIgnoreCase(line, "DSP LICINFO:")) {
							technology = "4G";
						}
							
						nodename = null;
						isDspLicusage = true;
					}
					
				} catch (Exception e) {
					log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
				}
			}
		} catch (Exception e) {
			log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
		}
	}

	private Map<String, Integer> deduceColumnsIndexesFromLine(String line) {
		Map<String, Integer> headerIndexes = new HashMap<>();
		String[] headerNames = line.split(",");
		
		for (int i = 0; i < headerNames.length; i++) {
			String currentname = headerNames[i].trim();
			headerIndexes.put(currentname.toUpperCase(), i);
		}
		
		return headerIndexes;
	}

	private String getExpirationDate(String collectiondate, String licenseremaindays) {
		String date = StringUtils.substringBefore(collectiondate, " ");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		try{

		   c.setTime(sdf.parse(date));
		}catch(Exception e){
			return null;
		}
		c.add(Calendar.DAY_OF_MONTH, Integer.parseInt(licenseremaindays));
		
		String expirationDate = sdf.format(c.getTime()); 
		
		return expirationDate;
	}

	private Map<String, Entry<Integer, Integer>> deduceColumnsIndexesFromLine(String line, String pattern){
		Map<String, Entry<Integer, Integer>> headerIndexes = new HashMap<>();
		
		String[] headerNames = line.split(pattern);
		Integer currentstartpos = 0;
		for (int i = 0; i < headerNames.length; i++) {
			
			Integer index_of_name = null;
			Integer index_of_next_name = null;
			String currentname = headerNames[i];
			
			if(i != 0) {
				String previousname = headerNames[i -1];
				currentstartpos = currentstartpos + previousname.length();
			}
			
			index_of_name = StringUtils.indexOf(line, currentname, currentstartpos);
			
			if(i != headerNames.length - 1) {
				String nextname = headerNames[i + 1];
				int nextstartpos = index_of_name + currentname.length();
				index_of_next_name = StringUtils.indexOf(line, nextname, nextstartpos);
			}
			
			else {
				index_of_next_name = line.length();
			}
			
			Map.Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<Integer, Integer>(index_of_name, index_of_next_name);
			headerIndexes.put(currentname.toUpperCase(), entry);
		}
		
		return headerIndexes;
	}
	
	private String getColumnValue(String columnname, String line, Map<String, Entry<Integer, Integer>> headerIndexes) {
		String value = null;
		
		Entry<Integer, Integer> entry = headerIndexes.get(columnname.toUpperCase());
		if(entry != null)
			value = StringUtils.substring(line, entry.getKey(), entry.getValue());
		
		return StringUtils.trim(value);
	}

	private void init() {
		licenses = new ArrayList<>();
		licensesMap = new HashMap<>();
		
		bscsByName = new HashMap<>();
		rncsByName = new HashMap<>();
		ranBssByName = new HashMap<>();
		btssByName = new HashMap<>();
		nodeBsByName = new HashMap<>();
		enodeBsByName = new HashMap<>();
		ranBssByName = new HashMap<>();
		coreElementsByName = new HashMap<>();
		mgwsByName = new HashMap<>();
		msssByName = new HashMap<>();
		hsssByName = new HashMap<>();
		mscsByName = new HashMap<>();
		
		for(ElementBsc bsc : m2v2r0RanPhysicalParser.getElementBscs().values())
			bscsByName.put(bsc.getName().toUpperCase(), bsc);
		
		for(ElementRnc rnc : m2v2r0RanPhysicalParser.getElementRncs().values())
			rncsByName.put(rnc.getName().toUpperCase(), rnc);
		
		for(ElementBs bts : m2v2r0RanPhysicalParser.getElementBtss().values())
			btssByName.put(bts.getName().toUpperCase(), bts);
		
		for(ElementENodeB enodeB : m2v2r0RanPhysicalParser.getElementENodeBs().values())
			enodeBsByName.put(enodeB.getName().toUpperCase(), enodeB);
		
		for(ElementNb nodeB : m2v2r0RanPhysicalParser.getElementNodeBs().values())
			nodeBsByName.put(nodeB.getName().toUpperCase(), nodeB);
		
		for(ElementCoreElement coreElement : m2v2r0RanPhysicalParser.getCoreElementMap().values())
			coreElementsByName.put(coreElement.getName().toUpperCase(), coreElement);
		
		for(ElementMss coreElement : m2v2r0RanPhysicalParser.getMssMap().values())
			msssByName.put(coreElement.getName().toUpperCase(), coreElement);
		
		for(ElementMgw coreElement : m2v2r0RanPhysicalParser.getMgwMap().values())
			mgwsByName.put(coreElement.getName().toUpperCase(), coreElement);
		
		for(ElementHss coreElement : m2v2r0RanPhysicalParser.getHssMap().values())
			hsssByName.put(coreElement.getName().toUpperCase(), coreElement);
		
		for(ElementMsc coreElement : m2v2r0RanPhysicalParser.getMscMap().values())
			mscsByName.put(coreElement.getName().toUpperCase(), coreElement);
		
		for(ElementRanBs ranBs : m2v2r0RanPhysicalParser.getElementRanBss().values()) {
			ranBssByName.put(ranBs.getName().toUpperCase(), ranBs);
			
			if(ranBs.getGsmCapability() != null)
				ranBssByName.put(ranBs.getGsmCapability().getName().toUpperCase(), ranBs);
			if(ranBs.getUmtsCapability() != null)
				ranBssByName.put(ranBs.getUmtsCapability().getName().toUpperCase(), ranBs);
			if(ranBs.getLteCapability() != null)
				ranBssByName.put(ranBs.getLteCapability().getName().toUpperCase(), ranBs);
			if(ranBs.getNrCapability() != null)
				ranBssByName.put(ranBs.getNrCapability().getName().toUpperCase(), ranBs);
		}
		
		for(String gnodebtoranbs : m2v2r0RanPhysicalParser.getRanbsRelationMap().keySet()) {
			String ranbsid = m2v2r0RanPhysicalParser.getRanbsRelationMap().get(gnodebtoranbs);
			if(m2v2r0RanPhysicalParser.getElementRanBss().containsKey(ranbsid)) {
				ranBssByName.put(gnodebtoranbs.toUpperCase(), m2v2r0RanPhysicalParser.getElementRanBss().get(ranbsid));
			}
		}
		
	}
	
	private ElementNode deduceNodeFromNodeName(String nodeName) {
		ElementNode elmentNode = null;
		
		if(StringUtils.isBlank(nodeName))
			return null;
		
		if(bscsByName.containsKey(nodeName.toUpperCase()))
			return bscsByName.get(nodeName.toUpperCase());
		
		else if(rncsByName.containsKey(nodeName.toUpperCase()))
			return rncsByName.get(nodeName.toUpperCase());
		
		else if(btssByName.containsKey(nodeName.toUpperCase()))
			return btssByName.get(nodeName.toUpperCase());
		
		else if(enodeBsByName.containsKey(nodeName.toUpperCase()))
			return enodeBsByName.get(nodeName.toUpperCase());
		
		else if(nodeBsByName.containsKey(nodeName.toUpperCase()))
			return nodeBsByName.get(nodeName.toUpperCase());
		
		else if(coreElementsByName.containsKey(nodeName.toUpperCase()))
			return coreElementsByName.get(nodeName.toUpperCase());
		
		else if(hsssByName.containsKey(nodeName.toUpperCase()))
			return hsssByName.get(nodeName.toUpperCase());
		
		else if(msssByName.containsKey(nodeName.toUpperCase()))
			return msssByName.get(nodeName.toUpperCase());
		
		else if(mgwsByName.containsKey(nodeName.toUpperCase()))
			return mgwsByName.get(nodeName.toUpperCase());
		
		else if(mscsByName.containsKey(nodeName.toUpperCase()))
			return mscsByName.get(nodeName.toUpperCase());
		
		else if(ranBssByName.containsKey(nodeName.toUpperCase()))
			return ranBssByName.get(nodeName.toUpperCase());
		
		
		
		return elmentNode;
		
	}
	
	private String deduceType(ElementNode elmentNode) {
		if(elmentNode instanceof ElementBsc)
			return PossibleNodeType.BSC.toString();
		
		else if(elmentNode instanceof ElementRnc)
			return PossibleNodeType.RNC.toString();
		
		else if(elmentNode instanceof ElementBs)
			return PossibleNodeType.BTS.toString();
		
		else if(elmentNode instanceof ElementENodeB)
			return PossibleNodeType.eNodeB.toString();
		
		else if(elmentNode instanceof ElementNb)
			return PossibleNodeType.NodeB.toString();
		
		else if(elmentNode instanceof ElementRanBs)
			return PossibleNodeType.RanBs.toString();
		
		else if(elmentNode instanceof ElementHss)
			return PossibleNodeType.HSS.toString();
		
		else if(elmentNode instanceof ElementMsc)
			return PossibleNodeType.MSC.toString();
		
		else if(elmentNode instanceof ElementMss)
			return PossibleNodeType.MSS.toString();
		
		else if(elmentNode instanceof ElementMgw)
			return PossibleNodeType.MGW.toString();
		
		else if(elmentNode instanceof ElementCoreElement)
			return PossibleNodeType.COREELEMENT.toString();
		
		return null;
	}
	
	public static class LicenseData {
		public String identifier;
		public String details;
		public String detailsvalue;
		public String unit;
		public String usage;
	}
}
