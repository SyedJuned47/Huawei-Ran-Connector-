
package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobinets.nep.model.autoimporter.Category;
import com.mobinets.nep.model.autoimporter.ErrorAlarm;
import com.mobinets.nep.model.autoimporter.ImporterConnector;
import com.mobinets.nep.npt.NptConstants;
import com.mobinets.nps.customer.transmission.common.BoardTypeMatchingParser;
import com.mobinets.nps.customer.transmission.common.CommonConfig;
import com.mobinets.nps.customer.transmission.common.FlexibleDateParser;
import com.mobinets.nps.customer.transmission.common.ManufactureFixer;
import com.mobinets.nps.customer.transmission.common.ManufacturerConstant;
import com.mobinets.nps.customer.transmission.common.NodesMatchingParser;
import com.mobinets.nps.customer.transmission.common.SiteMatchingFounder;
import com.mobinets.nps.customer.transmission.common.TransmissionCommon;
import com.mobinets.nps.customer.transmission.common.mbtsrelation.MbtsRelationData;
import com.mobinets.nps.customer.transmission.common.mbtsrelation.MbtsRelationMatchingParser;
import com.mobinets.nps.customer.transmission.manufacture.common.NeReportParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.common.M2V2R0RanCommon.Antenna;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.common.M2V2R0RanUniqueidParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.common.MultiPhysicalDumpParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers.BscRuMapper;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers.CabinetModelMatchingParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers.ModelByRackTypeRuleParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers.ModelMatchingParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers.Serie3900Mapper;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.mappers.SlotIndexMatchingParser;
import com.mobinets.nps.daemon.common.NodeContainer;
import com.mobinets.nps.model.UserTypes.PossibleNodeType;
import com.mobinets.nps.model.autoimporter.NetworkElementDump;
import com.mobinets.nps.model.customer.data.element.ElementBs;
import com.mobinets.nps.model.customer.data.element.ElementBsc;
import com.mobinets.nps.model.customer.data.element.ElementCg;
import com.mobinets.nps.model.customer.data.element.ElementCgp;
import com.mobinets.nps.model.customer.data.element.ElementCoreElement;
import com.mobinets.nps.model.customer.data.element.ElementENodeB;
import com.mobinets.nps.model.customer.data.element.ElementGGSN;
import com.mobinets.nps.model.customer.data.element.ElementHlr;
import com.mobinets.nps.model.customer.data.element.ElementHss;
import com.mobinets.nps.model.customer.data.element.ElementIpclk;
import com.mobinets.nps.model.customer.data.element.ElementMgw;
import com.mobinets.nps.model.customer.data.element.ElementMme;
import com.mobinets.nps.model.customer.data.element.ElementMsc;
import com.mobinets.nps.model.customer.data.element.ElementMss;
import com.mobinets.nps.model.customer.data.element.ElementNb;
import com.mobinets.nps.model.customer.data.element.ElementNode;
import com.mobinets.nps.model.customer.data.element.ElementRadioAntenna;
import com.mobinets.nps.model.customer.data.element.ElementRanBs;
import com.mobinets.nps.model.customer.data.element.ElementRnc;
import com.mobinets.nps.model.customer.data.element.ElementSessionEngine;
import com.mobinets.nps.model.customer.data.element.ElementSgsn;
import com.mobinets.nps.model.customer.data.element.ElementSgw;
import com.mobinets.nps.model.customer.data.element.ElementSranController;
import com.mobinets.nps.model.customer.data.element.ElementTrau;
import com.mobinets.nps.model.customer.data.element.ElementUgw;
import com.mobinets.nps.model.network.ElementAdditionalInfo;
import com.mobinets.nps.model.network.ElementAdditionalInfo.ElementType;
import com.mobinets.nps.model.network.Site;
import com.mobinets.nps.model.nodeinterfaces.NodeBoard;
import com.mobinets.nps.model.nodeinterfaces.NodeCabinet;
import com.mobinets.nps.model.nodeinterfaces.NodeInterface;
import com.mobinets.nps.model.nodeinterfaces.NodeShelf;
import com.mobinets.nps.model.nodeinterfaces.NodeSlot;
import com.mobinets.nps.summaryReport.SummaryReportManagement;

public class M2V2R0RanPhysicalParser {

	private static final Log log = LogFactory.getLog(M2V2R0RanPhysicalParser.class);

	private Map<String, String> m2v2r0Patterns;
	private CommonConfig m2v2r0Config;

	private HashMap<String, ElementRnc> rncMap;
	private HashMap<String, ElementNb> nodeBMap;
	private HashMap<String, ElementBs> btsMap;
	private HashMap<String, ElementBsc> bscMap;
	private HashMap<String, ElementENodeB> eNodeBMap;
	private HashMap<String, ElementRanBs> ranBsMap;
	private HashMap<String, ElementGGSN> ggsnMap;
	private HashMap<String, ElementSgsn> sgsnMap;
	private HashMap<String, ElementSranController> sRanControllerMap;
	private HashMap<String, ElementMgw> mgwMap;
	private HashMap<String, ElementMsc> mscMap;
	private HashMap<String, ElementHlr> hlrMap;
	private HashMap<String, ElementCgp> cgpMap;
	private HashMap<String, ElementSessionEngine> sessionEngineMap;
	private HashMap<String, ElementCg> cgMap;
	private HashMap<String, ElementIpclk> ipclkMap;
	private HashMap<String, ElementUgw> ugwMap;
	private HashMap<String, ElementCoreElement> coreElementMap;
	private HashMap<String, ElementMss> mssMap;
	private HashMap<String, ElementMme> mmeMap;
	private HashMap<String, ElementSgw> sgwMap;
	private HashMap<String, ElementHss> hssMap;
	private HashMap<String, ElementRadioAntenna> antennaMap;
	private MultiPhysicalDumpParser multiPhysicalDumpParser;

	private Map<String, NodeSlot> nodeSlotMap;
	private Map<String, NodeSlot> nodeSlotForMatching;

	private Map<String, String> parentRncMap;
	private Map<String, String> parentBscMap;
	private List<String> radioantennaserialnoList = new ArrayList<String>();

	private NodesMatchingParser nodesMatchingParser;
	private FlexibleDateParser flexibleDateParser;
	private BoardTypeMatchingParser boardTypeMatching;
	private SiteMatchingFounder siteMatchFounder;

	private Map<String, NodeCabinet> mapCabinet;
	private Map<String, NodeShelf> mapShelf;
	private Map<String, NodeBoard> nodeBoardMap;
	private Map<String, NodeInterface> nodeInterfaceMap;
	private Map<String, NodeBoard> nodeBoardsForMatching;
	private Map<String, String> subrackMap;
	private List<String> nodeModelByRackTypeList;
	private List<String> nodeModelByFrameTypeList;
	private List<NodeCabinet> currentCabinetsList;
	private List<NodeShelf> currentShelfsList;

	private Map<String, String> boardId4NodeInterfaceMap;
	private Map<String, String> neModel4BtsAndSRanBsMap;
	private Map<String, String> btsNameBySiteIndex;
	private Map<String, String> neModel4NodeBMap;
	private Map<String, String> neModel4EnodeBMap;

	private SummaryReportManagement summaryManager;
	private Map<String, Site> sitesByNameAndObjId;
	private ManufactureFixer manufactureFixer;
	private BscRuMapper bscRuMapper;
	private Serie3900Mapper serie3900RuMapper;
	private CabinetModelMatchingParser cabinetModelMatchingParser;

	private Map<String, String> specificBoardMatchingMap;
	private Map<String, String> nodeTypeByNeIdMap;

	private Map<String, Map<String, String>> nodeBNameByCellIdByRnc;
	private String softVersion;
//	private String hardwareVer;

	private ModelMatchingParser modelMatchingParser;
	private SlotIndexMatchingParser slotIndexMatchingParser;
	private ModelByRackTypeRuleParser modelByRackTypeRuleParser;

	private Map<String, NodeSlot> map4SpuBoards;
	private Map<String, ElementTrau> elementTrauMap;

	private Map<String, Set<String>> bbu3900SerialNbMap;
	private Map<String, Set<String>> neTypeBbu3900SerialNbMap;
	private Map<String, Set<String>> virtualMicroSerialNbMap;
	private Map<String, Set<String>> neTypeVirtualMicroSerialNbMap;
	private Set<String> bts3900SerialNb;
	private Set<String> microBts3900SerialNb;
	private Map<String, Set<String>> cabinetsIdByNeIdMap;
	private Map<String, Set<String>> shelfsIdByNeIdMap;
	private Map<String, Set<String>> slotsIdByNeIdMap;
	private Map<String, String> ranbsRelationMap;
    private M2V2R0RanUniqueidParser m2v2r0ranUniqueidParser;
	private Map<String, String> ranBsIdByRoleIdMap;
	private Map<String, String> btsNameByNodebNameRelationMap;
	private Map<String, String> nodeBFunctionNameByNeIdMap;
	private Map<String, String> neIdByNodeBFunctionNameMap;
	private Map<String, String> neIdByGbtsFunctionNameMap;
	private Map<String, String> bts3900NameByBtsNameRelationMap;
	private Map<String, String> bts3900NameByNodeBNameRelationMap;
	private Map<String, String> microBts3900NameByBtsNameRelationMap;
	private Map<String, String> microBts3900NameByNodeBNameRelationMap;
	private Map<String, String> enodeBNameByNodeBNameRelationMap;
	private String btsNameFromRelationFile;
	private String nodebNameFromRelationFile;
	private String eNodebNameFromRelationFile;
	private String bts3900NameFromRelationFile;
	private String microBts3900NameFromRelationFile;

	private Set<String> bts5900nodes;
	private Map<String, String> fileNameByNodeIdMap;
	private Map<String, String> nodeNameMap;
	private Map<String, Set<String>> cellNameByNeIdMap;
	private Map<String, NetworkElementDump> networkElementDumps;
	private Map<String, File> filesList;
	private Map<String, String> enodeBFunctionNameByneIdMap;
	private Map<String, String> gbtsFunctionNameByneIdMap;
	private MbtsRelationMatchingParser mbtsRelationMatchingParser;
	private Set<String> nodesWithNoSerials;
	private Map<String, ElementAdditionalInfo> addInfoMap;
	private NeReportParser neReportParser;
	private Map<String, List<Antenna>> antennaPartMap;
	private Map<String,String> newIdMap;

	/******************************
	 ********** setters ***********
	 *******************************/
	
	public void setSiteMatchFounder(SiteMatchingFounder siteMatchFounder) {
		this.siteMatchFounder = siteMatchFounder;
	}

	public void setNeReportParser(NeReportParser neReportParser) {
		this.neReportParser = neReportParser;
	}

	public void setFlexibleDateParser(FlexibleDateParser flexibleDateParser) {
		this.flexibleDateParser = flexibleDateParser;
	}

	public void setBoardTypeMatching(BoardTypeMatchingParser boardTypeMatching) {
		this.boardTypeMatching = boardTypeMatching;
	}

	public void setNodesMatchingParser(NodesMatchingParser nodesMatchingParser) {
		this.nodesMatchingParser = nodesMatchingParser;
	}

	public void setM2v2r0Config(CommonConfig m2v2r0Config) {
		this.m2v2r0Config = m2v2r0Config;
	}

	public void setM2v2r0Patterns(Map<String, String> m2v2r0Patterns) {
		this.m2v2r0Patterns = m2v2r0Patterns;
	}

	public SummaryReportManagement getSummaryManager() {
		return summaryManager;
	}

	public void setSummaryManager(SummaryReportManagement summaryManager) {
		this.summaryManager = summaryManager;
	}

	public void setManufactureFixer(ManufactureFixer manufactureFixer) {
		this.manufactureFixer = manufactureFixer;
	}

	public void setBscRuMapper(BscRuMapper bscRuMapper) {
		this.bscRuMapper = bscRuMapper;
	}

	public void setSerie3900RuMapper(Serie3900Mapper serie3900RuMapper) {
		this.serie3900RuMapper = serie3900RuMapper;
	}

	public void setModelMatchingParser(ModelMatchingParser modelMatchingParser) {
		this.modelMatchingParser = modelMatchingParser;
	}

	public void setSlotIndexMatchingParser(SlotIndexMatchingParser slotIndexMatchingParser) {
		this.slotIndexMatchingParser = slotIndexMatchingParser;
	}

	public void setModelByRackTypeRuleParser(ModelByRackTypeRuleParser modelByRackTypeRuleParser) {
		this.modelByRackTypeRuleParser = modelByRackTypeRuleParser;
	}

	public void setCabinetModelMatchingParser(CabinetModelMatchingParser cabinetModelMatchingParser) {
		this.cabinetModelMatchingParser = cabinetModelMatchingParser;
	}

	public void setMbtsRelationMatchingParser(MbtsRelationMatchingParser mbtsRelationMatchingParser) {
		this.mbtsRelationMatchingParser = mbtsRelationMatchingParser;
	}
	
	public void setM2v2r0ranUniqueidParser(M2V2R0RanUniqueidParser m2v2r0ranUniqueidParser) {
		this.m2v2r0ranUniqueidParser = m2v2r0ranUniqueidParser;
	}

//2 file
	public void parseFiles() {
		log.debug("Begin of Huawei M2000_V2R0 Ran parsing from Huawei Files");
		log.debug("Sample Logs Added for Testing Radio Antenna");
		initMap();

		try {
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			String pathFolder = m2v2r0Config.getProperty("huawei.m2v2r0.ran.dump.folder");

			if (null == pathFolder) {
				log.error("Missing Attribute (huawei.m2v2r0.ran.dump.folder) in manufacture-config.xml file.");
				return;
			}

			File folderDumps = new File(pathFolder);
			if (!folderDumps.exists()) {
				log.error("Folder (" + pathFolder + ") not found");
				return;
			}

			summaryManager.createFolderSummary(pathFolder);

			List<File> neFiles = new ArrayList<File>();
			listf(folderDumps, neFiles);
			

			neFiles.sort(Comparator.comparing(File::getName));

			Map<String, Set<String>> discardedRanBsMap = new HashMap<String, Set<String>>();
			for (File neFile : neFiles) {
				try {
					log.debug("Start Processing file :" + neFile.getName());

					InputStream in = new FileInputStream(neFile);
					XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
					boolean boardExist = false;
					StartElement startElement = null;
					String neId = null;
					String hostVersion = null;
					String type = null;
					String site = null;
					String siteNo = null;
					String neName = null;
					String neType = null;
					String neModel = null;
					String attrName = null;
					String Supported = null;
					String nodeBFunctionNameForSRan = null;
					String gbtsFunctionNameForSran = null;
					String eNodeBFunctionNameForSRan = null;
					Set<String> gCellNameSet = new HashSet<String>();
					Set<String> uCellNameSet = new HashSet<String>();
					

					softVersion = null;
					nodebNameFromRelationFile = null;
					btsNameFromRelationFile = null;
					eNodebNameFromRelationFile = null;
					bts3900NameFromRelationFile = null;
					microBts3900NameFromRelationFile = null;
					
					Map<String, String> subSlotNo2And3Map = new HashMap<String, String>();
					Map<String, String> boardUPIUMap = new HashMap<String, String>();

					currentCabinetsList = new ArrayList<NodeCabinet>();
					currentShelfsList = new ArrayList<NodeShelf>();
					nodeModelByRackTypeList = new ArrayList<String>();
					nodeModelByFrameTypeList = new ArrayList<String>();
					
					while (eventReader.hasNext()) {
						XMLEvent event = null;
						try {
							event = eventReader.nextEvent();
						} catch (Exception e) {
							log.error("Can't read the xml file: " + neFile.getPath());
							break;
						}

						if (event.isStartElement()) {
							startElement = event.asStartElement();
							String localPart = startElement.getName().getLocalPart();

							// extract neModel for nodeB && ENodeB from specific
							// files
							if (neFile.getName().matches(m2v2r0Patterns.get("fileNamePat4NodeBENodeBModel"))) {
								fillNeModelMaps4NodeBENodeB(startElement, localPart);
							}
							if (neFile.getName().matches(m2v2r0Patterns.get("fileNamePat4Roles"))) {
								fillMap4Roles(startElement, localPart);
							}

							if (localPart.matches(m2v2r0Patterns.get("ne"))) {
								// get neName
								Attribute neNameAttribute = startElement
										.getAttributeByName(new QName(m2v2r0Patterns.get("neName")));
								if (neNameAttribute != null) {
									neName = neNameAttribute.getValue();
									
									neName = StringUtils.replace(neName, "&", "");
								}

								// get Type
								Attribute neTypeAttribute = startElement
										.getAttributeByName(new QName(m2v2r0Patterns.get("neType")));
								if (neTypeAttribute != null) {
									neType = neTypeAttribute.getValue();

									if (neType != null) {
										if (isNeTypeMatched(neType, m2v2r0Patterns.get("cgpomu"))) {
											if (manufactureFixer.isCellCImport()) {
												type = nodesMatchingParser.getNeType(neType);
											} else if (manufactureFixer.isZkImport()) {
												type = nodesMatchingParser.getNeType(neName);
											} else {
												if (neName != null)
													neName = neName.split("_CGP")[0];

												type = nodesMatchingParser.getNeType(neName);
											}
										} else if (isNeTypeMatched(neType, m2v2r0Patterns.get("hss"))
												&& !manufactureFixer.isMtcTouchImport()
												&& !manufactureFixer.isOmanTel()) {
											if (manufactureFixer.isZkImport())
												type = "HSS";
											else {
												if (neName != null)
													neName = neName.split("_HSS9860")[0];

												type = nodesMatchingParser.getNeType(neName);
											}
										} else if (isNeTypeMatched(neType, m2v2r0Patterns.get("ugw"))
												&& manufactureFixer.isMtcTouchImport())
											type = "Core Element";
										else
											type = nodesMatchingParser.getNeType(neType);
									}
								}

								// get neId
								Attribute neIdAttribute = startElement
										.getAttributeByName(new QName(m2v2r0Patterns.get("neFdn")));
								if (neIdAttribute != null) {
									neId = neIdAttribute.getValue();

									if (manufactureFixer.isMtcTouchImport()) {
										Matcher neIdMatcher = Pattern.compile(m2v2r0Patterns.get("neIdPat"))
												.matcher(neId);

										if (neIdMatcher.find() && !type.matches(m2v2r0Patterns.get("enodeB")))
											neId = neIdMatcher.group(1);

										else
											neId = neName;
									} else
										neId = neName;
								}

								if(manufactureFixer.isZkImport())
									neId=multiPhysicalDumpParser.getCorrespandantDump(neId);
							}

							else if (localPart.matches(m2v2r0Patterns.get("table"))) {
								// get attrName
								Attribute attrNameAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("attrName")));
								if (attrNameAttribute != null) {
									attrName = attrNameAttribute.getValue();
								}
							}

							else if (attrName != null && localPart.matches(m2v2r0Patterns.get("row"))) {
								if (attrName.matches(m2v2r0Patterns.get("rack"))) {
									createCabinet(startElement, neType, neId);
								}

								// extract Site
								if (attrName.matches(m2v2r0Patterns.get("site4Node")) && siteNo == null)
									siteNo = extractSiteNo(startElement, neType);

								if(site == null){
									site=siteMatchFounder.tryToMatchSite(neName);
									if (manufactureFixer.isAzerbaijan())
										site = neId;
								}
								if (site == null || site.trim().equalsIgnoreCase(neName))
									site = manufactureFixer.fixSiteHuaweiM2000V2R0(neType, neId, neName, siteNo,
											sitesByNameAndObjId);

								if (attrName.matches(m2v2r0Patterns.get("frame"))) {
									createShelf(neFile.getName(),startElement, neType, type, neId, site);
								}
								
								if (attrName.matches(m2v2r0Patterns.get("hostVerTable"))) {
									createHostVer(neFile.getName(), startElement, neType, type, neId);
								}
                                
							//Changes to be done.
								if(type == null)
                                type ="";
								
								if (attrName.matches(m2v2r0Patterns.get("board"))) {

									boardExist = createBoard(boardExist, startElement, neId, neName, neType, type, subSlotNo2And3Map, boardUPIUMap);
								}
								
								if (attrName.matches(m2v2r0Patterns.get("antenna"))) {
									if(StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.antenna.parsing"), "false"))
										createAntenna(neFile.getAbsolutePath(), startElement, neId);
								}
								

								// put parentRnc
								if (((isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu"))
										|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Umts"))
										|| isNeTypeMatched(neType, "(?i)BSC6910UMTS")
										|| isNeTypeMatched(neType, "(?i)BSC6910GU"))
										&& attrName.matches(m2v2r0Patterns.get("cell")))
										|| (isNeTypeMatched(neType, m2v2r0Patterns.get("rnc"))
												&& attrName.matches(m2v2r0Patterns.get("rncCell")))) {
									putParentRnc(startElement, neId, neType);
								}

								// put parentBsc
								if (((isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu"))
										|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gsm"))
										|| isNeTypeMatched(neType, "(?i)BSC6910GU")
										|| isNeTypeMatched(neType, "(?i)BSC6910GSM"))
										&& attrName.matches(m2v2r0Patterns.get("bts")))
										|| (isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6000"))
												&& attrName.matches(m2v2r0Patterns.get("bsc6000Bts")))) {
									putParentBsc(startElement, neId);
								}

								// worked if indexOnSlot different for 0
								if (attrName.matches(m2v2r0Patterns.get("port"))) {
									// putBoardId4NodeInterface(startElement,
									// neName);
								}

								if ((isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu"))
										|| isNeTypeMatched(neType, "(?i)BSC6910GU")
										|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gsm"))
										|| isNeTypeMatched(neType, "(?i)BSC6910GSM"))
										&& attrName.matches(m2v2r0Patterns.get("bts"))) {
									putNeModel4BtsAndSRanBs(startElement, neId);
								}

								if (attrName.matches("(?i)MBTS3GCell"))
									fillNodeBFuctionNameByNodeName(startElement, neId);

								if (attrName.matches("(?i)NodeBCell")) {
									String nodeBName = null;

									Attribute nodeBFunctionNameAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("NodeBFunctionName")));
									if (nodeBFunctionNameAttribute != null) {
										nodeBName = nodeBFunctionNameAttribute.getValue();
									}

									if (nodeBName != null && !nodeBName.isEmpty() && neId != null)
										neIdByNodeBFunctionNameMap.put(nodeBName.toUpperCase() + "_NODEB", neId);
								}

								if (attrName.matches("(?i)UCELL")) {
									Attribute nodeBFunctionNameAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("NodeBFunctionName")));
									if (nodeBFunctionNameForSRan == null && nodeBFunctionNameAttribute != null) {
										nodeBFunctionNameForSRan = nodeBFunctionNameAttribute.getValue();
									}

									if (nodeBFunctionNameForSRan != null && !nodeBFunctionNameForSRan.isEmpty() && neId != null) {
										neIdByNodeBFunctionNameMap.put(nodeBFunctionNameForSRan.toUpperCase() + "_SRAN-BS", neId);
										nodeBFunctionNameByNeIdMap.put(neId, nodeBFunctionNameForSRan);
									}

									Attribute cellName = startElement.getAttributeByName(new QName("CellName"));
									if (cellName != null) {
										uCellNameSet.add(cellName.getValue());
									}
								}
								if (attrName.matches("(?i)GCELL")) {
									Attribute gbtsFunctionNameAttribute = startElement.getAttributeByName(new QName("GbtsFunctionName"));
									if (gbtsFunctionNameForSran == null && gbtsFunctionNameAttribute != null) {
										gbtsFunctionNameForSran = gbtsFunctionNameAttribute.getValue();
									}
									
									if( neId != null) {
										neIdByGbtsFunctionNameMap.put(neId, gbtsFunctionNameForSran);
										gbtsFunctionNameByneIdMap.put(neId, gbtsFunctionNameForSran);
									}
										
									Attribute cellName = startElement.getAttributeByName(new QName("CellName"));
									if (cellName != null) {
										gCellNameSet.add(cellName.getValue());
									}
								}
								if ((attrName.matches("(?i)LCELL") || attrName.matches("(?i)eNodeBCell"))  && !manufactureFixer.isMtcTouchImport()) {
									Attribute eNodeBFunctionNameAttribute = startElement.getAttributeByName(new QName("ENodeBFunctionName"));
									if (eNodeBFunctionNameForSRan == null && eNodeBFunctionNameAttribute != null) {
										eNodeBFunctionNameForSRan = eNodeBFunctionNameAttribute.getValue();
									}
									if (eNodeBFunctionNameForSRan != null && !eNodeBFunctionNameForSRan.isEmpty() && neId != null) {
										neIdByNodeBFunctionNameMap.put(eNodeBFunctionNameForSRan, neId);
										enodeBFunctionNameByneIdMap.put(neId, eNodeBFunctionNameForSRan);
									}
								}
								
								if (attrName.matches("(?i)HostVer")) {

									Attribute hostVerType = startElement
											.getAttributeByName(new QName("HostVerType"));
									
									if(hostVerType!=null && "HOSTVER".equalsIgnoreCase(hostVerType.getValue())){
										Attribute softwareVersionAttribute = startElement.getAttributeByName(new QName("HostVer"));
										if(softwareVersionAttribute!=null){
											hostVersion = softwareVersionAttribute.getValue();
										}
											
									}

								}

								fillNodebNameByCell4RncMap(startElement, neId);
							}
						}
					}

					if (neId == null)
						continue;

					if (gCellNameSet.size() > 0)
						cellNameByNeIdMap.put(neId.toUpperCase() + "_GCELL", gCellNameSet);

					if (uCellNameSet.size() > 0)
						cellNameByNeIdMap.put(neId.toUpperCase() + "_UCELL", uCellNameSet);
					
					Supported = extractSupported(neType);
					neModel = extractNeModel(softVersion, neType, neName);
					setModel4CurrentsCabinet(neModel);
					setModel4CurrentsShelf(neModel);
					setRuForCurrentShelfs(neType);
					setBoardTypeForUPIUboards(boardUPIUMap, subSlotNo2And3Map);
					filesList.put(neId, neFile);
//					if (boardExist){
					if(newIdMap.containsKey(neId))
						neId = newIdMap.get(neId);
					createNode(neId, neName, neType, type, neModel, site, Supported, neFile.getName(), nodeBFunctionNameForSRan, gbtsFunctionNameForSran, eNodeBFunctionNameForSRan, siteNo);
					
					ElementType elementtype = matchElementType(type, neType);
					
					if(elementtype != null) {
						if(StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.new.additional.info.parsing"), "false")) {
							
							if(hostVersion != null){
								ElementAdditionalInfo additionalInfo = TransmissionCommon.createAdditionalInformation(neId, elementtype, "SOFTWARE_VERSION", "SOFTWARE_VERSION", hostVersion);
								addInfoMap.put(String.join("_", neId, "SOFTWARE_VERSION"), additionalInfo);
							}
							
							if(elementtype != ElementType.SRanBS && elementtype != ElementType.BTS && elementtype != ElementType.NodeB && elementtype != ElementType.eNodeB) {
								if(neReportParser.getNeReportMap().containsKey(neName)){
									String creationDate = neReportParser.getNeReportMap().get(neName);
									if(creationDate!=null){
										creationDate = creationDate.split(" ")[0];
										ElementAdditionalInfo additionalInfo = TransmissionCommon.createAdditionalInformation(neId, elementtype, "INSTALLATION_DATE", "INSTALLATION_DATE", creationDate);
									    addInfoMap.put(String.join("_", neId, "INSTALLATION_DATE"), additionalInfo);
										
									}
								}
							}
						}
					
					}
					
					if(StringUtils.contains(neType, "BTS5900")) {
						bts5900nodes.add(neId);
					}
					
		
//					}
				} catch (Exception e) {
					log.error("Error : ", e);
				}
			}

			fillParentRnc();
			fillParentBsc();
			addNodeInterfaces();
			fillNeModel();
			updateSpecificBoardType();
			createRolesForSRanBs(discardedRanBsMap);
			updateRanBsTechnologies(discardedRanBsMap);
			if(StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.new.additional.info.parsing"), "false"))
				createRanNodeInstallationDate();
			
		//	createAntenna();
		}

		catch (Exception e) {
			log.error("Error : ", e);
		}

		log.debug("Finish of Huawei M2000_V2R0 Ran parsing from Huawei Files");
	}

	private void createRanNodeInstallationDate() {
		for(ElementBs node : btsMap.values()) {
			if(neReportParser.getNeReportMap().containsKey(node.getId())){
				String creationDate = neReportParser.getNeReportMap().get(node.getId());
				if(creationDate!=null){
					creationDate = creationDate.split(" ")[0];
					ElementAdditionalInfo additionalInfo = TransmissionCommon.createAdditionalInformation(node.getId(), ElementType.BTS, "INSTALLATION_DATE", "INSTALLATION_DATE", creationDate);
				    addInfoMap.put(String.join("_", node.getId(), "INSTALLATION_DATE"), additionalInfo);
					
				}
			}
		}
		
		for(ElementNb node : nodeBMap.values()) {
			if(neReportParser.getNeReportMap().containsKey(node.getId())){
				String creationDate = neReportParser.getNeReportMap().get(node.getId());
				if(creationDate!=null){
					creationDate = creationDate.split(" ")[0];
					ElementAdditionalInfo additionalInfo = TransmissionCommon.createAdditionalInformation(node.getId(), ElementType.NodeB, "INSTALLATION_DATE", "INSTALLATION_DATE", creationDate);
				    addInfoMap.put(String.join("_", node.getId(), "INSTALLATION_DATE"), additionalInfo);
					
				}
			}
		}
		
		for(ElementENodeB node : eNodeBMap.values()) {
			if(neReportParser.getNeReportMap().containsKey(node.getId())){
				String creationDate = neReportParser.getNeReportMap().get(node.getId());
				if(creationDate!=null){
					creationDate = creationDate.split(" ")[0];
					ElementAdditionalInfo additionalInfo = TransmissionCommon.createAdditionalInformation(node.getId(), ElementType.eNodeB, "INSTALLATION_DATE", "INSTALLATION_DATE", creationDate);
				    addInfoMap.put(String.join("_", node.getId(), "INSTALLATION_DATE"), additionalInfo);
					
				}
			}
		}
		
	}

	private void updateRanBsTechnologies(Map<String, Set<String>> discardedRanBsMap) {
		for(ElementRanBs ranBs : ranBsMap.values()) {
			
			Set<String> discardedids = discardedRanBsMap.get(ranBs.getId());
			
			Set<String> technologies = new TreeSet<String>();
			if(ranBs.getGsmCapability() != null)
				technologies.add("2G");
			if(ranBs.getUmtsCapability() != null)
				technologies.add("3G");
			if(ranBs.getLteCapability() != null)
				technologies.add("4G");
			
			if(StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.5g.parsing"), "false")) {
				if(ranBs.getNrCapability() != null)
					technologies.add("5G");
				
				
				if(technologies.isEmpty() && ranBs.getName().contains("5G"))
					technologies.add("5G");
				
				if(discardedids != null) {
					for(String discardedid : discardedids) {
						if(StringUtils.containsIgnoreCase(discardedid, "5G")) {
							technologies.add("5G");
							break;
						}
					}
				}
				
			}
			
			String supportedtch = String.join("/", technologies);
			ranBs.setTechnologySupported(supportedtch);
			
			if(StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.new.additional.info.parsing"), "true"))
				return;
			
			String creationDate = neReportParser.getNeReportMap().get(ranBs.getId());
			
			if(StringUtils.isBlank(creationDate) || StringUtils.equalsIgnoreCase(creationDate.trim(), "-")) {
				if(ranBs.getLteCapability() != null)
					creationDate = neReportParser.getNeReportMap().get(ranBs.getLteCapability().getId());
			}
			
			if(StringUtils.isBlank(creationDate) || StringUtils.equalsIgnoreCase(creationDate.trim(), "-")) {
				
				if(discardedids != null) {
					for(String discardedid : discardedids) {
						if(StringUtils.containsIgnoreCase(discardedid, "5G"))
							continue;
						creationDate = neReportParser.getNeReportMap().get(discardedid);
						if(StringUtils.isNotBlank(creationDate) && !StringUtils.equalsIgnoreCase(creationDate.trim(), "-")) 
							break;
					}
				}
			}
			
			if(StringUtils.isBlank(creationDate) || StringUtils.equalsIgnoreCase(creationDate.trim(), "-")) {
				if(ranBs.getNrCapability() != null)
					creationDate = neReportParser.getNeReportMap().get(ranBs.getNrCapability().getId());
			}
			
			if(StringUtils.isNotBlank(creationDate)) {
				creationDate = creationDate.split(" ")[0];
				ElementAdditionalInfo additionalInfo = TransmissionCommon.createAdditionalInformation(ranBs.getId(), ElementType.SRanBS, "INSTALLATION_DATE", "INSTALLATION_DATE", creationDate);
			    addInfoMap.put(String.join("_", ranBs.getId(), "INSTALLATION_DATE"), additionalInfo);
			}
			
		}
		
	}

	private void createHostVer(String name, StartElement startElement, String neType, String type, String neId) throws ParseException {
		
		if(!isRanBs(neType))
			return;
		
		String hostVerType = null;
		String hostVerValue = null;
		
		
		Attribute hostVerTypeNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("hostVerType")));
		if(hostVerTypeNbAttribute != null)
			hostVerType = hostVerTypeNbAttribute.getValue();
		
		
		Attribute hostVerValueNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("hostVer")));
		if (hostVerValueNbAttribute != null) 
			hostVerValue = hostVerValueNbAttribute.getValue();
		
		String category = null;
		String attribute = null;
		
		if(StringUtils.equalsIgnoreCase(hostVerType, "GBTSFunctionHOSTVER") || StringUtils.equalsIgnoreCase(hostVerType, "NodeBFunctionHOSTVER") || StringUtils.equalsIgnoreCase(hostVerType, "eNodeBFunctionHOSTVER")
				|| StringUtils.equalsIgnoreCase(hostVerType, "gNodeBFunctionHOSTVER")) {
			category = "SOFTWARE_VERSION";
			if(StringUtils.equalsIgnoreCase(hostVerType, "GBTSFunctionHOSTVER"))
				attribute = String.join("_", category, "2G");
			else if(StringUtils.equalsIgnoreCase(hostVerType, "NodeBFunctionHOSTVER"))
				attribute = String.join("_", category, "3G");
			else if(StringUtils.equalsIgnoreCase(hostVerType, "eNodeBFunctionHOSTVER"))
				attribute = String.join("_", category, "4G");
			
			else if(StringUtils.equalsIgnoreCase(hostVerType, "gNodeBFunctionHOSTVER"))
				attribute = String.join("_", category, "5G");
		}
		
		else if(StringUtils.equalsIgnoreCase(hostVerType, "GBTSFunctionBACKVER") || StringUtils.equalsIgnoreCase(hostVerType, "NodeBFunctionBACKVER") || StringUtils.equalsIgnoreCase(hostVerType, "eNodeBFunctionBACKVER")
				 || StringUtils.equalsIgnoreCase(hostVerType, "gNodeBFunctionBACKVER")) {
			category = "BACKUP_SOFTWARE_VERSION";
			if(StringUtils.equalsIgnoreCase(hostVerType, "GBTSFunctionBACKVER"))
				attribute = String.join("_", category, "2G");
			else if(StringUtils.equalsIgnoreCase(hostVerType, "NodeBFunctionBACKVER"))
				attribute = String.join("_", category, "3G");
			else if(StringUtils.equalsIgnoreCase(hostVerType, "eNodeBFunctionBACKVER"))
				attribute = String.join("_", category, "4G");
			else if(StringUtils.equalsIgnoreCase(hostVerType, "gNodeBFunctionBACKVER"))
				attribute = String.join("_", category, "5G");
		}
		
		
		if(StringUtils.isBlank(category) || StringUtils.isBlank(attribute) || StringUtils.isBlank(hostVerValue))
			return;
		
		String key = TransmissionCommon.concatenateStrings("_", neId, category, attribute);
		ElementAdditionalInfo additionalInfo = TransmissionCommon.createAdditionalInformation(neId, ElementType.SRanBS, category, attribute, hostVerValue);
		addInfoMap.put(key, additionalInfo);
		
		
	}

	public void listf(File directory, List<File> files) {
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				files.add(file);
			} else if (file.isDirectory()) {
				listf(file, files);
			}
		}
	}

	/**
	 * 
	 */
	private void initMap() {

		summaryManager = new SummaryReportManagement(TransmissionCommon.HUAWEI_M2000_V2R0EN0SP0);
		
		antennaPartMap = new HashMap<String, List<Antenna>>();
		antennaMap = new HashMap<String, ElementRadioAntenna>();
		nodeSlotMap = new HashMap<String, NodeSlot>();
		mapCabinet = new HashMap<String, NodeCabinet>();
		mapShelf = new HashMap<String, NodeShelf>();
		nodeBoardMap = new HashMap<String, NodeBoard>();
		parentRncMap = new HashMap<String, String>();
		parentBscMap = new HashMap<String, String>();
		nodeInterfaceMap = new HashMap<String, NodeInterface>();
		boardId4NodeInterfaceMap = new HashMap<String, String>();
		neModel4BtsAndSRanBsMap = new HashMap<String, String>();
		btsNameBySiteIndex = new HashMap<String, String>();
		neModel4NodeBMap = new HashMap<String, String>();
		neModel4EnodeBMap = new HashMap<String, String>();
		nodeBoardsForMatching = new HashMap<String, NodeBoard>();
		specificBoardMatchingMap = new HashMap<String, String>();
		nodeTypeByNeIdMap = new HashMap<String, String>();
		nodeBNameByCellIdByRnc = new HashMap<String, Map<String, String>>();
		map4SpuBoards = new HashMap<String, NodeSlot>();
		elementTrauMap = new HashMap<String, ElementTrau>();
		bbu3900SerialNbMap = new HashMap<String, Set<String>>();
		neTypeBbu3900SerialNbMap = new HashMap<String, Set<String>>();
		virtualMicroSerialNbMap = new HashMap<String, Set<String>>();
		neTypeVirtualMicroSerialNbMap = new HashMap<String, Set<String>>();
		bts3900SerialNb = new HashSet<String>();
		microBts3900SerialNb = new HashSet<String>();
		cabinetsIdByNeIdMap = new HashMap<String, Set<String>>();
		shelfsIdByNeIdMap = new HashMap<String, Set<String>>();
		slotsIdByNeIdMap = new HashMap<String, Set<String>>();
		ranBsIdByRoleIdMap = new HashMap<String, String>();
		btsNameByNodebNameRelationMap = new HashMap<String, String>();
		bts3900NameByBtsNameRelationMap = new HashMap<String, String>();
		bts3900NameByNodeBNameRelationMap = new HashMap<String, String>();
		microBts3900NameByBtsNameRelationMap = new HashMap<String, String>();
		microBts3900NameByNodeBNameRelationMap = new HashMap<String, String>();
		enodeBNameByNodeBNameRelationMap = new HashMap<String, String>();
		nodeBFunctionNameByNeIdMap = new HashMap<String, String>();
		neIdByGbtsFunctionNameMap = new HashMap<String, String>();
		neIdByNodeBFunctionNameMap = new HashMap<String, String>();
		fileNameByNodeIdMap = new HashMap<String, String>();
		nodeSlotForMatching = new HashMap<String, NodeSlot>();
		nodeNameMap = new HashMap<String, String>();
		cellNameByNeIdMap = new HashMap<String, Set<String>>();
		enodeBFunctionNameByneIdMap = new HashMap<String, String>();
		gbtsFunctionNameByneIdMap = new HashMap<String, String>();
        subrackMap =  new HashMap<String, String>();
		sitesByNameAndObjId = new HashMap<String, Site>();
		ranbsRelationMap = new HashMap<String, String>();
		bts5900nodes = new HashSet<String>();
		for (Site site : NodeContainer.getSites().values()) {
			sitesByNameAndObjId.put(site.getName().toUpperCase(), site);
			sitesByNameAndObjId.put(site.getObjectId().toUpperCase(), site);
		}

		rncMap = new HashMap<String, ElementRnc>();
		nodeBMap = new HashMap<String, ElementNb>();
		btsMap = new HashMap<String, ElementBs>();
		bscMap = new HashMap<String, ElementBsc>();
		eNodeBMap = new HashMap<String, ElementENodeB>();
		ranBsMap = new HashMap<String, ElementRanBs>();
		ggsnMap = new HashMap<String, ElementGGSN>();
		sgsnMap = new HashMap<String, ElementSgsn>();
		sRanControllerMap = new HashMap<String, ElementSranController>();
		mgwMap = new HashMap<String, ElementMgw>();
		mscMap = new HashMap<String, ElementMsc>();
		hlrMap = new HashMap<String, ElementHlr>();
		cgpMap = new HashMap<String, ElementCgp>();
		sessionEngineMap = new HashMap<String, ElementSessionEngine>();
		cgMap = new HashMap<String, ElementCg>();
		ipclkMap = new HashMap<String, ElementIpclk>();
		ugwMap = new HashMap<String, ElementUgw>();
		coreElementMap = new HashMap<String, ElementCoreElement>();
		mssMap = new HashMap<String, ElementMss>();
		mmeMap = new HashMap<String, ElementMme>();
		sgwMap = new HashMap<String, ElementSgw>();
		networkElementDumps = new HashMap<String, NetworkElementDump>();
		filesList = new HashMap<String, File>();
		hssMap = new HashMap<String, ElementHss>();
		nodesWithNoSerials = new HashSet<String>();
		addInfoMap = new HashMap<String, ElementAdditionalInfo>();
		newIdMap = m2v2r0ranUniqueidParser.getNewObjectId();
	}

	/**
	 * 
	 * @param startElement
	 * @param neId
	 */
	private void fillNodeBFuctionNameByNodeName(StartElement startElement, String neId) {

		String nodeBName = null;

		Attribute nodeBFunctionNameAttribute = startElement
				.getAttributeByName(new QName(m2v2r0Patterns.get("NodeBFunctionName")));
		if (nodeBFunctionNameAttribute != null) {
			nodeBName = nodeBFunctionNameAttribute.getValue();
		}

		if (nodeBName != null && !nodeBName.isEmpty() && neId != null) {
			if (nodeBFunctionNameByNeIdMap.get(neId) == null)
				nodeBFunctionNameByNeIdMap.put(neId, nodeBName);

			neIdByNodeBFunctionNameMap.put(nodeBName.toUpperCase() + "_SRAN-BS", neId);
		}
	}

	/**
	 * 
	 * @param startElement
	 * @param neId
	 */
	private void fillNodebNameByCell4RncMap(StartElement startElement, String neId) {
		String cellId = null;
		String nodeBName = null;

		Attribute cellIdAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("CellId")));
		if (cellIdAttribute != null) {
			cellId = cellIdAttribute.getValue();
		}

		Attribute nodebNameAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("NodeBName")));
		if (nodebNameAttribute != null) {
			nodeBName = nodebNameAttribute.getValue();
		}
		if (cellId != null && nodeBName != null && !cellId.isEmpty() && !nodeBName.isEmpty()) {

			Map<String, String> nodeBNameByCellIdForRnc = nodeBNameByCellIdByRnc.get(neId.toUpperCase());
			if (nodeBNameByCellIdForRnc == null) {
				nodeBNameByCellIdForRnc = new HashMap<String, String>();
			}
			nodeBNameByCellIdForRnc.put(cellId, nodeBName);
			nodeBNameByCellIdByRnc.put(neId.toUpperCase(), nodeBNameByCellIdForRnc);
		}
	}

	/**
	 * @param discardedRanBsMap 
	 * 
	 */

	private void createRolesForSRanBs(Map<String, Set<String>> discardedRanBsMap) {
		createRoles(discardedRanBsMap);
	}

	/**
	 * 
	 * @param neId
	 * @param site
	 * @return
	 */
	private String getMatchingSite(String neId, String site) {
		String siteName;
		if (site != null && !site.isEmpty()) {
			Site siteObj = sitesByNameAndObjId.get(site.toUpperCase());
			if (siteObj != null) {
				return siteObj.getObjectId();
			}
			try {
				siteObj = sitesByNameAndObjId.get(String.valueOf(Integer.parseInt(site)).toUpperCase());
				if (siteObj != null) {
					return siteObj.getObjectId();
				}
			} catch (Exception e) {
			}

			String matchingKey = site;
			if (manufactureFixer.isVodacomImport()) {
				matchingKey = matchingKey + TransmissionCommon.SEPERATOR + TransmissionCommon.HUAWEI;
			}
			siteName = siteMatchFounder.tryToMatchSite(matchingKey);
			if (siteName != null && !siteName.equalsIgnoreCase(matchingKey)) {
				return siteName;
			}
		}
		String matchingKey = neId;
		if (manufactureFixer.isVodacomImport()) {
			matchingKey = matchingKey + TransmissionCommon.SEPERATOR + TransmissionCommon.HUAWEI;
		}
		siteName = siteMatchFounder.tryToMatchSite(matchingKey);
		if (siteName != null && siteName.equalsIgnoreCase(matchingKey) && site != null && !site.isEmpty()) {
			siteName = site;
		} else
			siteName = site;
		return siteName;
	}

	/**
	 * 
	 */
	public void fillParentRnc() {
		for (ElementNb nodeB : nodeBMap.values()) {
			if (nodeB.getName() != null)
				nodeB.setParentRnc(parentRncMap.get(nodeB.getName()));
		}

		for (ElementRanBs ranBs : ranBsMap.values()) {
			if (ranBs.getUmtsCapability() != null && ranBs.getUmtsCapability().getName() != null) {
				ranBs.getUmtsCapability().setParentRnc(parentRncMap.get(ranBs.getUmtsCapability().getName()));
			}
		}
	}

	/**
	 * 
	 */
	public void fillParentBsc() {
		for (ElementBs bts : btsMap.values()) {
			if (bts.getName() != null)
				bts.setParentBsc(parentBscMap.get(bts.getName()));
		}
		for (ElementRanBs ranBs : ranBsMap.values()) {
			if (ranBs.getGsmCapability() != null && ranBs.getGsmCapability().getName() != null) {
				ranBs.getGsmCapability().setParentBsc(parentBscMap.get(ranBs.getGsmCapability().getName()));
			}
		}
	}

	/**
	 * 
	 */
	public void fillNeModel() {

		List<String> bts4Remove = new ArrayList<String>();
		List<String> nodeB4Remove = new ArrayList<String>();
		List<String> eNodeB4Remove = new ArrayList<String>();
		List<String> ranBs4Remove = new ArrayList<String>();

		for (ElementBs bts : btsMap.values()) {
			if (bts.getType() == null) {
				if (neModel4BtsAndSRanBsMap.get(bts.getName()) == null
						|| neModel4BtsAndSRanBsMap.get(bts.getName()).isEmpty()) {
					if (!manufactureFixer.isMeditel3xImport()
							|| (mbtsRelationMatchingParser.findMatchByGmbtsId(bts.getName()) == null)) {

						String fileName = fileNameByNodeIdMap.get(bts.getId().toUpperCase() + "_BTS");
						String message = "Undefined Model: ID=" + bts.getId() + ", Site=" + bts.getSite()
								+ ", Manufacturer = " + TransmissionCommon.HUAWEI_M2000_V2R0EN0SP0 + ", Model=N/A";
						addErrorAlarm(message, fileName, Category.UNDEFINED_MODEL, NptConstants.BTS);

						bts4Remove.add(bts.getId());
					}
				} else
					bts.setType(nodesMatchingParser.getNeModel(neModel4BtsAndSRanBsMap.get(bts.getName())));
			}
		}

		for (ElementNb nodeB : nodeBMap.values()) {
			if (nodeB.getType() == null) {
				if (neModel4NodeBMap.get(nodeB.getName()) == null || neModel4NodeBMap.get(nodeB.getName()).isEmpty()) {
					if (!manufactureFixer.isMeditel3xImport()
							|| (mbtsRelationMatchingParser.findMatchByBts3900Id(nodeB.getName()) == null)) {
						String fileName = fileNameByNodeIdMap.get(nodeB.getId().toUpperCase() + "_NODEB");
						String message = "Undefined Model: ID=" + nodeB.getId() + ", Site=" + nodeB.getSite()
								+ ", Manufacturer = " + TransmissionCommon.HUAWEI_M2000_V2R0EN0SP0 + ", Model=N/A";
						addErrorAlarm(message, fileName, Category.UNDEFINED_MODEL, NptConstants.NODE_B);

						nodeB4Remove.add(nodeB.getId());
					}
				} else
					nodeB.setType(nodesMatchingParser.getNeModel(neModel4NodeBMap.get(nodeB.getName())));
			}
		}

		for (ElementENodeB enodeB : eNodeBMap.values()) {
			if (enodeB.getType() == null) {
				if (neModel4EnodeBMap.get(enodeB.getName()) == null
						|| neModel4EnodeBMap.get(enodeB.getName()).isEmpty()) {
					String fileName = fileNameByNodeIdMap.get(enodeB.getId().toUpperCase() + "_ENODEB");
					String message = "Undefined Model: ID=" + enodeB.getId() + ", Site=" + enodeB.getSite()
							+ ", Manufacturer = " + TransmissionCommon.HUAWEI_M2000_V2R0EN0SP0 + ", Model=N/A";
					addErrorAlarm(message, fileName, Category.UNDEFINED_MODEL, NptConstants.ENODEB);

					eNodeB4Remove.add(enodeB.getId());
				} else
					enodeB.setType(nodesMatchingParser.getNeModel(neModel4EnodeBMap.get(enodeB.getName())));
			}
		}

		for (ElementRanBs ranBs : ranBsMap.values()) {
			if (ranBs.getType() == null) {
				if (!manufactureFixer.isMeditel3xImport() || !mbtsRelationMatchingParser.mbtsExist(ranBs.getName())) {
					String ranBsName = null;

					String fileName = fileNameByNodeIdMap.get(ranBs.getId().toUpperCase() + "_RANBS");

					Matcher ranBsNameMatcher = Pattern.compile(m2v2r0Patterns.get("neNamePat4Mbts"))
							.matcher(ranBs.getName());
					if (ranBsNameMatcher.find()) {
						ranBsName = ranBsNameMatcher.group(2);

						if (neModel4EnodeBMap.get(ranBsName) == null || neModel4EnodeBMap.get(ranBsName).isEmpty()) {
							String message = "Undefined Model: ID=" + ranBs.getId() + ", Site=" + ranBs.getSite()
									+ ", Manufacturer = " + TransmissionCommon.HUAWEI_M2000_V2R0EN0SP0 + ", Model=N/A";
							addErrorAlarm(message, fileName, Category.UNDEFINED_MODEL, NptConstants.RAN_BS);

							ranBs4Remove.add(ranBs.getId());
						} else
							ranBs.setType(nodesMatchingParser.getNeModel(neModel4EnodeBMap.get(ranBsName)));
					} else {
						String message = "Undefined Model: ID=" + ranBs.getId() + ", Site=" + ranBs.getSite()
								+ ", Manufacturer = " + TransmissionCommon.HUAWEI_M2000_V2R0EN0SP0 + ", Model=N/A";
						addErrorAlarm(message, fileName, Category.UNDEFINED_MODEL, NptConstants.RAN_BS);

						ranBs4Remove.add(ranBs.getId());
					}
				}
			}
		}

		for (String btsId : bts4Remove) {
			btsMap.remove(btsId);
		}

		for (String nodeBId : nodeB4Remove) {
			nodeBMap.remove(nodeBId);
		}

		for (String eNodeBId : eNodeB4Remove) {
			eNodeBMap.remove(eNodeBId);
		}

		for (String ranBsId : ranBs4Remove) {
			ranBsMap.remove(ranBsId);
		}
	}

	/**
	 * \
	 * 
	 * @param startElement
	 * @param neType
	 * @return
	 */
	public String extractSiteNo(StartElement startElement, String neType) {

		String siteNo = null;

		if (neType != null && (isNeTypeMatched(neType, m2v2r0Patterns.get("nodeB"))
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsUl")) || isNeTypeMatched(neType, "(?i)MBTS"))) {
			// get site
			Attribute siteAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("siteNo")));
			if (siteAttribute != null) {
				siteNo = siteAttribute.getValue();
			}
		}

		return siteNo;
	}

	/**
	 * @param neId
	 * @param neName
	 * @param type
	 * @param neModel
	 * @param site
	 * @param fileName
	 */
	public void createNode(String neId, String neName, String neType, String type, String neModel, String site,
			String Supported, String fileName, String nodeBFunctionNameForSRan,
			String gbtsFunctionNameForSRan, String eNodeBFunctionNameForSRan, String siteNo) {

		if (isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu")) || isNeTypeMatched(neType, "(?i)BSC6910GU") ) {
			createNodeForTypeBcs6900Gu(neId, neName, site, type, neModel, fileName, siteNo);
		} else if (isNeTypeMatched(neType, m2v2r0Patterns.get("bts3900"))) {
			createNodeForTypeBts3900(neId, neName, site, type, neModel, fileName, nodeBFunctionNameForSRan,
					gbtsFunctionNameForSRan, eNodeBFunctionNameForSRan, Supported, siteNo);
		} 
		else if (isNeTypeMatched(neType, "(?i)MICROBTS3900") || isNeTypeMatched(neType, "(?i)PICOBTS3900")) {
			createNodeForTypeMicroBts3900(neId, neName, site, type, neModel, fileName, nodeBFunctionNameForSRan,
					gbtsFunctionNameForSRan, eNodeBFunctionNameForSRan, Supported, siteNo);
		}
		else if (isNeTypeMatched(neType, "(?i)BTS5900") || isNeTypeMatched(neType, "(?i)BTS59005G")) {
			
			if(StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.5g.parsing"), "false")) {
				createNodeForTypeBts5900(neId, neName, site, type, neModel, fileName, nodeBFunctionNameForSRan,
					gbtsFunctionNameForSRan, eNodeBFunctionNameForSRan, Supported, siteNo);
			}
		}
		else if (type != null) {
			if (nodeTypeByNeIdMap.get(neId) == null)
				nodeTypeByNeIdMap.put(neId, type);

			if (type.matches(m2v2r0Patterns.get("rnc"))) {
				if (neModel != null) {
					if (rncMap.get(neId) == null)
						rncMap.put(neId, (ElementRnc) addNode(new ElementRnc(), neId, neName, neModel, site, null,
								siteNo, PossibleNodeType.RNC));
					else
						createDuplicatedNodeError(neId, site, type, neModel, NptConstants.RNC, fileName);
				} else {
					String message = "Undefined Model: ID=" + neId + ", Site=" + site + ", Manufacturer = "
							+ TransmissionCommon.HUAWEI_M2000_V2R0EN0SP0 + ", Model=N/A";
					addErrorAlarm(message, fileName, Category.UNDEFINED_MODEL, type);
				}
			}

			else if (type.matches(m2v2r0Patterns.get("nodeB"))) {
				if (nodeBMap.get(neId) == null) {
					nodeBMap.put(neId, (ElementNb) addNode(new ElementNb(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.NodeB));
					fileNameByNodeIdMap.put(neId.toUpperCase() + "_NODEB", fileName);
				} else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.NODEB, fileName);
			}

			else if (type.matches(m2v2r0Patterns.get("bts"))) {
				if (btsMap.get(neId) == null) {
					btsMap.put(neId, (ElementBs) addNode(new ElementBs(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.BTS));
					fileNameByNodeIdMap.put(neId.toUpperCase() + "_BTS", fileName);
				} else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.BTS, fileName);
			}

			else if (type.matches(m2v2r0Patterns.get("bsc"))) {
				if (neModel != null) {
					if (bscMap.get(neId) == null)
						bscMap.put(neId, (ElementBsc) addNode(new ElementBsc(), neId, neName, neModel, site, null,
								siteNo, PossibleNodeType.BSC));
					else
						createDuplicatedNodeError(neId, site, type, neModel, NptConstants.BSC, fileName);
				} else {
					String message = "Undefined Model: ID=" + neId + ", Site=" + site + ", Manufacturer = "
							+ TransmissionCommon.HUAWEI_M2000_V2R0EN0SP0 + ", Model=N/A";
					addErrorAlarm(message, fileName, Category.UNDEFINED_MODEL, type);
				}
			}

			else if (type.matches(m2v2r0Patterns.get("enodeB"))) {
				if (eNodeBMap.get(neId) == null) {
					eNodeBMap.put(neId, (ElementENodeB) addNode(new ElementENodeB(), neId, neName, neModel, site, null,
							siteNo, PossibleNodeType.eNodeB));
					fileNameByNodeIdMap.put(neId.toUpperCase() + "_ENODEB", fileName);
				} else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.ENODEB, fileName);
			}

			else if (type.matches(m2v2r0Patterns.get("ranBs"))) {
				if (ranBsMap.get(neId) == null) {
					ranBsMap.put(neId, (ElementRanBs) addNode(new ElementRanBs(), neId, neName, neModel, site,
							Supported, siteNo, PossibleNodeType.RanBs));
					fileNameByNodeIdMap.put(neId.toUpperCase() + "_RANBS", fileName);
				} else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.RAN_BS, fileName);
			} else if (type.matches(m2v2r0Patterns.get("ggsn"))) {
				if (ggsnMap.get(neId) == null)
					ggsnMap.put(neId, (ElementGGSN) addNode(new ElementGGSN(), neId, neName, neModel, site, null,
							siteNo, PossibleNodeType.GGSN));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.GGSN, fileName);
			} else if (type.matches(m2v2r0Patterns.get("sgsn"))) {
				if (sgsnMap.get(neId) == null)
					sgsnMap.put(neId, (ElementSgsn) addNode(new ElementSgsn(), neId, neName, neModel, site, null,
							siteNo, PossibleNodeType.SGSN));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.SGSN, fileName);
			} else if (type.matches(m2v2r0Patterns.get("mgw"))) {
				if (mgwMap.get(neId) == null)
					mgwMap.put(neId, (ElementMgw) addNode(new ElementMgw(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.MGW));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.MGW, fileName);
			} else if (type.matches(m2v2r0Patterns.get("msc"))) {
				if (mscMap.get(neId) == null)
					mscMap.put(neId, (ElementMsc) addNode(new ElementMsc(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.MSC));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.MSC, fileName);
			} else if (type.matches(m2v2r0Patterns.get("hlr"))) {
				if (hlrMap.get(neId) == null)
					hlrMap.put(neId, (ElementHlr) addNode(new ElementHlr(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.HLR));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.HLR, fileName);
			} else if (type.matches(m2v2r0Patterns.get("cgp"))) {
				if (cgpMap.get(neId) == null)
					cgpMap.put(neId, (ElementCgp) addNode(new ElementCgp(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.CGP));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.CGP, fileName);
			} else if (type.matches(m2v2r0Patterns.get("sessionEngine"))) {
				if (sessionEngineMap.get(neId) == null)
					sessionEngineMap.put(neId, (ElementSessionEngine) addNode(new ElementSessionEngine(), neId, neName,
							neModel, site, null, siteNo, null));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.MSC, fileName);
			} else if (type.matches(m2v2r0Patterns.get("cg"))) {
				if (cgMap.get(neId) == null)
					cgMap.put(neId,
							(ElementCg) addNode(new ElementCg(), neId, neName, neModel, site, null, siteNo, null));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.MSC, fileName);
			} else if (type.matches(m2v2r0Patterns.get("IPCLK"))) {
				if (ipclkMap.get(neId) == null)
					ipclkMap.put(neId, (ElementIpclk) addNode(new ElementIpclk(), neId, neName, neModel, site, null,
							siteNo, PossibleNodeType.IPCLK));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.IPCLK, fileName);
			} else if (type.matches(m2v2r0Patterns.get("ugw"))) {
				if (ugwMap.get(neId) == null)
					ugwMap.put(neId,
							(ElementUgw) addNode(new ElementUgw(), neId, neName, neModel, site, null, siteNo, null));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.MSC, fileName);
			} else if (type.matches(m2v2r0Patterns.get("CoreElement"))) {
				if (coreElementMap.get(neId) == null)
					coreElementMap.put(neId, (ElementCoreElement) addNode(new ElementCoreElement(), neId, neName,
							neModel, site, null, siteNo, PossibleNodeType.COREELEMENT));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.CORE_ELEMENT, fileName);
			} else if (type.matches(m2v2r0Patterns.get("mss"))) {
				if (mssMap.get(neId) == null)
					mssMap.put(neId, (ElementMss) addNode(new ElementMss(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.MSS));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.MSS, fileName);
			} else if (type.matches(m2v2r0Patterns.get("mme")) && !manufactureFixer.isMtcTouchImport()) {
				if (mmeMap.get(neId) == null)
					mmeMap.put(neId, (ElementMme) addNode(new ElementMme(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.MME));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.MME, fileName);
			} else if (type.matches(m2v2r0Patterns.get("sgw")) && !manufactureFixer.isMtcTouchImport()) {
				if (sgwMap.get(neId) == null)
					sgwMap.put(neId, (ElementSgw) addNode(new ElementSgw(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.SGW));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.SGW, fileName);
			} else if (type.equalsIgnoreCase(NptConstants.HSS.toString()) && !manufactureFixer.isMtcTouchImport()) {
				if (hssMap.get(neId) == null)
					hssMap.put(neId, (ElementHss) addNode(new ElementHss(), neId, neName, neModel, site, null, siteNo,
							PossibleNodeType.HSS));
				else
					createDuplicatedNodeError(neId, site, type, neModel, NptConstants.HSS, fileName);
			}
		}
	}
	
	private void createNodeForTypeBts5900(String neId, String neName, String site, String type, String neModel,
			String fileName, String nodeBFunctionNameForSRan, String gbtsFunctionNameForSRan,
			String enodeBFunctionNameForSRan, String Supported, String siteNo) {

		if (ranBsMap.get(neId) == null) {
			ElementRanBs ranBs = (ElementRanBs) addNode(new ElementRanBs(), neId, neName, neModel, site,
					Supported, siteNo, PossibleNodeType.RanBs);
	
			ranBsMap.put(neId, ranBs);
		} else
			createDuplicatedNodeError(neId, site, type, neModel, NptConstants.RAN_BS, fileName);
	}


	/**
	 * 
	 * @param neId
	 * @param neName
	 * @param site
	 * @param type
	 * @param neModel
	 * @param fileName
	 */
	private void createNodeForTypeBts3900(String neId, String neName, String site, String type, String neModel,
			String fileName, String nodeBFunctionNameForSRan, String gbtsFunctionNameForSRan,
			String enodeBFunctionNameForSRan, String Supported, String siteNo) {

		ElementNb elementNodeB = (ElementNb) addNode(new ElementNb(), nodeBFunctionNameForSRan,
				nodeBFunctionNameForSRan, neModel, site, null, siteNo, PossibleNodeType.NodeB);
		ElementBs elementBs = (ElementBs) addNode(new ElementBs(), gbtsFunctionNameForSRan, gbtsFunctionNameForSRan,
				neModel, site, null, siteNo, PossibleNodeType.BTS);
		ElementENodeB elementEnodeB = (ElementENodeB) addNode(new ElementENodeB(), enodeBFunctionNameForSRan,
				enodeBFunctionNameForSRan, neModel, site, null, siteNo, PossibleNodeType.eNodeB);

		if (ranBsMap.get(neId) == null) {
			ElementRanBs ranBs = (ElementRanBs) addNode(new ElementRanBs(), neId, neName, neModel, site,
					Supported, siteNo, PossibleNodeType.RanBs);
			if (gbtsFunctionNameForSRan != null && !gbtsFunctionNameForSRan.isEmpty())
				ranBs.setGsmCapability(elementBs);
			if (nodeBFunctionNameForSRan != null && !nodeBFunctionNameForSRan.isEmpty())
				ranBs.setUmtsCapability(elementNodeB);
			if (enodeBFunctionNameForSRan != null && !enodeBFunctionNameForSRan.isEmpty())
				ranBs.setLteCapability(elementEnodeB);

			if (!manufactureFixer.isMtcTouchImport()) {
				if (nodeBFunctionNameForSRan != null && !nodeBFunctionNameForSRan.isEmpty())
					ranBsIdByRoleIdMap.put(nodeBFunctionNameForSRan.toUpperCase(), neId);
				if (gbtsFunctionNameForSRan != null && !gbtsFunctionNameForSRan.isEmpty())
					ranBsIdByRoleIdMap.put(gbtsFunctionNameForSRan.toUpperCase(), neId);
				if (enodeBFunctionNameForSRan != null && !enodeBFunctionNameForSRan.isEmpty())
					ranBsIdByRoleIdMap.put(enodeBFunctionNameForSRan.toUpperCase(), neId);
			}
			ranBsMap.put(neId, ranBs);
		} else
			createDuplicatedNodeError(neId, site, type, neModel, NptConstants.RAN_BS, fileName);
	}

	/**
	 * 
	 * @param neId
	 * @param neName
	 * @param site
	 * @param type
	 * @param neModel
	 * @param fileName
	 */
	private void createNodeForTypeMicroBts3900(String neId, String neName, String site, String type, String neModel,
			String fileName, String nodeBFunctionNameForSRan, String gbtsFunctionNameForSRan,
			String enodeBFunctionNameForSRan, String Supported, String siteNo) {

		ElementNb elementNodeB = (ElementNb) addNode(new ElementNb(), nodeBFunctionNameForSRan,
				nodeBFunctionNameForSRan, neModel, site, null, siteNo, PossibleNodeType.NodeB);
		ElementBs elementBs = (ElementBs) addNode(new ElementBs(), gbtsFunctionNameForSRan, gbtsFunctionNameForSRan,
				neModel, site, null, siteNo, PossibleNodeType.BTS);
		ElementENodeB elementEnodeB = (ElementENodeB) addNode(new ElementENodeB(), enodeBFunctionNameForSRan,
				enodeBFunctionNameForSRan, neModel, site, null, siteNo, PossibleNodeType.eNodeB);

		if (ranBsMap.get(neId) == null) {
			ElementRanBs ranBs = (ElementRanBs) addNode(new ElementRanBs(), neId, neName, neModel, site,
					Supported, siteNo, PossibleNodeType.RanBs);
			if (gbtsFunctionNameForSRan != null && !gbtsFunctionNameForSRan.isEmpty())
				ranBs.setGsmCapability(elementBs);
			if (nodeBFunctionNameForSRan != null && !nodeBFunctionNameForSRan.isEmpty())
				ranBs.setUmtsCapability(elementNodeB);
			if (enodeBFunctionNameForSRan != null && !enodeBFunctionNameForSRan.isEmpty())
				ranBs.setLteCapability(elementEnodeB);

			
				if (nodeBFunctionNameForSRan != null && !nodeBFunctionNameForSRan.isEmpty())
					ranBsIdByRoleIdMap.put(nodeBFunctionNameForSRan.toUpperCase(), neId);
				if (gbtsFunctionNameForSRan != null && !gbtsFunctionNameForSRan.isEmpty())
					ranBsIdByRoleIdMap.put(gbtsFunctionNameForSRan.toUpperCase(), neId);
				if (enodeBFunctionNameForSRan != null && !enodeBFunctionNameForSRan.isEmpty())
					ranBsIdByRoleIdMap.put(enodeBFunctionNameForSRan.toUpperCase(), neId);
			
			ranBsMap.put(neId, ranBs);
		} else
			createDuplicatedNodeError(neId, site, type, neModel, NptConstants.RAN_BS, fileName);
	}
	/**
	 * 
	 * @param neId
	 * @param neName
	 * @param site
	 * @param type
	 * @param neModel
	 * @param fileName
	 */
	private void createNodeForTypeBcs6900Gu(String neId, String neName, String site, String type, String neModel,
			String fileName, String siteNo) {

		// Rnc node
		String rncNeModel = m2v2r0Patterns.get("bsc6900") + "_" + m2v2r0Patterns.get("umts");
		ElementRnc elementRnc = (ElementRnc) addNode(new ElementRnc(), neId, neName, rncNeModel, site, null, siteNo,
				PossibleNodeType.RNC);

		// Bsc node
		String bscNeModel = m2v2r0Patterns.get("bsc6900") + "_" + m2v2r0Patterns.get("gsm");
		ElementBsc elementBsc = (ElementBsc) addNode(new ElementBsc(), neId, neName, bscNeModel, site, null, siteNo,
				PossibleNodeType.BSC);

		if (manufactureFixer.isSyriatelImport()) {
			if (sRanControllerMap.get(neId.toUpperCase()) == null) {
				ElementSranController sRanController = (ElementSranController) addNode(new ElementSranController(),
						neId, neName, neModel, site, null, siteNo, PossibleNodeType.SRanController);
				sRanController.setGsmCapability(elementBsc);
				sRanController.setUmtsCapability(elementRnc);

				sRanControllerMap.put(neId.toUpperCase(), sRanController);
			} else
				createDuplicatedNodeError(neId, site, type, neModel, NptConstants.SRANCONTROLLER, fileName);
		} else {
			if (rncMap.get(neId) == null)
				rncMap.put(neId, elementRnc);
			else
				createDuplicatedNodeError(neId, site, "RNC", rncNeModel, NptConstants.RNC, fileName);

			if (bscMap.get(neId) == null)
				bscMap.put(neId, elementBsc);
			else
				createDuplicatedNodeError(neId, site, "BSC", bscNeModel, NptConstants.BSC, fileName);
		}
	}

	/**
	 * 
	 * @param neId
	 * @param site
	 * @param type
	 * @param model
	 * @param nptConstants
	 */
	private void createDuplicatedNodeError(String neId, String site, String type, String model, String nptConstants,
			String fileName) {
		String message = "Duplicate Node found : NEID='" + neId + "', Site='" + site + "', Type='" + type + "' ,Model='" + model + "'";
		addErrorAlarm(message, fileName, Category.DUPLICATE_NETWORK_ELEMENT, nptConstants);
	}

	/**
	 * 
	 * @param neId
	 * @param site
	 * @param type
	 * @param model
	 * @param fileName
	 * @param category
	 * @param nptConstants
	 */
	public void addErrorAlarm(String message, String fileName, Category category, String nptConstants) {
		ErrorAlarm error = new ErrorAlarm(category);
		error.setMessage(message);
		error.setNeType(nptConstants);

		if (fileName != null)
			error.setAdditionalInfo("File Path : " + fileName);

		error.setType(ErrorAlarm.NETWORK_ERROR);
		NodeContainer.addErrorToList(error);
	}

	/**
	 * 
	 * @param elementNode
	 * @param neId
	 * @param neName
	 * @param neModel
	 * @param site
	 * @param tchSupported
	 * @return
	 */
	public ElementNode addNode(ElementNode elementNode, String neId, String neName, String neModel, String site,
			String tchSupported, String siteNo, PossibleNodeType nodeType) {
		// NetworkElementDumps
		File f = filesList.get(neId);
		if (f != null && nodeType != null) {
			NetworkElementDump elementDump = new NetworkElementDump();
			elementDump.setNodeId(neId);
			elementDump.setNodeType(nodeType);
			elementDump.setFileChangeDate(new Date(f.lastModified()));
			elementDump.setFilePath(f.getAbsolutePath());
			networkElementDumps.put(neId, elementDump);
		}
		elementNode.setId(neId);
		elementNode.setName(neName);
		elementNode.setElementName(neName);
		elementNode.setExternal(neId);
		elementNode.setSupplier("HUAWEI");
		elementNode.setType(neModel);
		elementNode.setImporterConnector(ImporterConnector.Huawei_M2000);

		if (elementNode instanceof ElementRanBs)
			((ElementRanBs) elementNode).setTechnologySupported(tchSupported);

		if (elementNode instanceof ElementNb) {
			if (site != null)
				NodeContainer.addNodeBByLabel(site, neId);

			if (siteNo != null)
				NodeContainer.addNodeBByLabel(siteNo, neId);
		}

		// set site (if it is touch deduce it from logical)
		if (!(elementNode instanceof ElementENodeB)
				|| ((elementNode instanceof ElementENodeB) && !manufactureFixer.isMtcTouchImport())) {
			elementNode.setSite(getMatchingSite(neId, site));
		} else if ((elementNode instanceof ElementNb) && manufactureFixer.isMtcTouchImport())
			elementNode.setSite(site);
		if (neName != null)
			nodeNameMap.put(neName.toLowerCase(), neId);

		return elementNode;
	}

	/**
	 * 
	 */
	public void addNodeInterfaces() {

		for (String key : nodeInterfaceMap.keySet()) {
			NodeInterface nodeInterface = nodeInterfaceMap.get(key);

			String boardId = nodeInterface.getNodeBoardId();

			if (boardId == null)
				continue;

			String slotId = boardId.substring(0, boardId.length() - 2);

			NodeSlot nodeSlot = nodeSlotMap.get(slotId);

			if (nodeSlot == null)
				continue;

			for (NodeBoard nodeBoard : nodeSlot.getNodeBoards()) {
				if (nodeBoard.getId().equalsIgnoreCase(boardId)) {
					Collection<NodeInterface> listNodeInterface = nodeBoard.getNodeInterfaces();
					listNodeInterface.add(nodeInterface);
					nodeBoard.setNodeInterfaces(listNodeInterface);
				}
			}
		}
	}

	/**
	 * @param startElement
	 * @param localPart
	 */
	public void fillNeModelMaps4NodeBENodeB(StartElement startElement, String localPart) {

		String name = null;
		String typeMatched = null;

		if (localPart.matches(m2v2r0Patterns.get("nodeB"))) {
			// get nodeBName
			Attribute nodeBNameAttribute = startElement
					.getAttributeByName(new QName(m2v2r0Patterns.get("nodeBNameAtt")));
			if (nodeBNameAttribute != null) {
				name = nodeBNameAttribute.getValue();
			}

			// get nodeBType
			Attribute nodeBTypeAttribute = startElement
					.getAttributeByName(new QName(m2v2r0Patterns.get("nodeBTypeAtt")));
			if (nodeBTypeAttribute != null) {
				typeMatched = nodeBTypeAttribute.getValue();
			}

			if (name != null && !name.isEmpty() && typeMatched != null && !typeMatched.isEmpty()
					&& !neModel4NodeBMap.containsKey(name))
				neModel4NodeBMap.put(name, typeMatched);
		} else if (localPart.matches(m2v2r0Patterns.get("enodeB"))) {
			// get nodeBName
			Attribute eNodeBNameAttribute = startElement
					.getAttributeByName(new QName(m2v2r0Patterns.get("eNodeBNameAtt")));
			if (eNodeBNameAttribute != null) {
				name = eNodeBNameAttribute.getValue();
			}

			// get nodeBType
			Attribute eNodeBTypeAttribute = startElement
					.getAttributeByName(new QName(m2v2r0Patterns.get("eNodeBTypeAtt")));
			if (eNodeBTypeAttribute != null) {
				typeMatched = eNodeBTypeAttribute.getValue();
			}

			if (name != null && !name.isEmpty() && typeMatched != null && !typeMatched.isEmpty()
					&& !neModel4EnodeBMap.containsKey(name))
				neModel4EnodeBMap.put(name, typeMatched);

		}
	}

	/**
	 * 
	 * @param startElement
	 * @param localPart
	 */
	private void fillMap4Roles(StartElement startElement, String localPart) {

		if (localPart.matches(m2v2r0Patterns.get("nodeB"))) {
			// get nodeBName
			Attribute nodeBNameAttribute = startElement
					.getAttributeByName(new QName(m2v2r0Patterns.get("nodeBNameAtt")));
			if (nodeBNameAttribute != null) {
				nodebNameFromRelationFile = nodeBNameAttribute.getValue();
			}
		} 
		else if (localPart.matches(m2v2r0Patterns.get("bts"))) {
			// get btsName
			Attribute btsNameAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("btsNameAtt")));
			if (btsNameAttribute != null) {
				btsNameFromRelationFile = btsNameAttribute.getValue();
			}
		} 
		else if (localPart.matches(m2v2r0Patterns.get("eNodeB"))) {
			// get enodeBName
			Attribute enodeBNameAttribute = startElement
					.getAttributeByName(new QName(m2v2r0Patterns.get("eNodeBNameAtt")));
			if (enodeBNameAttribute != null) {
				eNodebNameFromRelationFile = enodeBNameAttribute.getValue();
			}
		} else if (localPart.matches(m2v2r0Patterns.get("bts_3900"))) {
			// get bts3900
			Attribute bts3900NameAttribute = startElement
					.getAttributeByName(new QName(m2v2r0Patterns.get("bts3900NameAtt")));
			if (bts3900NameAttribute != null) {
				bts3900NameFromRelationFile = bts3900NameAttribute.getValue();
			}
		}
		 else if (localPart.matches("(?i)MICROBTS3900")) {
				// get microBts3900
				Attribute microBts3900NameAttribute = startElement
						.getAttributeByName(new QName("MICROBTS3900Name"));
				if (microBts3900NameAttribute != null) {
					microBts3900NameFromRelationFile = microBts3900NameAttribute.getValue();
				}
			}

		if (nodebNameFromRelationFile != null && btsNameFromRelationFile != null)
			btsNameByNodebNameRelationMap.put(nodebNameFromRelationFile, btsNameFromRelationFile);

		if (eNodebNameFromRelationFile != null && btsNameFromRelationFile != null)
			btsNameByNodebNameRelationMap.put(eNodebNameFromRelationFile, btsNameFromRelationFile);

		if (btsNameFromRelationFile != null && bts3900NameFromRelationFile != null)
			bts3900NameByBtsNameRelationMap.put(bts3900NameFromRelationFile, btsNameFromRelationFile);
		
		if (btsNameFromRelationFile != null && microBts3900NameFromRelationFile != null)
			microBts3900NameByBtsNameRelationMap.put(microBts3900NameFromRelationFile, btsNameFromRelationFile);

		if (nodebNameFromRelationFile != null && eNodebNameFromRelationFile != null)
			enodeBNameByNodeBNameRelationMap.put(nodebNameFromRelationFile, eNodebNameFromRelationFile);

		if (nodebNameFromRelationFile != null && bts3900NameFromRelationFile != null)
			bts3900NameByNodeBNameRelationMap.put(nodebNameFromRelationFile, bts3900NameFromRelationFile);
		
		if (nodebNameFromRelationFile != null && microBts3900NameFromRelationFile != null)
			microBts3900NameByNodeBNameRelationMap.put(nodebNameFromRelationFile, microBts3900NameFromRelationFile);
	}

	/**
	 * @param neType
	 * @return
	 */
	public String extractSupported(String neType) {

		if (neType == null)
			return null;

		String Supported = null;

		if (isNeTypeMatched(neType, m2v2r0Patterns.get("gsmBts"))
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gsm"))
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts"))
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6000Bts"))
				|| isNeTypeMatched(neType, "(?i)MBTS"))
			Supported = m2v2r0Patterns.get("2g");

		else if (isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Umts"))
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("nodeB")) || isNeTypeMatched(neType, "(?i)BSC6910UMTS"))
			Supported = m2v2r0Patterns.get("3g");

		else if (isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu")) || isNeTypeMatched(neType, "(?i)BSC6910GU")
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGu")))
			Supported = m2v2r0Patterns.get("2g3g");

		else if (isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGl")))
			Supported = m2v2r0Patterns.get("2g4g");

		else if (isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsUl")))
			Supported = m2v2r0Patterns.get("3g4g");

		else if (isNeTypeMatched(neType, m2v2r0Patterns.get("enodeB")))
			Supported = m2v2r0Patterns.get("4g");

		else if (isNeTypeMatched(neType, m2v2r0Patterns.get("bts3900")) || isNeTypeMatched(neType, "(?i)MICROBTS3900") || isNeTypeMatched(neType, "(?i)MBTS\\(GUL\\)") || isNeTypeMatched(neType, "(?i)PICOBTS3900"))
			Supported = "2G/3G/4G";
		
		
		else if (isNeTypeMatched(neType, "(?i)BTS59005G") || isNeTypeMatched(neType, "(?i)BTS59005G")) {
			if(StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.5g.parsing"), "false")) {
				Supported = "5G";
			}
		}
			

		return Supported;
	}
	
	public boolean isRanBs(String neType) {

		if (neType == null)
			return false;

		
		if (isNeTypeMatched(neType, m2v2r0Patterns.get("gsmBts")) || isNeTypeMatched(neType, "(?i)MBTS") || isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGu"))
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGu")) || isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGl")) || isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsUl"))
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts3900")) || isNeTypeMatched(neType, "(?i)MICROBTS3900") || isNeTypeMatched(neType, "(?i)MBTS\\(GUL\\)")
				|| isNeTypeMatched(neType, "(?i)PICOBTS3900"))
			return true;
		
		else if(StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.5g.parsing"), "false")) {
			if(isNeTypeMatched(neType, "(?i)BTS59005G") || isNeTypeMatched(neType, "(?i)BTS59005G"))
				return true;
		}
		
		return false;
	}


	/**
	 * @param softVersion
	 * @param neType
	 * @param neName
	 * @return
	 */
	public String extractNeModel(String softVersion, String neType, String neName) {

		String neModel = null;

		if (neType != null) {
			// get model for CgpOmu
			if (!manufactureFixer.isCellCImport()) {
				if (isNeTypeMatched(neType, m2v2r0Patterns.get("cgpomu"))) {
					neModel = nodesMatchingParser.getNeModel(neName);
				}
				
				if (isNeTypeMatched(neType, "(?i)BTS5900") || isNeTypeMatched(neType, "(?i)BTS59005G")) {
					neModel = neType;
				}
			}

			// get model for RNC
			 if (isNeTypeMatched(neType, m2v2r0Patterns.get("rnc"))) {
				neModel = getNeModel4Rnc(softVersion);
			}

			// get model for BSC6900GU , bsc6900Gsm, bsc6900Umts.......
			else if (neType.equals("UPCC") || neType.equals("SE2900") || neType.equals("ATS")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu"))
					|| isNeTypeMatched(neType, "(?i)BSC6910GU")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gsm"))
					|| isNeTypeMatched(neType, "(?i)BSC6910GSM")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Umts"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("ugw"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6000"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("hlr"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("GGSN80"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("sgsn"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("SE2600"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("MSCServer"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mgw"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("FMCMGW"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("cg"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("ics"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("hss")) || isNeTypeMatched(neType, "(?i)BSC6910UMTS")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("usn"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("UIM"))
					|| isNeTypeMatched(neType, "(?i)PICOBTS3900")) {
				neModel = nodesMatchingParser.getNeModel(neType);
			}

			// get model for GsmBts, NodeB, eNodeB, Mbts(GU), Mbts(Gl),
			// Mbts(Ul).......
			else if (isNeTypeMatched(neType, m2v2r0Patterns.get("gsmBts"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("nodeB"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("enodeB"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGu"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGl"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsUl")) || isNeTypeMatched(neType, "(?i)MBTS")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6000Bts"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts")) || isNeTypeMatched(neType, "(?i)MICROBTS3900")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts3900"))
					|| isNeTypeMatched(neType, "(?i)MBTS\\(GUL\\)")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("dbs3900ibs"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("oss"))
					|| ((isNeTypeMatched(neType, "(?i)BTS5900")
					|| isNeTypeMatched(neType, "(?i)BTS59005G")) && StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.5g.parsing"), "false"))) {
				
				neModel = modelByRackTypeRuleParser.getNeModel(nodeModelByRackTypeList, nodeModelByFrameTypeList, neType);

				if (neModel == null && !nodeModelByRackTypeList.isEmpty())
					neModel = nodeModelByRackTypeList.get(0);
			}
		}
		
//		if(neModel==null && neType!=null)
//		  neModel = nodesMatchingParser.getNeModel(neType);

		return neModel;
	}

	/**
	 * @param softVer
	 * @return
	 */
	public String getNeModel4Rnc(String softVer) {
		String neModel = null;

		if (softVer == null)
			return null;

		if (softVer.matches(m2v2r0Patterns.get("bsc6810Pat"))) {
			neModel = m2v2r0Patterns.get("rnc6810");
		} else if (softVer.matches(m2v2r0Patterns.get("bsc6800Pat"))) {
			neModel = m2v2r0Patterns.get("rnc6800");
		}

		return neModel;
	}

	/**
	 * 
	 * @param neType
	 */
	private void setRuForCurrentShelfs(String neType) {
		for (NodeCabinet cab : currentCabinetsList) {
			for (NodeShelf shelf : currentShelfsList) {
				String cabinetModel = cab.getModel();
				Integer ruIndex = shelf.getStartRu();
				Integer cabinetIndexFromCurrentCab = cab.getCabinetIndex();
				Integer cabinetIndexFromCurrentShelf = shelf.getCabinet() != null ? shelf.getCabinet().getCabinetIndex()
						: null;

				if (ruIndex != null || isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu"))
						|| isNeTypeMatched(neType, "(?i)BSC6910GU")
						|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gsm"))
						|| isNeTypeMatched(neType, "(?i)BSC6910GSM")
						|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Umts"))
						|| isNeTypeMatched(neType, "(?i)BSC6910UMTS")
						|| (cabinetIndexFromCurrentCab != cabinetIndexFromCurrentShelf))
					continue;

				ruIndex = serie3900RuMapper.getRuNumberForSerie3900(cabinetModel, shelf.getModel(),
						shelf.getShelfIndex().toString());

				if (ruIndex == null) {
					if (isNeTypeMatched(neType, m2v2r0Patterns.get("ugw")))
						ruIndex = -2;
					else
						ruIndex = -1;
				}

				shelf.setStartRu(ruIndex);

				mapShelf.put(shelf.getId(), shelf);
			}
		}
	}

	/**
	 * @param neModel
	 */
	public void setModel4CurrentsCabinet(String neModel) {

		for (NodeCabinet cab : currentCabinetsList) {
			String cabinetModel = cab.getModel();
			String matchedCabinetModel = null;

			if (cabinetModel == null || cabinetModel.isEmpty())
				matchedCabinetModel = cabinetModelMatchingParser.getCabinetMatchedModelWithoutRackType(neModel);
			else
				matchedCabinetModel = cabinetModelMatchingParser.getCabinetMatchedModelWithRackType(neModel,
						cabinetModel);

			if (matchedCabinetModel != null) {
				if (!manufactureFixer.isMtcTouchImport() && !manufactureFixer.isZkImport())
					NodeContainer.getNodeCabinetTypes().remove(cabinetModel);
				cabinetModel = matchedCabinetModel;
			} else if (cabinetModel == null)
				cabinetModel = "Cabinet" + "_" + cab.getCabinetIndex() + "_" + neModel;

			cab.setModel(cabinetModel);
			cab.setCabinetType(NodeContainer.getNodeCabinetTypeForName(cabinetModel));

			mapCabinet.put(cab.getId(), cab);
		}
	}

	/**
	 * @param neModel
	 */
	public void setModel4CurrentsShelf(String neModel) {

		for (NodeShelf shelf : currentShelfsList) {
			String shelfModel = shelf.getModel();

			if (shelfModel != null) {
				if (shelfModel.isEmpty()) {
					if (neModel != null) {
						String model = "Shelf" + "_" + shelf.getShelfIndex() + "_" + neModel;
						shelf.setModel(model);
						shelf.setShelfType(NodeContainer.getNodeShelfTypeForName(model));

						mapShelf.put(shelf.getId(), shelf);
					}
				} else {
					for (NodeCabinet cabinet : currentCabinetsList) {
						if (shelf.getCabinet() != null && shelf.getCabinet().getId().equals(cabinet.getId())) {
							String matchedModel = modelMatchingParser
									.getShelfMatchedModel(cabinet.getModel() + "_" + shelfModel);

							if (matchedModel == null)
								break;

							shelf.setModel(matchedModel);
							shelf.setShelfType(NodeContainer.getNodeShelfTypeForName(matchedModel));

							mapShelf.put(shelf.getId(), shelf);

							break;
						}
					}
				}
			}
		}
	}

	/**
	 * @param startElement
	 * @param neName
	 */
	public void putBoardId4NodeInterface(StartElement startElement, String neName) {

		String slotPos = null;
		String slotNo = null;
		String frameNo = null;
		String rackNo = null;

		// get indexOnSlot
		Attribute indexOnSlotAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("slotPos")));
		if (indexOnSlotAttribute != null) {
			slotPos = indexOnSlotAttribute.getValue();
		}

		// get slotIndex
		Attribute slotIndexAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("slotNo")));
		if (slotIndexAttribute != null) {
			slotNo = slotIndexAttribute.getValue();
		}

		// get cabinetIndex
		Attribute cabinetIdAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("rackNo")));
		if (cabinetIdAttribute != null) {
			rackNo = cabinetIdAttribute.getValue();
		}
		if (rackNo == null) {
			Attribute cabinetNoAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("CabinetNo")));
			if (cabinetNoAttribute != null) {
				rackNo = cabinetNoAttribute.getValue();
			}
		}

		// get shelfIndex
		Attribute shelfIdAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("frameNo")));
		if (shelfIdAttribute != null) {
			frameNo = shelfIdAttribute.getValue();
		}
		if (frameNo == null) {
			Attribute subrackNoAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("SubrackNo")));
			if (subrackNoAttribute != null) {
				frameNo = subrackNoAttribute.getValue();
			}
		}

		String partBoardId = neName + "_" + rackNo + "_" + frameNo + "_" + slotNo;
		String fullBoardId = partBoardId + "_" + slotPos;
		boardId4NodeInterfaceMap.put(partBoardId, fullBoardId);
	}

	/**
	 * @param startElement
	 */
	public void putNeModel4BtsAndSRanBs(StartElement startElement, String neId) {

		String siteIndex = null;
		String siteName = null;
		String siteType = null;

		// get siteIndex
		Attribute siteIndexAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("siteIndexAtt")));
		if (siteIndexAttribute != null) {
			siteIndex = siteIndexAttribute.getValue();
		}

		// get siteName
		Attribute siteNameAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("siteNameAtt")));
		if (siteNameAttribute != null) {
			siteName = siteNameAttribute.getValue();
		}

		// get siteName
		Attribute siteTypeAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("siteTypeAtt")));
		if (siteTypeAttribute != null) {
			siteType = siteTypeAttribute.getValue();
		}

		if (siteName != null && !siteName.isEmpty() && siteType != null && !siteType.isEmpty()
				&& !neModel4BtsAndSRanBsMap.containsKey(siteName))
			neModel4BtsAndSRanBsMap.put(siteName, siteType);
		if (siteName != null && !siteName.isEmpty() && siteIndex != null && !siteIndex.isEmpty()
				&& !btsNameBySiteIndex.containsKey(siteName))
			btsNameBySiteIndex.put(neId + "_" + siteIndex, siteName);
	}

	/**
	 * @param startElement
	 * @param neId
	 */
	public void putParentRnc(StartElement startElement, String neId, String neType) {

		Attribute nodeBNameAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("nodeBName")));
		if (nodeBNameAttribute != null) {
			String parentRnc = nodeBNameAttribute.getValue();
			if (!parentRnc.isEmpty() && !parentRncMap.containsKey(parentRnc)) {
				parentRncMap.put(parentRnc, neId);
			}
		}
	}

	/**
	 * @param startElement
	 * @param neId
	 */
	public void putParentBsc(StartElement startElement, String neId) {

		Attribute siteNameAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("siteNameAtt")));
		if (siteNameAttribute != null) {
			String parentBsc = siteNameAttribute.getValue();
			if (!parentBsc.isEmpty() && !parentBscMap.containsKey(parentBsc)) {
				parentBscMap.put(parentBsc, neId);
			}
		}
	}

	/**
	 * @param startElement
	 * @param neType
	 * @param neName
	 * @throws ParseException
	 */
	public void createCabinet(StartElement startElement, String neType, String neId) throws ParseException {

		String index = null;
		String model = null;
		String partNb = null;
		String issueNb = null;
		String serialNb = null;
		String manufacturerDate = null;
		String description = null;

		// Get cabinetIndex
		Attribute cabIndexAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("rackNo")));
		if (cabIndexAttribute != null) {
			index = cabIndexAttribute.getValue();
		}
		if (index == null) {
			Attribute cabinetNoAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("CabinetNo")));
			if (cabinetNoAttribute != null) {
				index = cabinetNoAttribute.getValue();
			}
		}

		if (index.isEmpty())
			index = null;

		// get model
		Attribute modelAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("rackType")));
		if (modelAttribute != null) {
			model = modelAttribute.getValue();

			// fill map to extract NeModel from rackType for NodeB, gsmBts,
			// enodeB
			if (neType != null && !model.isEmpty() && (isNeTypeMatched(neType, m2v2r0Patterns.get("nodeB"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("gsmBts"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("enodeB"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsUl"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGl")) || isNeTypeMatched(neType, "(?i)MBTS")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGu"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6000Bts"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts")) || isNeTypeMatched(neType, "(?i)MICROBTS3900")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts3900"))
					|| isNeTypeMatched(neType, "(?i)MBTS\\(GUL\\)")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("dbs3900ibs"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("oss"))
					|| ((isNeTypeMatched(neType, "(?i)BTS5900")
					|| isNeTypeMatched(neType, "(?i)BTS59005G"))) && StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.5g.parsing"), "false"))
					&& !nodeModelByRackTypeList.contains(model)) {
				nodeModelByRackTypeList.add(model.toUpperCase());
			}
			if (model.isEmpty()) {
				if (neType.equals("MSCServer"))
					model = "N68E-22_MSOFTX3000";
			}
		}

		// get partNb
		Attribute partNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("bomCode")));
		if (partNbAttribute != null) {
			partNb = partNbAttribute.getValue();
		}

		// get manufacturerDate
		Attribute manufacturerDateAttribute = startElement
				.getAttributeByName(new QName(m2v2r0Patterns.get("dateOfManufacture")));
		if (manufacturerDateAttribute != null) {
			manufacturerDate = manufacturerDateAttribute.getValue();
		}

		// get issueNb
		Attribute issueNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("issueNumber")));
		if (issueNbAttribute != null) {
			issueNb = issueNbAttribute.getValue();
		}

		// get serialNb
		Attribute serialNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("serialNumber")));
		if (serialNbAttribute != null) {
			serialNb = serialNbAttribute.getValue();
		}

		// get description
		Attribute descriptionAttribute = startElement
				.getAttributeByName(new QName(m2v2r0Patterns.get("manufacturerData")));
		if (descriptionAttribute != null) {
			description = descriptionAttribute.getValue();
		}

		String cabinetId = neId + "_" + index;

		if (!mapCabinet.containsKey(cabinetId) && index != null) {

			NodeCabinet cabinet = new NodeCabinet();
			cabinet.setId(cabinetId);
			cabinet.setModel(model);
			cabinet.setNodeId(neId);
			cabinet.setCabinetType(NodeContainer.getNodeCabinetTypeForName(model));
			cabinet.setPartNumber(partNb);

			try {
				cabinet.setCabinetIndex(new Integer(index));
			} catch (Exception e) {
				log.error("Error: cabinetIndex (" + index + ") for cabinet (" + cabinetId + ") should be integer");
			}

			if (issueNb != null && !issueNb.isEmpty()) {
				try {
					cabinet.setIssueNumber(new Integer(issueNb));
				} catch (Exception e) {
					log.error("Error: Issue Nb (" + issueNb + ") for cabinet (" + cabinetId + ") should be integer");
				}
			}

			cabinet.setSerialNumber(serialNb);
			cabinet.setManufacturerName("HUAWEI");
			cabinet.setDescription(description);

			Date manufactDate = null;
			if (manufacturerDate != null && !manufacturerDate.isEmpty()) {
				manufactDate = flexibleDateParser.parseDate(manufacturerDate);
			}
			cabinet.setManufacturingDate(manufactDate);

			mapCabinet.put(cabinetId, cabinet);

			currentCabinetsList.add(cabinet);
			NodeContainer.getNodeCabinetForName(cabinet.getId(), cabinet);

			setElementsIdByNodeIdMap(cabinetsIdByNeIdMap, cabinetId, neId);
		}
	}

	/**
	 * @param startElement
	 * @param neType
	 * @param neName
	 * @throws ParseException
	 */
	public void createShelf(String fileName,StartElement startElement, String neType, String type, String neId, String site)
			throws ParseException {

		String index = null;
		String cabinetIndex = null;
		String model = null;
		String partNb = null;
		String issueNb = null;
		String serialNb = null;
		String manufacturerDate = null;
		String description = null;
		Integer ruIndex = null;
		String frameType = null;

		// get cabinetId
		Attribute cabinetIdAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("rackNo")));
		if (cabinetIdAttribute != null) {
			cabinetIndex = cabinetIdAttribute.getValue();
		}
		if (cabinetIndex == null) {
			Attribute cabinetNoAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("CabinetNo")));
			if (cabinetNoAttribute != null) {
				cabinetIndex = cabinetNoAttribute.getValue();
			}
		}

		// Get shelfIndex
		Attribute shelfIndexAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("frameNo")));
		if (shelfIndexAttribute != null) {
			index = shelfIndexAttribute.getValue();
		}
		if (index == null) {
			Attribute subrackNoAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("SubrackNo")));
			if (subrackNoAttribute != null) {
				index = subrackNoAttribute.getValue();
				if(fileName.startsWith("AIM") && manufactureFixer.isZkImport())
				subrackMap.put(neId, index);
			}
		}

		if (index.isEmpty())
			index = null;

		// get model
		Attribute modelAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("frameType")));
		if (modelAttribute != null) {
			model = modelAttribute.getValue();
			frameType = model;

			// fill map to extract NeModel from rackType for NodeB, gsmBts,
			// enodeB
			if (neType != null && !model.isEmpty() && (isNeTypeMatched(neType, m2v2r0Patterns.get("nodeB"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("gsmBts"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("enodeB"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsUl"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGl")) || isNeTypeMatched(neType, "(?i)MBTS")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGu"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts")) || isNeTypeMatched(neType, "(?i)MICROBTS3900")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6000Bts"))
					|| ((isNeTypeMatched(neType, "(?i)BTS59005G")
					|| isNeTypeMatched(neType, "(?i)BTS5900")) && StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.5g.parsing"), "false"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts3900"))
					|| isNeTypeMatched(neType, "(?i)MBTS\\(GUL\\)")) && !nodeModelByFrameTypeList.contains(model)) {
				nodeModelByFrameTypeList.add(model.toUpperCase());
			}
		}

		// get partNb
		Attribute partNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("bomCode")));
		if (partNbAttribute != null) {
			partNb = partNbAttribute.getValue();
		}

		// get manufacturerDate
		Attribute manufacturerDateAttribute = startElement
				.getAttributeByName(new QName(m2v2r0Patterns.get("dateOfManufacture")));
		if (manufacturerDateAttribute != null) {
			manufacturerDate = manufacturerDateAttribute.getValue();
		}

		// get issueNb
		Attribute issueNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("issueNumber")));
		if (issueNbAttribute != null) {
			issueNb = issueNbAttribute.getValue();
		}

		// get serialNb
		Attribute serialNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("serialNumber")));
		if (serialNbAttribute != null) {
			serialNb = serialNbAttribute.getValue();
		}

		// get description
		Attribute descriptionAttribute = startElement
				.getAttributeByName(new QName(m2v2r0Patterns.get("manufacturerData")));
		if (descriptionAttribute != null) {
			description = descriptionAttribute.getValue();
		}

		String cabId = neId + "_" + cabinetIndex;
		String shelfId = cabId + "_" + index;

		if (isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu")) || isNeTypeMatched(neType, "(?i)BSC6910GU")
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gsm"))
				|| isNeTypeMatched(neType, "(?i)BSC6910GSM")
				|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Umts"))
				|| isNeTypeMatched(neType, "(?i)BSC6910UMTS"))
			ruIndex = bscRuMapper.getRuNumberForBsc(model, index);

		else {
			NodeCabinet cabinet = mapCabinet.get(cabId);
			if (cabinet != null) {
				String cabinetModel = cabinet.getModel();
				ruIndex = serie3900RuMapper.getRuNumberForSerie3900(cabinetModel, model, index);
			}
		}

		if (model != null) {
			if (model.matches(m2v2r0Patterns.get("rru")))
				model = m2v2r0Patterns.get("rruShelfModel");
			else if (model.matches(m2v2r0Patterns.get("bbu")))
				model = m2v2r0Patterns.get("bbu3900");
		}

		if (!mapShelf.containsKey(shelfId) && index != null && cabinetIndex != null) {
			if (frameType.matches("TCS")) {
				ElementTrau trau = new ElementTrau("trau");
				trau.setId(shelfId);
				trau.setName(shelfId);
				trau.setSite(site);// getSiteFromNeId(neNameFromList));
				trau.setType(frameType);
				elementTrauMap.put(shelfId, trau);
			}
			NodeShelf shelf = new NodeShelf();
			shelf.setId(shelfId);

			NodeCabinet nodeCabinet = mapCabinet.get(cabId);
			shelf.setCabinet(nodeCabinet);
			shelf.setModel(model);
			shelf.setShelfType(NodeContainer.getNodeShelfTypeForName(model));
			shelf.setPartNumber(partNb);
			shelf.setStartRu(ruIndex);

			try {
				shelf.setShelfIndex(new Integer(index));
			} catch (Exception e) {
				log.error("Error: shelfIndex (" + index + ") for shelf (" + shelfId + ") should be integer");
			}

			if (issueNb != null && !issueNb.isEmpty()) {
				try {
					shelf.setIssueNumber(new Integer(issueNb));
				} catch (Exception e) {
					log.error("Error: Issue Nb should be integer");
				}
			}

			shelf.setSerialNumber(serialNb);
			shelf.setManufacturerName("HUAWEI");
			shelf.setDescription(description);

			Date manufactDate = null;
			if (manufacturerDate != null && !manufacturerDate.isEmpty() && !manufacturerDate.isEmpty()) {
				manufactDate = flexibleDateParser.parseDate(manufacturerDate);
			}

			shelf.setManufacturingDate(manufactDate);

			mapShelf.put(shelfId, shelf);

			currentShelfsList.add(shelf);
			NodeContainer.getNodeShelfForName(shelf.getId(), shelf);

			setElementsIdByNodeIdMap(shelfsIdByNeIdMap, shelfId, neId);
		}

		if (manufactureFixer.isMeditel3xImport() || manufactureFixer.isCellCImport() || manufactureFixer.isInwi3xImport() || manufactureFixer.isZkImport()) {
			String setId = null;
			if (isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGul")) || isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGu")) || isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGl")) || isNeTypeMatched(neType, "MBTS"))
				setId = neId + "_MBTS";
			else if (isNeTypeMatched(neType, m2v2r0Patterns.get("bts3900"))) {
				setId = neId + "_bts3900";
				if (serialNb != "" || !serialNb.isEmpty())
					bts3900SerialNb.add(serialNb.toUpperCase());
			} else if (isNeTypeMatched(neType, m2v2r0Patterns.get("nodeB")))
				setId = neId + "_nodeB";
			else if (isNeTypeMatched(neType, m2v2r0Patterns.get("enodeB")))
				setId = neId + "_enodeB";
			else if (isNeTypeMatched(neType, m2v2r0Patterns.get("gsmBts")))
				setId = neId + "_gsmBts";
			else if (isNeTypeMatched(neType, "(?i)BTS59005G") || isNeTypeMatched(neType, "(?i)BTS5900")) {
				if(StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.5g.parsing"), "false"))
					setId = neId + "_gnodeb";
			}
			
			
			if (setId != null) {
				if (type != null && frameType != null) {
					if (frameType.matches(m2v2r0Patterns.get("bbu3900")) || frameType.matches(m2v2r0Patterns.get("bbu3910")) || frameType.matches("BBU5900")) {
						if (serialNb != null && !serialNb.isEmpty()) {
							Set<String> neIdSet = bbu3900SerialNbMap.get(serialNb.toUpperCase());
							Set<String> neTypeSet = neTypeBbu3900SerialNbMap.get(serialNb.toUpperCase());
							if (neIdSet == null)
								neIdSet = new HashSet<String>();
							if (neTypeSet == null)
								neTypeSet = new HashSet<String>();
							neIdSet.add(setId);
							neTypeSet.add(neType);
							nodesWithNoSerials.remove(setId);
							if (!neIdSet.isEmpty())
								bbu3900SerialNbMap.put(serialNb.toUpperCase(), neIdSet);
							if (!neTypeSet.isEmpty())
								neTypeBbu3900SerialNbMap.put(serialNb.toUpperCase(), neTypeSet);
						} else
							nodesWithNoSerials.add(setId);
					} else if ((!frameType.matches(m2v2r0Patterns.get("bbu3900")) && !frameType.matches(m2v2r0Patterns.get("bbu3910")) && !frameType.matches("BBU5900"))) {
						nodesWithNoSerials.add(setId);
						for (Set<String> id : bbu3900SerialNbMap.values())
							if (id.contains(setId))
								nodesWithNoSerials.remove(setId);
					}
				}
			}
			
			if(manufactureFixer.isZkImport() && isNeTypeMatched(neType, "(?i)MICROBTS3900")){
				setId = neId + "_microBts3900";
				if (serialNb != "" || !serialNb.isEmpty()) {
					microBts3900SerialNb.add(serialNb.toUpperCase());
					if (serialNb != null && !serialNb.isEmpty()) {
						Set<String> neIdSet = virtualMicroSerialNbMap.get(serialNb.toUpperCase());
						Set<String> neTypeSet = neTypeVirtualMicroSerialNbMap.get(serialNb.toUpperCase());
						if (neIdSet == null)
							neIdSet = new HashSet<String>();
						if (neTypeSet == null)
							neTypeSet = new HashSet<String>();
						neIdSet.add(setId);
						neTypeSet.add(neType);
						nodesWithNoSerials.remove(setId);
						if (!neIdSet.isEmpty())
							virtualMicroSerialNbMap.put(serialNb.toUpperCase(), neIdSet);
						if (!neTypeSet.isEmpty())
							neTypeVirtualMicroSerialNbMap.put(serialNb.toUpperCase(), neTypeSet);
					} else
						nodesWithNoSerials.add(setId);
				}
			}
			
		} 
	}
	
	public void createAntenna(String fileName, StartElement startElement, String neId) throws ParseException {
		log.debug("Start Creating Antenna after Parsing Huwei Files");
		Map<String, String> rolesMap = new HashMap<String, String>();
		log.debug("Radio Antenna-Fixing");
		String inventoryunitid = null;
		String unitposition = null;
		String antennadevicetype = null;
		String serialnumber = null;
		String serialnumberex = null;
		
		if(neId.contains("16-GUST-TEMP-CO-MPT-839"))
			System.out.println();

		Attribute inventoryunitiddAttribute = startElement.getAttributeByName(new QName("InventoryUnitId"));
		if (inventoryunitiddAttribute != null) {
			inventoryunitid = inventoryunitiddAttribute.getValue();
		}
		
		Attribute unitPositionAttribute = startElement.getAttributeByName(new QName("UnitPosition"));
		if (unitPositionAttribute != null) {
			unitposition = unitPositionAttribute.getValue();
		}
		
		Attribute antennadevicetypeAttribute = startElement.getAttributeByName(new QName("AntennaDeviceType"));
		if (antennadevicetypeAttribute != null) {
			antennadevicetype = antennadevicetypeAttribute.getValue();
		}
		
		Attribute serialnumberexAttribute = startElement.getAttributeByName(new QName("SerialNumberEx"));
		if (serialnumberexAttribute != null) {
			serialnumberex = serialnumberexAttribute.getValue();
		}
		
		Attribute serialnumberAttribute = startElement.getAttributeByName(new QName("SerialNumber"));
		if (serialnumberAttribute != null) {
			serialnumber = serialnumberAttribute.getValue();
		}
		
		 	
		if(StringUtils.isNotBlank(inventoryunitid) && StringUtils.isNotBlank(antennadevicetype)) {
			Antenna antennaPart = new Antenna();
			antennaPart.antennadevicetype = antennadevicetype;
			antennaPart.unitposition = unitposition;
			antennaPart.inventoryunitid = inventoryunitid;
			antennaPart.serialnumber = serialnumber;
			antennaPart.serialnumberex = serialnumberex;
			antennaPart.neid = neId;
			antennaPart.file = fileName;
			
			if(antennaPart.site == null){
				antennaPart.site  = StringUtils.substringAfterLast(antennaPart.neid, "_");
			}
			
			 
			String antennakey = TransmissionCommon.concatenateStrings("_", antennaPart.site, antennaPart.inventoryunitid);
			String key = TransmissionCommon.concatenateStrings("_", antennakey, "SerialNumberEx", antennaPart.serialnumberex);
			ElementRadioAntenna radioAntenna = createRadioAntenna(antennaPart);
			ElementAdditionalInfo additionalInfo = TransmissionCommon.createAdditionalInformation(radioAntenna.getId(), ElementType.RadioAntenna, "SerialNumber", "SerialNumberEx", antennaPart.serialnumberex);
		    if(!radioantennaserialnoList.contains(antennaPart.serialnumberex)){
			addInfoMap.put(key, additionalInfo);
		    }
			radioantennaserialnoList.add(antennaPart.serialnumberex);
			
		}			
	}
	
//	private void createAntenna() {
//		log.debug("Start Creating Antenna after Parsing Huwei Files");
//		Map<String, String> rolesMap = new HashMap<String, String>();
//		for(ElementRanBs ranbs : ranBsMap.values()) {
//			if(ranbs.getGsmCapability() != null)
//				rolesMap.put(ranbs.getGsmCapability().getId(), ranbs.getId());
//			if(ranbs.getUmtsCapability() != null)
//				rolesMap.put(ranbs.getUmtsCapability().getId(), ranbs.getId());
//			if(ranbs.getLteCapability() != null)
//				rolesMap.put(ranbs.getLteCapability().getId(), ranbs.getId());
//			if(ranbs.getNrCapability() != null)
//				rolesMap.put(ranbs.getNrCapability().getId(), ranbs.getId());
//		}
//		
//		Comparator<Antenna> minComparator = new Comparator<Antenna>() {
//
//			@Override
//            public int compare(Antenna antenna1, Antenna antenna2) {
//				String inventoryunitid1 = antenna1.inventoryunitid;
//				String inventoryunitid2 = antenna2.inventoryunitid;
//				if(StringUtils.isNotBlank(inventoryunitid1))
//					inventoryunitid1 = inventoryunitid1.replaceAll("[^0-9]", "");
//				if(StringUtils.isNotBlank(inventoryunitid2))
//					inventoryunitid2 = inventoryunitid2.replaceAll("[^0-9]", "");
//                return Integer.valueOf(inventoryunitid1).compareTo(Integer.valueOf(inventoryunitid2));
//            }
//            
//        };
//		
//        Map<String, List<Antenna>> antennaInventoryUnitIdMap = new HashMap<>();
//		
//		for(String neid : antennaPartMap.keySet()) {
//			try {	
//				String parentnode = null;
//				
//				List<Antenna> antennaParts = antennaPartMap.get(neid);
//				String site = null;
//				if(ranBsMap.get(neid) != null)
//					site = ranBsMap.get(neid).getSite();
//				
//				else if(rolesMap.get(neid) != null) {
//					site = ranBsMap.get(rolesMap.get(neid)).getSite();
//					parentnode = ranBsMap.get(rolesMap.get(neid)).getId();
//				}
//				else if(btsMap.get(neid) != null) {
//					site = btsMap.get(neid).getSite();
//				}
//				else if(nodeBMap.get(neid) != null) {
//					site = nodeBMap.get(neid).getSite();
//				}
//				else if(eNodeBMap.get(neid) != null) {
//					site = eNodeBMap.get(neid).getSite();
//				}
//				
//				else if(ranBsIdByRoleIdMap.get(neid.toUpperCase()) != null && ranBsMap.get(ranBsIdByRoleIdMap.get(neid.toUpperCase())) != null) {
//					site = ranBsMap.get(ranBsIdByRoleIdMap.get(neid.toUpperCase())).getSite();
//					parentnode = ranBsMap.get(ranBsIdByRoleIdMap.get(neid.toUpperCase())).getId();
//				}
//				
//				else if(ranbsRelationMap.get(neid.toUpperCase()) != null && ranBsMap.get(ranbsRelationMap.get(neid.toUpperCase())) != null) {
//					site = ranBsMap.get(ranbsRelationMap.get(neid.toUpperCase())).getSite();
//					parentnode = ranBsMap.get(ranbsRelationMap.get(neid.toUpperCase())).getId();
//				}
//				
//				if(StringUtils.isBlank(site)) {
//					continue;
//				}
//				
//				for(Antenna antenna : antennaParts) {
//					antenna.site = site;
//					antenna.parentnode = parentnode;
//					if(!antennaInventoryUnitIdMap.containsKey(antenna.serialnumberex))
//						antennaInventoryUnitIdMap.put(antenna.serialnumberex, new ArrayList<>());
//					
//					antennaInventoryUnitIdMap.get(antenna.serialnumberex).add(antenna);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		
//		
//		for(String serialNumberEx : antennaInventoryUnitIdMap.keySet()) {
//			try {
//				List<Antenna> antennaList = antennaInventoryUnitIdMap.get(serialNumberEx);
//				if(StringUtils.isBlank(serialNumberEx)) {
//					for(Antenna antenna : antennaList) {
//						ElementRadioAntenna radioAntenna = createRadioAntenna(antenna);
//						if(radioAntenna != null) {
//							String message = "Serial Number not found for Antenna =" + radioAntenna.getId();
//							addErrorAlarm(message, antenna.file, Category.MISSING_DATA, "Radio Antenna");
//						}
//					}
//				}
//				else {
//					Map<String, List<Antenna>> antennabyneidMap = antennaList.stream().filter(e -> e.neid != null)
//							.collect(Collectors.groupingBy(e -> e.neid, Collectors.mapping(e -> e, Collectors.toList())));
//					
//					for(String neid : antennabyneidMap.keySet()) {
//						try {
//							Antenna antenna = antennabyneidMap.get(neid).stream().min(minComparator).orElse(null);
//							
//							if(antenna == null || antennabyneidMap.get(antenna.parentnode) != null)
//								continue;
//							
//							ElementRadioAntenna radioAntenna = createRadioAntenna(antenna);
//							if(radioAntenna != null) {
//								String antennakey = TransmissionCommon.concatenateStrings("_", antenna.site, antenna.inventoryunitid);
//								String key = TransmissionCommon.concatenateStrings("_", antennakey, "SerialNumberEx", serialNumberEx);
//								ElementAdditionalInfo additionalInfo = TransmissionCommon.createAdditionalInformation(radioAntenna.getId(), ElementType.RadioAntenna, "SerialNumber", "SerialNumberEx", serialNumberEx);
//								addInfoMap.put(key, additionalInfo);
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						
//					}
//					
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//	}
	
	private ElementRadioAntenna createRadioAntenna(Antenna antenna) {
		
		if(antenna == null)
			return null;
		String antennakey = TransmissionCommon.concatenateStrings("_", antenna.site, antenna.inventoryunitid);
		
//		if(antennaMap.containsKey(antennakey))
//			return null;
	
		String name = StringUtils.substringBetween(antenna.unitposition, "AntennaDeviceName=", ",");
		String antennaid = String.join("_", antenna.neid, antenna.inventoryunitid);
		
		String antennaname = String.join(",", antennaid, name);
		
		ElementRadioAntenna radioAntenna = new ElementRadioAntenna();
		radioAntenna.setId(antennaid);
		radioAntenna.setExternal(antennaid);
		radioAntenna.setSite(antenna.site);
		radioAntenna.setElementName(antennaname);
		radioAntenna.setType(antenna.antennadevicetype);
		radioAntenna.setSupplier(ManufacturerConstant.HUAWEI);
		radioAntenna.setName(antennaname);
		radioAntenna.setImporterConnector(ImporterConnector.Huawei_M2000);
		
		/***** default values ************/
		radioAntenna.setAzimuth(10f);
		radioAntenna.setLandHeight(10f);
		radioAntenna.setVswr(10f);
		radioAntenna.setMechanicalDowntilt(10f);
		radioAntenna.setSector(10l);
		radioAntenna.setSurfaceHeight(10f);
		
		antennaMap.put(antennakey, radioAntenna);
		
		return radioAntenna;

	}
	/**
	 * @param startElement
	 * @param neId
	 * @param neName
	 * @throws ParseException
	 */
	public boolean createBoard(boolean bExist, StartElement startElement, String neId, String neName, String neType,
			String type, Map<String, String> subSlotNo2And3Map, Map<String, String> boardUPIUMap)
			throws ParseException {
        try
        {
		String slotIndex = null;
		String indexOnSlot = null;
		String partNb = null;
		String issueNb = null;
		String serialNb = null;
		String manufacturerDate = null;
		String description = null;
		String boardTypeCode = null;

		String cabinetIndex = null;
		String shelfIndex = null;
		String boardType = null;
		String boardName = null;

		NodeSlot nodeSlot = null;
		NodeBoard nodeBoard = null;
		String basicBoardType = null;
		String subSlotNo = null;
		String hardwareVersion = null;
		
		// get SoftVer for NeModel if NEType=RNC
		if (softVersion != null)
			softVersion = null;
			
		Attribute softVerNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("softVer")));
		if ((softVersion == null || softVersion.isEmpty()) && softVerNbAttribute != null) {
			softVersion = softVerNbAttribute.getValue();
		}
		
		Attribute hardwareVersionNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("hardwareVersion")));
		if ( (hardwareVersion == null || hardwareVersion.isEmpty()) && hardwareVersionNbAttribute != null) {
			hardwareVersion = hardwareVersionNbAttribute.getValue();
		}
		
		Attribute basicBoardTypeAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("boardType")));
		if (basicBoardTypeAttribute != null) {
			basicBoardType = basicBoardTypeAttribute.getValue();
		}

		// get indexOnSlot
		String basicSlotPos = null;
		Attribute indexOnSlotAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("slotPos")));
		if (indexOnSlotAttribute != null) {
			basicSlotPos = indexOnSlotAttribute.getValue();
		}

		indexOnSlot = deducedIndexOnSlot(basicSlotPos, basicBoardType, neType);
		if (indexOnSlot == null || indexOnSlot.isEmpty())
			return bExist;

		// get cabinetId
		Attribute cabinetIdAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("rackNo")));
		if (cabinetIdAttribute != null) {
			cabinetIndex = cabinetIdAttribute.getValue();
		}
		if (cabinetIndex == null) {
			Attribute cabinetNoAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("CabinetNo")));
			if (cabinetNoAttribute != null) {
				cabinetIndex = cabinetNoAttribute.getValue();
			}
		}
		if (cabinetIndex == null || cabinetIndex.isEmpty())
			return bExist;

		// get shelfId
		Attribute shelfIdAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("frameNo")));
		if (shelfIdAttribute != null) {
			shelfIndex = shelfIdAttribute.getValue();
		}
		if (shelfIndex == null) {
			Attribute subrackNoAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("SubrackNo")));
			if (subrackNoAttribute != null) {
				shelfIndex = subrackNoAttribute.getValue();
			}
		}
		if (shelfIndex == null || shelfIndex.isEmpty())
			return bExist;

		// board Name
		Attribute boardNameAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("boardName")));
		if (boardNameAttribute != null) {
			boardName = boardNameAttribute.getValue();
		}

		// get slotIndex
		String basicSlotIndex = null;
		Attribute slotIndexAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("slotNo")));
		if (slotIndexAttribute != null) {
			basicSlotIndex = slotIndexAttribute.getValue();
		}

		slotIndex = deducedSlotIndex(basicSlotIndex, basicBoardType, neId, cabinetIndex, boardName);

		// SubSlotNo
		Attribute subSlotNoAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("subSlotNo")));
		if (subSlotNoAttribute != null) {
			subSlotNo = subSlotNoAttribute.getValue();
		}
		if (subSlotNo != null && !subSlotNo.equals("-1")) {
			if ((subSlotNo.equals("2") || subSlotNo.equals("3")) && !boardName.matches(m2v2r0Patterns.get("fModuleBoard")) && basicSlotIndex != null && !basicSlotIndex.isEmpty() && basicSlotPos != null && !basicSlotPos.isEmpty())
				subSlotNo2And3Map.put(cabinetIndex + "_" + shelfIndex + "_" + basicSlotIndex + "_" + basicSlotPos + "_" + subSlotNo, boardName);

			return bExist;
		}

		if (slotIndex == null || slotIndex.isEmpty())
			return bExist;

		// get boardTypeCode
		boardTypeCode = deducedBoardTypeCode(basicBoardType, neId, cabinetIndex, boardName, subSlotNo);

		if (basicBoardType == null || basicBoardType.isEmpty())
			boardType = boardName;
		else
			boardType = basicBoardType;

		// get partNb
		partNb = deducedPartNb(startElement, neId, boardType, cabinetIndex, shelfIndex, slotIndex);

		// get description
		Attribute descriptionAttribute = startElement
				.getAttributeByName(new QName(m2v2r0Patterns.get("manufacturerData")));
		if (descriptionAttribute != null) {
			description = descriptionAttribute.getValue();
		}

		// get manufacturerDate
		Attribute manufacturerDateAttribute = startElement
				.getAttributeByName(new QName(m2v2r0Patterns.get("dateOfManufacture")));
		if (manufacturerDateAttribute != null) {
			manufacturerDate = manufacturerDateAttribute.getValue();
		}

		// get serialNb
		Attribute serialNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("serialNumber")));
		if (serialNbAttribute != null) {
			serialNb = serialNbAttribute.getValue();
		}

		// get issueNb
		Attribute issueNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("issueNumber")));
		if (issueNbAttribute != null) {
			issueNb = issueNbAttribute.getValue();
		}

		String shelfId = multiPhysicalDumpParser.getCorrespandantDump(neId) + "_" + cabinetIndex + "_" + shelfIndex;
		String slotId = shelfId + "_" + slotIndex;
		String boardId = slotId + "_" + indexOnSlot;

		// ignore BoardName "FModule"
		if (boardName == null || !boardName.matches(m2v2r0Patterns.get("fModuleBoard"))) {

			if (!nodeSlotMap.containsKey(slotId)) {
				nodeSlot = new NodeSlot();

				nodeSlot.setId(slotId);

				NodeShelf nodeShelf = mapShelf.get(shelfId);
				nodeSlot.setShelf(nodeShelf);

				nodeSlot.setNodeId(neId);

				try {
					nodeSlot.setSlotIndex(Integer.parseInt(slotIndex));
				} catch (Exception e) {
					log.error("slotIndex should be integer for NodeSlot: " + slotId);
				}

				nodeSlot.setImporterConnector(ImporterConnector.Huawei_M2000);
			} else
				nodeSlot = nodeSlotMap.get(slotId);

			// multiple boards found
			if (nodeBoardMap.containsKey(boardId) && "0".equals(basicSlotPos)) {
				nodeBoardMap.remove(boardId);
				List<NodeBoard> boards = (List<NodeBoard>) nodeSlot.getNodeBoards();
				Iterator<NodeBoard> it = boards.iterator();
				while (it.hasNext()) {
					NodeBoard nodeBoardInSlot = it.next();

					if (nodeBoardInSlot.getId().equals(boardId)) {
						it.remove();
					}
				}
			}

			if (!nodeBoardMap.containsKey(boardId)) {
				nodeBoard = new NodeBoard();

				nodeBoard.setId(boardId);

				nodeBoard.setBoardTypeCode(NodeContainer.getBoardDictionnaryForName(boardTypeCode));
				nodeBoard.setModel(boardTypeCode);

				if (boardTypeCode.matches(m2v2r0Patterns.get("specificBoardMatching")))
					specificBoardMatchingMap.put(boardId, boardTypeCode);

				nodeBoard.setPartNumber(partNb);
				nodeBoard.setBoardSerialNumber(serialNb);
				nodeBoard.setDescription(description);
				if(hardwareVersion == null)
					hardwareVersion = "null";
				nodeBoard.setHardwareVersion(hardwareVersion);
				if(softVersion == null || softVersion.isEmpty())
					softVersion = "null";
				nodeBoard.setSoftwareVersion(softVersion);

				if (indexOnSlot != null) {
					try {
						nodeBoard.setBoardIndexOnSlot(new Integer(indexOnSlot));
					} catch (Exception e) {
						log.error("index on slot should be integer for Board: " + boardId);
					}
				}

				if (issueNb != null && !issueNb.isEmpty()) {
					try {
						nodeBoard.setIssueNumber(new Integer(issueNb));
					} catch (Exception e) {
						log.error("issue Nb should be integer for Board: " + boardId);
					}
				}

				Date manufactDate = null;
				if (manufacturerDate != null && !manufacturerDate.isEmpty()) {
					manufactDate = flexibleDateParser.parseDate(manufacturerDate);
				}

				nodeBoard.setManufacturingDate(manufactDate);

				nodeBoard.setManufacturerName("HUAWEI");
				nodeBoard.setSlotId(slotId);

				List<NodeBoard> boards;
				boards = (List<NodeBoard>) nodeSlot.getNodeBoards();

				boards.add(nodeBoard);
				nodeSlot.setNodeBoards(boards);

				nodeBoardMap.put(nodeBoard.getId(), nodeBoard);
				nodeBoardsForMatching.put(neId + "_" + shelfIndex + "_" + slotIndex + "_" + indexOnSlot, nodeBoard);
				nodeBoardsForMatching.put(neId + "_" + shelfIndex + "_" + slotIndex, nodeBoard);
				nodeBoardsForMatching.put(neName + "_" + slotIndex, nodeBoard);
				nodeBoardsForMatching.put(serialNb, nodeBoard);

				if (boardName != null && boardName.equalsIgnoreCase(m2v2r0Patterns.get("boardUPIU")))
					boardUPIUMap.put(cabinetIndex + "_" + shelfIndex + "_" + basicSlotIndex + "_" + basicSlotPos,
							nodeBoard.getId());

				NodeShelf nodeShelf = mapShelf.get(shelfId);
				if (nodeShelf != null)
					nodeBoardsForMatching.put(neId + "_" + nodeShelf.getModel() + "_" + slotIndex, nodeBoard);

				if (type != null && (type.matches(m2v2r0Patterns.get("nodeB")) || type.matches(m2v2r0Patterns.get("ranBs"))) && boardTypeCode.matches(m2v2r0Patterns.get("wmptBoardForInterface")))
					nodeBoardsForMatching.put(neId.toUpperCase(), nodeBoard);

				if (type != null && (type.matches(m2v2r0Patterns.get("bts")) || type.matches(m2v2r0Patterns.get("ranBs"))) && boardTypeCode.matches(m2v2r0Patterns.get("gtmuBoardForInterface")))
					nodeBoardsForMatching.put(neId.toUpperCase(), nodeBoard);
			}

			if (!nodeSlotMap.containsKey(nodeSlot.getId())) {
				nodeSlotMap.put(nodeSlot.getId(), nodeSlot);
				nodeSlotForMatching.put(nodeSlot.getId(), nodeSlot);

				String key4SpuBoards = neId + "_" + shelfIndex + "_" + slotIndex;
				if (!map4SpuBoards.containsKey(key4SpuBoards))
					map4SpuBoards.put(key4SpuBoards, nodeSlot);

				setElementsIdByNodeIdMap(slotsIdByNeIdMap, nodeSlot.getId(), neId);
			}

			bExist = true;
			return bExist;
		} 
		else {
			// if BoardName="FModule" : this is NodeInterface
			if(!type.contains("RAN-BS"))
					createNodeInterface(startElement, boardId, description, serialNb, manufacturerDate, neId, type, neType);
			return true;
		}
	}
	catch(Exception e){
		e.printStackTrace();
		System.out.println();
	}
        return true;
	}

	/**
	 * 
	 * @param startElement
	 * @param basicBoardType
	 * @param neType
	 * @return
	 */
	private String deducedIndexOnSlot(String basicSlotPos, String basicBoardType, String neType) {

		if (neType != null) {
			if (isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu")) || isNeTypeMatched(neType, "(?i)BSC6910GU")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gsm"))
					|| isNeTypeMatched(neType, "(?i)BSC6910GSM")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Umts"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("enodeB"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsUl"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGl")) || isNeTypeMatched(neType, "(?i)MBTS")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("mBtsGu"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("gsmBts"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("nodeB"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6000Bts"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts")) || isNeTypeMatched(neType,"(?i)MICROBTS3900")
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6000"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("rnc"))
					|| isNeTypeMatched(neType, m2v2r0Patterns.get("bts3900"))
					|| ((isNeTypeMatched(neType, "(?i)BTS59005G")
					|| isNeTypeMatched(neType, "(?i)BTS5900")) && StringUtils.equalsIgnoreCase(m2v2r0Config.getProperty("stop.antenna.parsing"), "false"))
					|| isNeTypeMatched(neType, "(?i)MBTS\\(GUL\\)") || isNeTypeMatched(neType, "(?i)BSC6910UMTS"))
				return "0";
		}

		String indexOnSlot = basicSlotPos;

		if (basicBoardType != null) {
			if (basicBoardType.equalsIgnoreCase(m2v2r0Patterns.get("boardCn21smu1")) || basicBoardType.equalsIgnoreCase(m2v2r0Patterns.get("boardCn2e02ffb")))
				indexOnSlot = "0";
			else if (basicBoardType.equalsIgnoreCase(m2v2r0Patterns.get("boardCn21sdmc0")) || basicBoardType.equalsIgnoreCase(m2v2r0Patterns.get("boardCn2e00pem")))
				indexOnSlot = "1";
		}

		if (indexOnSlot == null || indexOnSlot.isEmpty())
			indexOnSlot = "0";

		return indexOnSlot;
	}

	/**
	 * 
	 * @param startElement
	 * @param neId
	 * @param boardType
	 * @param cabinetIndex
	 * @param shelfIndex
	 * @param slotIndex
	 * @return
	 */
	private String deducedPartNb(StartElement startElement, String neId, String boardType, String cabinetIndex, String shelfIndex, String slotIndex) {

		String partNb = null;

		Attribute partNbAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("bomCode")));
		if (partNbAttribute != null) {
			partNb = partNbAttribute.getValue();
		}

		if (partNb == null || partNb.isEmpty()) {
			Attribute itemAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("item")));
			if (itemAttribute != null) {
				partNb = itemAttribute.getValue();
			} else {
				if (boardType != null && !boardType.isEmpty()) {
					partNb = boardType + "(UndefinedPartNumber)";
				} else if (!shelfIndex.isEmpty() || !cabinetIndex.isEmpty()) {
					ErrorAlarm error = new ErrorAlarm(Category.MISSING_DATA);
					error.setMessage("missing board information for nodeSlot (" + neId + "_" + cabinetIndex + "_" + shelfIndex + "_" + slotIndex + ").");
					error.setNeType(NptConstants.NODE_SLOT);
					error.setType(ErrorAlarm.NETWORK_ERROR);
					NodeContainer.addErrorToList(error);
				}
			}
		}
		return partNb;
	}

	/**
	 * 
	 * @param basicSlotIndex
	 * @param basicBoardType
	 * @param neId
	 * @param cabinetIndex
	 * @param boardName
	 * @return
	 */
	private String deducedSlotIndex(String basicSlotIndex, String basicBoardType, String neId, String cabinetIndex, String boardName) {

		if (basicSlotIndex == null || basicSlotIndex.isEmpty() || basicSlotIndex.equals("221"))
			return null;

		String slotIndexFromBoardType = slotIndexMatchingParser.getMatchedSlotIndex(basicBoardType + "_" + basicSlotIndex);

		if (slotIndexFromBoardType != null)
			return slotIndexFromBoardType;
		else {
			for (NodeCabinet cab : currentCabinetsList) {
				if (cab.getId().equals(neId + "_" + cabinetIndex)) {
					String rackType = cab.getModel();
					slotIndexFromBoardType = slotIndexMatchingParser.getMatchedSlotIndexWithRackType(rackType + "_" + boardName + "_" + basicSlotIndex);

					if (slotIndexFromBoardType != null) {
						return slotIndexFromBoardType;
					}
				}
			}
		}

		return basicSlotIndex;
	}

	/**
	 * 
	 * @param basicBoardType
	 * @param neId
	 * @param cabinetIndex
	 * @param boardName
	 * @return
	 */
	private String deducedBoardTypeCode(String basicBoardType, String neId, String cabinetIndex, String boardName, String subSlotNo) {

		String boardTypeCode = basicBoardType;
		String boardTypeMatched = null;

		for (NodeCabinet cab : currentCabinetsList) {
			if (cab.getId().equals(neId + "_" + cabinetIndex)) {
				String rackType = cab.getModel();

				boardTypeMatched = modelMatchingParser.getBoardMatchedModel(rackType + "_" + boardName);

				if (boardTypeMatched == null)
					continue;

				break;
			}
		}

		if (boardTypeMatched != null) {
			boardTypeCode = boardTypeMatched;
		} else if (boardTypeCode == null || boardTypeCode.isEmpty()) {
			boardTypeCode = boardName;
		}

		if (boardTypeCode != null) {
			if (boardTypeMatching.getBoardType(boardTypeCode.toUpperCase()) != null) {
				boardTypeCode = boardTypeMatching.getBoardType(boardTypeCode.toUpperCase()).getBoardTypeCode();
			} else
				boardTypeCode = boardTypeCode.toUpperCase();
		}

		return boardTypeCode;
	}

	/**
	 * 
	 * @param startElement
	 * @param boardId
	 * @param description
	 * @param neId
	 */
	public void createNodeInterface(StartElement startElement, String boardId, String description, String serialNb, String manufacturerDate, String neId, String type, String neType) {

		String interfaceIndex = null;
		String interfaceModel = null;
		NodeInterface nodeInterface = null;
		String partNumber = null;

		// get interfaceIndex
		Attribute portNoAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("portNo")));
		if (portNoAttribute != null) {
			interfaceIndex = portNoAttribute.getValue();
		} else {
			Attribute unitPositionAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("unitPosition")));
			if (unitPositionAttribute != null) {
				String unitPosValue = unitPositionAttribute.getValue();

				Matcher interfaceIndexMatcher = Pattern.compile(m2v2r0Patterns.get("unitPositionPat")).matcher(unitPosValue);

				if (interfaceIndexMatcher.find())
					interfaceIndex = interfaceIndexMatcher.group(2);
			}
		}

		// get part Number
		Attribute bomCodeAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("bomCode")));
		if (bomCodeAttribute != null) {
			partNumber = bomCodeAttribute.getValue();
		}

		// get boardType
		Attribute InterfaceModelAttribute = startElement.getAttributeByName(new QName(m2v2r0Patterns.get("boardType")));
		if (InterfaceModelAttribute != null) {
			interfaceModel = InterfaceModelAttribute.getValue();
		}

		String interfaceId = boardId + "_" + interfaceIndex;

		if (interfaceModel != null && !interfaceModel.isEmpty() && !nodeInterfaceMap.containsKey(interfaceId) && interfaceIndex != null && !interfaceIndex.isEmpty()) {
			nodeInterface = new NodeInterface();
			nodeInterface.setId(interfaceId);

			try {
				nodeInterface.setInterfaceIndex(new Integer(interfaceIndex));
			} catch (Exception e) {
				log.error("Error: interface Index should be Integer for Interface: " + interfaceId);
			}

			// nodeInterface.setSfpdescription(description);
			nodeInterface.setNodeBoardId(boardId);
			nodeInterface.setPartnumber(partNumber);
			nodeInterface.setSerial(serialNb);
			nodeInterface.setTransceiverDesc(description);
			nodeInterface.setSfpdescription(interfaceModel);
			
			nodeInterfaceMap.put(interfaceId, nodeInterface);
		}
	}

	/**
	 * 
	 * @param map
	 * @param elementId
	 * @param neId
	 */
	public void setElementsIdByNodeIdMap(Map<String, Set<String>> map, String elementId, String neId) {

		Set<String> elementsId = map.get(neId);
		if (elementsId == null)
			elementsId = new HashSet<String>();

		elementsId.add(elementId);
		map.put(neId, elementsId);
	}

	/**
	 * 
	 * @param neId
	 */
	public void removeCabinetsFromMap(String neId) {

		Set<String> cabinetsId = cabinetsIdByNeIdMap.get(neId);
		if (cabinetsId != null) {
			Iterator<String> itr = cabinetsId.iterator();
			Map<String, NodeCabinet> cabinets = NodeContainer.getNodeCabinetsMap();
			while (itr.hasNext()) {
				String cabinetId = itr.next();
				if (cabinets.containsKey(cabinetId))
					cabinets.remove(cabinetId);
			}
		}
	}

	/**
	 * 
	 * @param neId
	 */
	public void removeShelfsFromMap(String neId) {

		Set<String> shelfsId = shelfsIdByNeIdMap.get(neId);
		if (shelfsId != null) {
			Iterator<String> itr = shelfsId.iterator();
			Map<String, NodeShelf> shelfs = NodeContainer.getNodeShelfsMap();
			while (itr.hasNext()) {
				String shelfId = itr.next();
				if (shelfs.containsKey(shelfId))
					shelfs.remove(shelfId);
			}
		}
	}

	/**
	 * 
	 * @param neId
	 */
	public void removeSlotsFromMap(String neId) {

		Set<String> slotsId = slotsIdByNeIdMap.get(neId);
		if (slotsId != null) {
			Iterator<String> itr = slotsId.iterator();
			while (itr.hasNext()) {
				String slotId = itr.next();
				if (nodeSlotMap.containsKey(slotId))
					nodeSlotMap.remove(slotId);
			}
		}
	}

	/**
	 * 
	 * @param boardUPIUMap
	 * @param subSlotNo2And3Map
	 */
	private void setBoardTypeForUPIUboards(Map<String, String> boardUPIUMap, Map<String, String> subSlotNo2And3Map) {

		Pattern pat = Pattern.compile("^(.*)(_\\d$)");

		for (String key : boardUPIUMap.keySet()) {
			try {
				String boardName = null;
				String boardTypeCode = m2v2r0Patterns.get("boardUPIU");
				String boardId = boardUPIUMap.get(key);

				String nodeSlotId = null;

				Matcher m = pat.matcher(boardId);
				if (m.find())
					nodeSlotId = m.group(1);

				if (nodeSlotId == null)
					continue;

				NodeSlot nodeSlot = nodeSlotMap.get(nodeSlotId);

				if (nodeSlot == null)
					continue;

				boardName = subSlotNo2And3Map.get(key + "_2");
				if (boardName != null) {
					boardTypeCode = boardTypeCode + "/" + boardName;
				}

				boardName = subSlotNo2And3Map.get(key + "_3");
				if (boardName != null) {
					boardTypeCode = boardTypeCode + "/" + boardName;
				}

				Collection<NodeBoard> nodeBoard = nodeSlot.getNodeBoards();

				for (NodeBoard board : nodeBoard) {
					if (boardId.equalsIgnoreCase(board.getId())) {
						board.setBoardTypeCode(NodeContainer.getBoardDictionnaryForName(boardTypeCode));
						board.setModel(boardTypeCode);
					}
				}
			} catch (Exception e) {
				log.error("Error", e);
			}
		}
	}

	/**
	 * 
	 */
	public void updateSpecificBoardType() {

		Pattern pat = Pattern.compile("^(.*)(_\\d$)");

		for (String boardId : specificBoardMatchingMap.keySet()) {
			try {
				String nodeSlotId = null;
				String boardTypeCode = specificBoardMatchingMap.get(boardId);

				Matcher m = pat.matcher(boardId);
				if (m.find())
					nodeSlotId = m.group(1);

				if (nodeSlotId == null && boardTypeCode == null)
					continue;

				NodeSlot nodeSlot = nodeSlotMap.get(nodeSlotId);

				if (nodeSlot == null)
					continue;

				String neModel = null;
				String neId = nodeSlot.getNodeId();

				if (neId != null && nodeTypeByNeIdMap.get(neId) != null) {
					String nodeType = nodeTypeByNeIdMap.get(neId);

					if (nodeType.matches(m2v2r0Patterns.get("bts"))) {
						ElementBs bts = btsMap.get(neId);
						if (bts != null)
							neModel = bts.getType();
					} else if (nodeType.matches(m2v2r0Patterns.get("bsc"))) {
						ElementBsc bsc = bscMap.get(neId);
						if (bsc != null)
							neModel = bsc.getType();
					} else if (nodeType.matches(m2v2r0Patterns.get("nodeB"))) {
						ElementNb nodeB = nodeBMap.get(neId);
						if (nodeB != null)
							neModel = nodeB.getType();
					} else if (nodeType.matches(m2v2r0Patterns.get("enodeB"))) {
						ElementENodeB enodeB = eNodeBMap.get(neId);
						if (enodeB != null)
							neModel = enodeB.getType();
					} else if (nodeType.matches(m2v2r0Patterns.get("ranBs"))) {
						ElementRanBs ranBs = ranBsMap.get(neId);
						if (ranBs != null)
							neModel = ranBs.getType();
					} else if (nodeType.matches(m2v2r0Patterns.get("rnc"))) {
						ElementRnc rnc = rncMap.get(neId);
						if (rnc != null)
							neModel = rnc.getType();
					}
				}

				Collection<NodeBoard> nodeBoard = nodeSlot.getNodeBoards();

				for (NodeBoard board : nodeBoard) {
					if (neModel != null && boardId.equalsIgnoreCase(board.getId())) {
						if (boardTypeMatching.getBoardForModel(boardTypeCode.toUpperCase(), neModel) != null) {
							boardTypeCode = boardTypeMatching.getBoardForModel(boardTypeCode.toUpperCase(), neModel)
									.getBoardTypeCode();
						} else
							boardTypeCode = boardTypeCode.toUpperCase();

						board.setBoardTypeCode(NodeContainer.getBoardDictionnaryForName(boardTypeCode));
						board.setModel(boardTypeCode);
					}
				}
			} catch (Exception e) {
				log.error("Error", e);
			}
		}
	}

	private void createRoles(Map<String, Set<String>> discardedRanBsMap) {

		for (String serialNb : bbu3900SerialNbMap.keySet()) {
			
			String neType = null;
			
			Set<String> neTypeSet = neTypeBbu3900SerialNbMap.get(serialNb);
			for (String neTp : neTypeSet) {
				neType = neTp;
			}
			
			String mbtsId = null;
			String gsmbtsId = null;
			
			String nodeBId = null;
			String enodeBId = null;
			String gnodeBId = null;
			Set<String> bts3900Ids = new HashSet<String>();

			Set<String> neIdSet = bbu3900SerialNbMap.get(serialNb);
			

			for (String neId : neIdSet) {

				if (neId.endsWith("_MBTS"))
					mbtsId = neId.split("_MBTS")[0];

				if (neId.endsWith("_gsmBts"))
					gsmbtsId = neId.split("_gsmBts")[0];

				if (neId.endsWith("_nodeB"))
					nodeBId = neId.split("_nodeB")[0];
				
				if (neId.endsWith("_enodeB"))
					enodeBId = neId.split("_enodeB")[0];
				
				if (neId.endsWith("_gnodeb"))
					gnodeBId = neId.split("_gnodeb")[0];
				
				if (neId.endsWith("_bts3900")) {
					String bts3900Id = neId.split("_bts3900")[0];
					bts3900Ids.add(bts3900Id);
				}
			}
						// MBTS file exists
			if (mbtsId != null || ranBsMap.get(mbtsId) != null) {
				
				ElementRanBs elementRanBs = ranBsMap.get(mbtsId);
				
				if(StringUtils.isNotBlank(gnodeBId)) {
					ranbsRelationMap.put(gnodeBId.toUpperCase(), elementRanBs.getId());
					
//					ElementGNb elementgnb = (ElementGNb) addNode(new ElementGNb(), gnodeBId, gnodeBId, elementRanBs.getType(), elementRanBs.getSite(), null, null, PossibleNodeType.gNodeB);
//					
//					elementRanBs.setNrCapability(elementgnb);

					ranBsMap.remove(gnodeBId);
					removeCabinetsFromMap(gnodeBId);
					removeShelfsFromMap(gnodeBId);
					removeSlotsFromMap(gnodeBId);
					if(discardedRanBsMap.get(mbtsId) == null)
						discardedRanBsMap.put(mbtsId, new HashSet<String>());
					discardedRanBsMap.get(mbtsId).add(gnodeBId);
				}
				
				// 2G role name to be deduced from NEName in GSMBTS
				if (gsmbtsId != null && btsMap.get(gsmbtsId) != null) {
					ElementBs elementBts = btsMap.get(gsmbtsId);
					elementRanBs.setGsmCapability(elementBts);
					btsMap.remove(gsmbtsId);
					removeCabinetsFromMap(gsmbtsId);
					removeShelfsFromMap(gsmbtsId);
					removeSlotsFromMap(gsmbtsId);
					ranBsIdByRoleIdMap.put(gsmbtsId.toUpperCase(), mbtsId);
				}
				
				if(StringUtils.isBlank(gsmbtsId)) {
					String gbtsName = gbtsFunctionNameByneIdMap.get(mbtsId);
					if (gbtsName != null && btsMap.get(gbtsName) != null) {
						ElementBs elementbs = btsMap.get(gbtsName);
						elementRanBs.setGsmCapability(elementbs);
	
						btsMap.remove(gbtsName);
						removeCabinetsFromMap(gbtsName);
						removeShelfsFromMap(gbtsName);
						removeSlotsFromMap(gbtsName);
	
						ranBsIdByRoleIdMap.put(gbtsName.toUpperCase(), mbtsId);
					} else if (gbtsName != null && btsMap.get(gbtsName) == null) {
						ElementBs elementbs = (ElementBs) addNode(new ElementBs(), gbtsName, gbtsName, elementRanBs.getType(), elementRanBs.getSite(), null, null, PossibleNodeType.BTS);
						elementbs.setParentBsc(parentBscMap.get(elementbs.getName()));
						elementRanBs.setGsmCapability(elementbs);
						ranBsIdByRoleIdMap.put(gbtsName.toUpperCase(), mbtsId);
					}
					
					if(StringUtils.isBlank(gbtsName)) {
						for (String bts3900Id : bts3900Ids) {
							gbtsName = gbtsFunctionNameByneIdMap.get(bts3900Id);
							break;
						}
						if (gbtsName != null && btsMap.get(gbtsName) != null) {
							ElementBs elementbs = btsMap.get(gbtsName);
							elementRanBs.setGsmCapability(elementbs);
	
							btsMap.remove(gbtsName);
							removeCabinetsFromMap(gbtsName);
							removeShelfsFromMap(gbtsName);
							removeSlotsFromMap(gbtsName);
	
							ranBsIdByRoleIdMap.put(gbtsName.toUpperCase(), mbtsId);
						}
						if (gbtsName != null && btsMap.get(gbtsName) == null) {
							ElementBs elementbs = (ElementBs) addNode(new ElementBs(), gbtsName, gbtsName, elementRanBs.getType(), elementRanBs.getSite(), null, null, PossibleNodeType.BTS);
							elementbs.setParentBsc(parentBscMap.get(elementbs.getName()));
							elementRanBs.setGsmCapability(elementbs);
							ranBsIdByRoleIdMap.put(gbtsName.toUpperCase(), mbtsId);
						} 
					}
				}
				
				// 3G role name to be deduced from NodeBFunctionName in the MBTS
				// or if empty then from NodeBFunctionName in the BTS3900 or
				// from NEName in NodeB file
				String nodebName = nodeBFunctionNameByNeIdMap.get(mbtsId);

				if (nodebName != null && nodeBMap.get(nodebName) != null) {
					ElementNb elementNb = nodeBMap.get(nodebName);
					elementRanBs.setUmtsCapability(elementNb);

					nodeBMap.remove(nodebName);
					removeCabinetsFromMap(nodebName);
					removeShelfsFromMap(nodebName);
					removeSlotsFromMap(nodebName);

					ranBsIdByRoleIdMap.put(nodebName.toUpperCase(), mbtsId);
				} else if (nodebName != null && nodeBMap.get(nodebName) == null) {
					ElementNb elementNodeB = (ElementNb) addNode(new ElementNb(), nodebName, nodebName, elementRanBs.getType(), elementRanBs.getSite(), null, null, PossibleNodeType.NodeB);
					elementNodeB.setParentRnc(parentRncMap.get(elementNodeB.getName()));
					elementRanBs.setUmtsCapability(elementNodeB);
					ranBsIdByRoleIdMap.put(nodebName.toUpperCase(), mbtsId);
				}
				  
				if (nodebName == null) {

					for (String bts3900Id : bts3900Ids) {
						nodebName = nodeBFunctionNameByNeIdMap.get(bts3900Id);
						break;
					}
					if (nodebName != null && nodeBMap.get(nodebName) != null) {
						ElementNb elementNb = nodeBMap.get(nodebName);
						elementRanBs.setUmtsCapability(elementNb);

						nodeBMap.remove(nodebName);
						removeCabinetsFromMap(nodebName);
						removeShelfsFromMap(nodebName);
						removeSlotsFromMap(nodebName);

						ranBsIdByRoleIdMap.put(nodebName.toUpperCase(), mbtsId);
					}
					if (nodebName != null && nodeBMap.get(nodebName) == null) {
						ElementNb elementNodeB = (ElementNb) addNode(new ElementNb(), nodebName, nodebName,
								elementRanBs.getType(), elementRanBs.getSite(), null, null, PossibleNodeType.NodeB);
						elementNodeB.setParentRnc(parentRncMap.get(elementNodeB.getName()));
						elementRanBs.setUmtsCapability(elementNodeB);
						ranBsIdByRoleIdMap.put(nodebName.toUpperCase(), mbtsId);
					} else {
						if (nodeBId != null && nodeBMap.get(nodeBId) != null) {
							ElementNb elementNb = nodeBMap.get(nodeBId);
							elementRanBs.setUmtsCapability(elementNb);

							nodeBMap.remove(nodeBId);
							removeCabinetsFromMap(nodeBId);
							removeShelfsFromMap(nodeBId);
							removeSlotsFromMap(nodeBId);
							ranBsIdByRoleIdMap.put(nodeBId.toUpperCase(), mbtsId);
						}
					
					}
				}
				
				if (enodeBId != null && eNodeBMap.get(enodeBId) != null) {
					ElementENodeB enodeB = eNodeBMap.get(enodeBId);
					elementRanBs.setLteCapability(enodeB);

					eNodeBMap.remove(enodeBId);
					removeCabinetsFromMap(enodeBId);
					removeShelfsFromMap(enodeBId);
					removeSlotsFromMap(enodeBId);
					ranBsIdByRoleIdMap.put(enodeBId.toUpperCase(), mbtsId);
				}
				
				String enodeBName = enodeBFunctionNameByneIdMap.get(mbtsId);
				if (enodeBName != null) {
					ElementENodeB elementEnodeB = (ElementENodeB) addNode(new ElementENodeB(), enodeBName, enodeBName, elementRanBs.getType(), elementRanBs.getSite(), null, null, PossibleNodeType.eNodeB);
					elementRanBs.setLteCapability(elementEnodeB);
					ranBsIdByRoleIdMap.put(enodeBName.toUpperCase(), mbtsId);
				} else {
					for (String bts3900Id : bts3900Ids) {
						enodeBName = enodeBFunctionNameByneIdMap.get(bts3900Id);
						break;
					}

					if (enodeBName != null) {
						ElementENodeB elementEnodeB = (ElementENodeB) addNode(new ElementENodeB(), enodeBName, enodeBName, elementRanBs.getType(), elementRanBs.getSite(), null, null, PossibleNodeType.eNodeB);
						elementRanBs.setLteCapability(elementEnodeB);
						ranBsIdByRoleIdMap.put(enodeBName.toUpperCase(), mbtsId);
					}

				}

				ranBsMap.put(mbtsId, elementRanBs);
				for (String bts3900Id : bts3900Ids) {
					if (!bts3900Id.equalsIgnoreCase(mbtsId)) {
						removeCabinetsFromMap(bts3900Id);
						removeShelfsFromMap(bts3900Id);
						removeSlotsFromMap(bts3900Id);
						ranBsMap.remove(bts3900Id);
						ranbsRelationMap.put(bts3900Id.toUpperCase(), elementRanBs.getId());
						
						
						if(discardedRanBsMap.get(mbtsId) == null)
							discardedRanBsMap.put(mbtsId, new HashSet<String>());
						discardedRanBsMap.get(mbtsId).add(bts3900Id);
						
					}
				}
				if (gsmbtsId != null) {
					removeCabinetsFromMap(gsmbtsId);
					removeShelfsFromMap(gsmbtsId);
					removeSlotsFromMap(gsmbtsId);
					btsMap.remove(gsmbtsId);
					ranbsRelationMap.put(gsmbtsId.toUpperCase(), elementRanBs.getId());
					
				}
				if (nodeBId != null) {
					removeCabinetsFromMap(nodeBId);
					removeShelfsFromMap(nodeBId);
					removeSlotsFromMap(nodeBId);
					nodeBMap.remove(nodeBId);
					ranbsRelationMap.put(nodeBId.toUpperCase(), elementRanBs.getId());
					
				}
			}

			else {
				if (gsmbtsId != null && !bts3900Ids.isEmpty()) {
					String bts3900Id = null;
					for (String id : bts3900Ids) {
						bts3900Id = id;
						break;
					}
					String id = gsmbtsId + "_" + bts3900Id;
					MbtsRelationData mbtsRelation = mbtsRelationMatchingParser.findMatch(id);
					if (mbtsRelation != null) {
						String neName = mbtsRelation.getSranBsName();
						String neModel = mbtsRelation.getGsmType().replace(" GSM", "_MS");
						ElementRanBs bts3900 = ranBsMap.get(bts3900Id);
						ElementBs elementBts = btsMap.get(gsmbtsId);

						ElementRanBs ranBs = (ElementRanBs) addNode(new ElementRanBs(), neName, neName, neModel,
								elementBts.getSite(), "2G/3G/4G", null, PossibleNodeType.RanBs);
						updatePhysicalConfig(neName, gsmbtsId, bts3900Id);
						if (bts3900 != null) {
							ranBs.setUmtsCapability(bts3900.getUmtsCapability());
							ranBs.setLteCapability(bts3900.getLteCapability());
							if (ranBs.getLteCapability() != null)
								ranBsIdByRoleIdMap.put(ranBs.getLteCapability().getName().toUpperCase(), neName);
							if (ranBs.getUmtsCapability() != null)
								ranBsIdByRoleIdMap.put(ranBs.getUmtsCapability().getName().toUpperCase(), neName);
						}
						ranBs.setGsmCapability(elementBts);
						ranBsIdByRoleIdMap.put(elementBts.getName().toUpperCase(), neName);
						if (elementBts != null) {
							removeCabinetsFromMap(gsmbtsId);
							removeShelfsFromMap(gsmbtsId);
							removeSlotsFromMap(gsmbtsId);
						}

						ranBsMap.put(neName, ranBs);
						for (String btsid : bts3900Ids) {
							if (!btsid.equalsIgnoreCase(neName)) {
								ranBsMap.remove(btsid);
								removeCabinetsFromMap(btsid);
								removeShelfsFromMap(btsid);
								removeSlotsFromMap(btsid);
							}
						}
						btsMap.remove(gsmbtsId);
					}

				} else if (gsmbtsId != null && nodeBId != null && bts3900Ids.isEmpty()) {
					String id = gsmbtsId + "_" + nodeBId;
					MbtsRelationData mbtsRelation = mbtsRelationMatchingParser.findMatch(id);
					if (mbtsRelation != null) {
						String neName = mbtsRelation.getSranBsName();
						String neModel = mbtsRelation.getGsmType().replace(" GSM", "_MS");

						ElementBs elementBts = btsMap.get(gsmbtsId);
						ElementNb elementNodeB = nodeBMap.get(nodeBId);
						ElementRanBs ranBs = (ElementRanBs) addNode(new ElementRanBs(), neName, neName, neModel,
								elementBts.getSite(), "2G/3G", null, PossibleNodeType.RanBs);
						ranBs.setGsmCapability(elementBts);
						ranBs.setUmtsCapability(elementNodeB);
						updatePhysicalConfig(neName, gsmbtsId, nodeBId);
						if (elementBts != null) {
							ranBsIdByRoleIdMap.put(elementBts.getName().toUpperCase(), neName);
							removeCabinetsFromMap(gsmbtsId);
							removeShelfsFromMap(gsmbtsId);
							removeSlotsFromMap(gsmbtsId);
						}
						if (elementNodeB != null) {
							ranBsIdByRoleIdMap.put(elementNodeB.getName().toUpperCase(), neName);
							removeCabinetsFromMap(nodeBId);
							removeShelfsFromMap(nodeBId);
							removeSlotsFromMap(nodeBId);
						}
						ranBsMap.put(neName, ranBs);
						btsMap.remove(gsmbtsId);
						nodeBMap.remove(nodeBId);
					}

				}
			}
			if (bts3900SerialNb.contains(serialNb) && bts3900Ids.isEmpty()) {
				// update here
				ElementBs elementBts = btsMap.get(gsmbtsId);
				ElementNb elementNodeB = nodeBMap.get(nodeBId);
				String neName = elementBts.getName();
				String neModel = elementBts.getType();
				if (neModel == null)
					neModel = neType;
				ElementRanBs ranBs = null;

				String ranBsName = ranBsIdByRoleIdMap.get(elementBts.getId());
				if (ranBsName != null) {
					ranBs = ranBsMap.get(ranBsName);
				}
				if (ranBs == null) {
					for (ElementRanBs r : ranBsMap.values()) {
						if (r.getName().contains(neName.toUpperCase().split("_")[0])) {
							ranBs = r;
							break;
						}

					}

				}
				if (ranBs != null) {
					if (elementBts != null)
						ranBs.setGsmCapability(elementBts);
					if (elementNodeB != null)
						ranBs.setUmtsCapability(elementNodeB);
					updatePhysicalConfig(ranBs.getName(), gsmbtsId, nodeBId);
				}
				
				else {
					ranBs = (ElementRanBs) addNode(new ElementRanBs(), neName, neName, neModel, elementBts.getSite(),"2G/3G/4G", null, PossibleNodeType.RanBs);
					if (elementBts != null)
						ranBs.setGsmCapability(elementBts);
					if (elementNodeB != null)
						ranBs.setUmtsCapability(elementNodeB);
					ranBsMap.put(neName, ranBs);
					updatePhysicalConfig(neName, gsmbtsId, nodeBId);
				}
				btsMap.remove(gsmbtsId);
				nodeBMap.remove(nodeBId);

			}
		}

		for (String id : nodesWithNoSerials) {
			ElementRanBs ranBs = null;
			MbtsRelationData mbtsRelation = null;
			boolean isGsmBts = false;
			boolean isNodeB = false;
			boolean isBts3900 = false;

			if (id.endsWith("_gsmBts")) {
				id = id.split("_gsmBts")[0];
				isGsmBts = true;
			}

			else if (id.endsWith("_nodeB")) {
				id = id.split("_nodeB")[0];
				isNodeB = true;
			} else if (id.endsWith("_bts3900")) {
				id = id.split("_bts3900")[0];
				isBts3900 = true;
			}

			if (isGsmBts)
				mbtsRelation = mbtsRelationMatchingParser.findMatchByGmbtsId(id);
			else if (isNodeB || isBts3900)
				mbtsRelation = mbtsRelationMatchingParser.findMatchByBts3900Id(id);
			if (mbtsRelation != null) {
				String neName = mbtsRelation.getSranBsName();
				ranBs = ranBsMap.get(neName);
				if (ranBs == null) {
					String neModel = mbtsRelation.getGsmType().replace(" GSM", "_MS");
					String site = null;
					if (isGsmBts)
						site = btsMap.get(id).getSite();
					else if (isNodeB)
						site = nodeBMap.get(id).getSite();
					else if (isBts3900)
						site = ranBsMap.get(id).getSite();

					ranBs = (ElementRanBs) addNode(new ElementRanBs(), neName, neName, neModel, site, "2G/3G/4G", null,
							PossibleNodeType.RanBs);
				} else {
					if (ranBs.getType() == null)
						ranBs.setType(mbtsRelation.getGsmType().replace(" GSM", "_MS"));
				}
				if (isGsmBts) {
					ElementBs elementBts = btsMap.get(id);
					if (elementBts != null) {
						if (ranBs.getGsmCapability() == null) {
							ranBs.setGsmCapability(elementBts);
							ranBsIdByRoleIdMap.put(elementBts.getName().toUpperCase(), neName);
						}
					}
				} else if (isNodeB) {
					ElementNb elementNb = nodeBMap.get(id);
					if (elementNb != null) {
						if (ranBs.getUmtsCapability() == null) {
							ranBs.setUmtsCapability(elementNb);
							ranBsIdByRoleIdMap.put(elementNb.getName().toUpperCase(), neName);
						}
					}
				} else if (isBts3900) {
					ElementRanBs bts3900 = ranBsMap.get(id);
					if (bts3900 != null) {
						if (ranBs.getGsmCapability() == null)
							ranBs.setGsmCapability(bts3900.getGsmCapability());
						if (ranBs.getUmtsCapability() == null)
							ranBs.setUmtsCapability(bts3900.getUmtsCapability());
						if (ranBs.getUmtsCapability() == null)
							ranBs.setLteCapability(bts3900.getLteCapability());
						if (ranBs.getLteCapability() != null)
							ranBsIdByRoleIdMap.put(ranBs.getLteCapability().getName().toUpperCase(), neName);
						if (ranBs.getUmtsCapability() != null)
							ranBsIdByRoleIdMap.put(ranBs.getUmtsCapability().getName().toUpperCase(), neName);
						if (ranBs.getGsmCapability() != null)
							ranBsIdByRoleIdMap.put(ranBs.getGsmCapability().getName().toUpperCase(), neName);
					}
				}
				ranBsMap.put(neName, ranBs);
				updateCabinets(neName, id);
				updateShelves(neName, id);
				updateSlots(neName, id);
				if (isGsmBts) {
					btsMap.remove(id);
					removeCabinetsFromMap(id);
					removeShelfsFromMap(id);
					removeSlotsFromMap(id);
				}
				if (isNodeB) {
					nodeBMap.remove(id);
					removeCabinetsFromMap(id);
					removeShelfsFromMap(id);
					removeSlotsFromMap(id);
				}
				if (isBts3900) {
					if (!neName.equalsIgnoreCase(id)) {
						ranBsMap.remove(id);
						removeCabinetsFromMap(id);
						removeShelfsFromMap(id);
						removeSlotsFromMap(id);
					}
				}
				removeCabinetsFromMap(id);
				removeShelfsFromMap(id);
				removeSlotsFromMap(id);
			} else {
				if (isBts3900) {
					ranBs = ranBsMap.get(id);
					if (ranBs != null) {
						ElementNb nb = ranBs.getUmtsCapability();
						String matchedRanBsId = null;
						ElementRanBs matchedRanBs = null;
						if (nb != null) {
							for (String neId : nodeBFunctionNameByNeIdMap.keySet()) {
								if (!neId.equalsIgnoreCase(ranBs.getName())
										&& nodeBFunctionNameByNeIdMap.get(neId).equalsIgnoreCase(nb.getName())) {
									matchedRanBsId = neId;
									matchedRanBs = ranBsMap.get(matchedRanBsId);
								}
							}
						}
						if (nb == null || matchedRanBs == null) {
							ElementENodeB enb = ranBs.getLteCapability();
							if (enb != null) {
								for (String neId : enodeBFunctionNameByneIdMap.keySet()) {
									if (!neId.equalsIgnoreCase(ranBs.getName())
											&& enodeBFunctionNameByneIdMap.get(neId).equalsIgnoreCase(enb.getName())) {
										matchedRanBsId = neId;
										matchedRanBs = ranBsMap.get(matchedRanBsId);
									}
								}
							}
						}
						if (matchedRanBs != null) {
							if (matchedRanBs.getGsmCapability() == null) {
								matchedRanBs.setGsmCapability(ranBs.getGsmCapability());
								if (ranBs.getGsmCapability() != null)
									ranBsIdByRoleIdMap.put(matchedRanBs.getGsmCapability().getName().toUpperCase(),
											matchedRanBs.getName());
							}
							if (matchedRanBs.getUmtsCapability() == null) {
								matchedRanBs.setUmtsCapability(ranBs.getUmtsCapability());
								if (ranBs.getUmtsCapability() != null)
									ranBsIdByRoleIdMap.put(matchedRanBs.getUmtsCapability().getName().toUpperCase(),
											matchedRanBs.getName());
							}
							if (matchedRanBs.getLteCapability() == null) {
								matchedRanBs.setLteCapability(ranBs.getLteCapability());
								if (ranBs.getLteCapability() != null)
									ranBsIdByRoleIdMap.put(matchedRanBs.getLteCapability().getName().toUpperCase(),
											matchedRanBs.getName());
							}
							ranBsMap.put(matchedRanBs.getName(), matchedRanBs);
							updateCabinets(matchedRanBs.getName(), id);
							updateShelves(matchedRanBs.getName(), id);
							updateSlots(matchedRanBs.getName(), id);
							if (!matchedRanBs.getName().equalsIgnoreCase(id)) {
								ranBsMap.remove(id);
								removeCabinetsFromMap(id);
								removeShelfsFromMap(id);
								removeSlotsFromMap(id);
							}
						}
						// use of the cim file relation file
						else {
							String btsName = bts3900NameByBtsNameRelationMap.get(id);
							if (btsName != null) {
								matchedRanBsId = ranBsIdByRoleIdMap.get(btsName.toUpperCase());
								if (matchedRanBsId != null) {
									matchedRanBs = ranBsMap.get(matchedRanBsId);
									if (matchedRanBs != null) {
										if (matchedRanBs.getGsmCapability() == null) {
											matchedRanBs.setGsmCapability(ranBs.getGsmCapability());
											if (ranBs.getGsmCapability() != null)
												ranBsIdByRoleIdMap.put(
														matchedRanBs.getGsmCapability().getName().toUpperCase(),
														matchedRanBs.getName());
										}
										if (matchedRanBs.getUmtsCapability() == null) {
											matchedRanBs.setUmtsCapability(ranBs.getUmtsCapability());
											if (ranBs.getUmtsCapability() != null)
												ranBsIdByRoleIdMap.put(
														matchedRanBs.getUmtsCapability().getName().toUpperCase(),
														matchedRanBs.getName());
										}
										if (matchedRanBs.getLteCapability() == null) {
											matchedRanBs.setLteCapability(ranBs.getLteCapability());
											if (ranBs.getLteCapability() != null)
												ranBsIdByRoleIdMap.put(
														matchedRanBs.getLteCapability().getName().toUpperCase(),
														matchedRanBs.getName());
										}
										ranBsMap.put(matchedRanBs.getName(), matchedRanBs);
										updateCabinets(matchedRanBs.getName(), id);
										updateShelves(matchedRanBs.getName(), id);
										updateSlots(matchedRanBs.getName(), id);
										ranBsMap.remove(id);
										removeCabinetsFromMap(id);
										removeShelfsFromMap(id);
										removeSlotsFromMap(id);
									}
								}
							}
						}
					}
				} else if (isNodeB) {
					String matchedRanBsId = null;
					ElementRanBs matchedRanBs = null;
					for (String neId : nodeBFunctionNameByNeIdMap.keySet()) {
						if (nodeBFunctionNameByNeIdMap.get(neId).equalsIgnoreCase(id))
							matchedRanBsId = neId;
					}
					if (matchedRanBsId != null) {
						matchedRanBs = ranBsMap.get(matchedRanBsId);
						if (matchedRanBs != null) {
							if (matchedRanBs.getUmtsCapability() == null) {
								matchedRanBs.setUmtsCapability(nodeBMap.get(id));
								if (matchedRanBs.getUmtsCapability() != null)
									ranBsIdByRoleIdMap.put(matchedRanBs.getUmtsCapability().getName().toUpperCase(),
											matchedRanBs.getName());
							}
							ranBsMap.put(matchedRanBs.getName(), matchedRanBs);
							updateCabinets(matchedRanBs.getName(), id);
							updateShelves(matchedRanBs.getName(), id);
							updateSlots(matchedRanBs.getName(), id);
							nodeBMap.remove(id);
							removeCabinetsFromMap(id);
							removeShelfsFromMap(id);
							removeSlotsFromMap(id);
						}
					}
					// use of the cim file relation file
					else {
						String enodeBName = null;
						String bts3900Name = null;
						String btsName = btsNameByNodebNameRelationMap.get(id);
						if (btsName != null) {
							matchedRanBsId = ranBsIdByRoleIdMap.get(btsName.toUpperCase());
							if (matchedRanBsId != null) {
								matchedRanBs = ranBsMap.get(matchedRanBsId);
							}

						}
						if (btsName == null) {
							bts3900Name = bts3900NameByNodeBNameRelationMap.get(id);
							if (bts3900Name != null) {
								matchedRanBsId = ranBsIdByRoleIdMap.get(bts3900Name.toUpperCase());
								if (matchedRanBsId != null) {
									matchedRanBs = ranBsMap.get(matchedRanBsId);
								}
							}
						}

						if (bts3900Name == null) {
							enodeBName = enodeBNameByNodeBNameRelationMap.get(id);
							if (enodeBName != null) {
								matchedRanBsId = ranBsIdByRoleIdMap.get(enodeBName.toUpperCase());
								if (matchedRanBsId != null) {
									matchedRanBs = ranBsMap.get(matchedRanBsId);
								}
							}
						}

						if (matchedRanBs != null) {
							if (matchedRanBs.getUmtsCapability() == null) {
								matchedRanBs.setUmtsCapability(nodeBMap.get(id));
								if (matchedRanBs.getUmtsCapability() != null)
									ranBsIdByRoleIdMap.put(matchedRanBs.getUmtsCapability().getName().toUpperCase(),
											matchedRanBs.getName());
							}
							ranBsMap.put(matchedRanBs.getName(), matchedRanBs);
							updateCabinets(matchedRanBs.getName(), id);
							updateShelves(matchedRanBs.getName(), id);
							updateSlots(matchedRanBs.getName(), id);
							nodeBMap.remove(id);
							removeCabinetsFromMap(id);
							removeShelfsFromMap(id);
							removeSlotsFromMap(id);
						}
					}
				} else if (isGsmBts) {
					// use of the cim file relation file
					String matchedRanBsId = null;
					ElementRanBs matchedRanBs = null;
					String enodeBName = null;
					String bts3900Name = null;
					String nodeBName = null;
					for (String neId : btsNameByNodebNameRelationMap.keySet()) {
						if (btsNameByNodebNameRelationMap.get(neId).equalsIgnoreCase(id)) {
							nodeBName = neId;
							matchedRanBsId = ranBsIdByRoleIdMap.get(nodeBName.toUpperCase());
							if (matchedRanBsId != null && matchedRanBs == null) {
								matchedRanBs = ranBsMap.get(matchedRanBsId);
							}
						}
					}

					if (nodeBName == null) {
						for (String neId : bts3900NameByBtsNameRelationMap.keySet()) {
							if (bts3900NameByBtsNameRelationMap.get(neId).equalsIgnoreCase(id)) {
								bts3900Name = neId;
								matchedRanBsId = ranBsIdByRoleIdMap.get(bts3900Name.toUpperCase());
								if (matchedRanBsId != null && matchedRanBs == null) {
									matchedRanBs = ranBsMap.get(matchedRanBsId);
								}
							}
						}
					}

					if (bts3900Name == null) {
						for (String neId : btsNameByNodebNameRelationMap.keySet()) {
							if (btsNameByNodebNameRelationMap.get(neId).equalsIgnoreCase(id)) {
								enodeBName = neId;
								matchedRanBsId = ranBsIdByRoleIdMap.get(enodeBName.toUpperCase());
								if (matchedRanBsId != null && matchedRanBs == null) {
									matchedRanBs = ranBsMap.get(matchedRanBsId);
								}
							}
						}
					}
					if (matchedRanBs != null) {
						if (matchedRanBs.getGsmCapability() == null) {
							matchedRanBs.setGsmCapability(btsMap.get(id));
							if (matchedRanBs.getGsmCapability() != null)
								ranBsIdByRoleIdMap.put(matchedRanBs.getGsmCapability().getName().toUpperCase(),
										matchedRanBs.getName());
						}
						ranBsMap.put(matchedRanBs.getName(), matchedRanBs);
						updateCabinets(matchedRanBs.getName(), id);
						updateShelves(matchedRanBs.getName(), id);
						updateSlots(matchedRanBs.getName(), id);
						btsMap.remove(id);
						removeCabinetsFromMap(id);
						removeShelfsFromMap(id);
						removeSlotsFromMap(id);
					}
				}
			}
		}

		List<String> bts4Remove = new ArrayList<String>();
		List<String> nodeB4Remove = new ArrayList<String>();
		for (String gsmBtsId : btsMap.keySet()) {
			if (mbtsRelationMatchingParser.findMatchByGmbtsId(gsmBtsId) != null) {
				MbtsRelationData mbtsData = mbtsRelationMatchingParser.findMatchByGmbtsId(gsmBtsId);
				ElementRanBs ranBs = null;
				String neModel = null;
				String neName = mbtsData.getSranBsName();
				ranBs = ranBsMap.get(neName);
				if (ranBs == null) {
					neModel = mbtsData.getGsmType().replace(" GSM", "_MS");
					String site = btsMap.get(gsmBtsId).getSite();
					ranBs = (ElementRanBs) addNode(new ElementRanBs(), neName, neName, neModel, site, "2G/3G/4G", null,
							PossibleNodeType.RanBs);
				} else {
					if (ranBs.getType() == null)
						ranBs.setType(mbtsData.getGsmType().replace(" GSM", "_MS"));
				}
				ElementBs elementBts = btsMap.get(gsmBtsId);
				if (elementBts != null) {
					if (ranBs.getGsmCapability() == null) {
						elementBts.setType(mbtsData.getGsmType());
						ranBs.setGsmCapability(elementBts);
						ranBsIdByRoleIdMap.put(elementBts.getName().toUpperCase(), neName);
					}
				}
				ranBsMap.put(neName, ranBs);
				updateCabinets(neName, gsmBtsId);
				updateShelves(neName, gsmBtsId);
				updateSlots(neName, gsmBtsId);
				bts4Remove.add(gsmBtsId);
				removeCabinetsFromMap(gsmBtsId);
				removeShelfsFromMap(gsmBtsId);
				removeSlotsFromMap(gsmBtsId);
			}
		}

		for (String nodeBId : nodeBMap.keySet()) {
			if (mbtsRelationMatchingParser.findMatchByBts3900Id(nodeBId) != null) {
				MbtsRelationData mbtsData = mbtsRelationMatchingParser.findMatchByBts3900Id(nodeBId);
				ElementRanBs ranBs = null;
				String neModel = null;
				String neName = mbtsData.getSranBsName();
				ranBs = ranBsMap.get(neName);
				if (ranBs == null) {
					neModel = mbtsData.getGsmType().replace(" GSM", "_MS");
					String site = nodeBMap.get(nodeBId).getSite();
					ranBs = (ElementRanBs) addNode(new ElementRanBs(), neName, neName, neModel, site, "2G/3G/4G", null,
							PossibleNodeType.RanBs);
				} else {
					if (ranBs.getType() == null)
						ranBs.setType(mbtsData.getGsmType().replace(" GSM", "_MS"));
				}
				ElementNb elementNb = nodeBMap.get(nodeBId);
				if (elementNb != null) {
					if (ranBs.getUmtsCapability() == null) {
						elementNb.setType(mbtsData.getUmtsType());
						ranBs.setUmtsCapability(elementNb);
						ranBsIdByRoleIdMap.put(elementNb.getName().toUpperCase(), neName);
					}
				}
				ranBsMap.put(neName, ranBs);
				updateCabinets(neName, nodeBId);
				updateShelves(neName, nodeBId);
				updateSlots(neName, nodeBId);
				nodeB4Remove.add(nodeBId);
				removeCabinetsFromMap(nodeBId);
				removeShelfsFromMap(nodeBId);
				removeSlotsFromMap(nodeBId);
			}

		}
		
		for(ElementRanBs ranbs : ranBsMap.values()) {
			if(ranbs.getGsmCapability() != null) {
				if(btsMap.containsKey(ranbs.getGsmCapability().getId())) {
					removeCabinetsFromMap(ranbs.getGsmCapability().getId());
					removeShelfsFromMap(ranbs.getGsmCapability().getId());
					removeSlotsFromMap(ranbs.getGsmCapability().getId());
					btsMap.remove(ranbs.getGsmCapability().getId());
				}
			}
			
			if(ranbs.getUmtsCapability() != null) {
				if(nodeBMap.containsKey(ranbs.getUmtsCapability().getId())) {
					removeCabinetsFromMap(ranbs.getUmtsCapability().getId());
					removeShelfsFromMap(ranbs.getUmtsCapability().getId());
					removeSlotsFromMap(ranbs.getUmtsCapability().getId());
					nodeBMap.remove(ranbs.getUmtsCapability().getId());
				}
			}
			
			if(ranbs.getLteCapability() != null) {
				if(eNodeBMap.containsKey(ranbs.getLteCapability().getId())) {
					removeCabinetsFromMap(ranbs.getLteCapability().getId());
					removeShelfsFromMap(ranbs.getLteCapability().getId());
					removeSlotsFromMap(ranbs.getLteCapability().getId());
					eNodeBMap.remove(ranbs.getLteCapability().getId());
					
				}
			}
		}
		for (String btsId : bts4Remove) {
			btsMap.remove(btsId);
		}

		for (String nodeBId : nodeB4Remove) {
			nodeBMap.remove(nodeBId);
		}
	}

	private void updatePhysicalConfig(String sranBsName, String gsmbtsIs, String nodebId) {
		updateCabinets(sranBsName, gsmbtsIs);
		updateCabinets(sranBsName, nodebId);
		updateShelves(sranBsName, gsmbtsIs);
		updateShelves(sranBsName, nodebId);
		updateSlots(sranBsName, gsmbtsIs);
		updateSlots(sranBsName, nodebId);
	}

	private void updateCabinets(String sranBsName, String gsmbtsId) {
		Set<String> cabinetsId = cabinetsIdByNeIdMap.get(gsmbtsId);
		if (cabinetsId != null) {
			for (String cabinetId : cabinetsId) {
				NodeCabinet cabinet = mapCabinet.get(cabinetId);
				if (cabinet != null) {
					mapCabinet.remove(cabinetId);
					String id = cabinet.getId();
					id = id.replace(gsmbtsId, sranBsName);
					cabinet.setId(id);
					cabinet.setNodeId(sranBsName);
					NodeContainer.getNodeCabinetForName(cabinet.getId(), cabinet);
					mapCabinet.put(id, cabinet);
					setElementsIdByNodeIdMap(cabinetsIdByNeIdMap, id, sranBsName);
				}
			}
		}
		cabinetsIdByNeIdMap.remove(gsmbtsId);
	}

	private void updateShelves(String sranBsName, String gsmbtsId) {

		Set<String> shelfsId = shelfsIdByNeIdMap.get(gsmbtsId);
		if (shelfsId != null) {
			for (String shelfId : shelfsId) {
				NodeShelf shelf = mapShelf.get(shelfId);
				if (shelf != null) {
					mapShelf.remove(shelfId);
					String id = shelf.getId();
					id = id.replace(gsmbtsId, sranBsName);
					shelf.setId(id);
					NodeContainer.getNodeShelfForName(shelf.getId(), shelf);
					mapShelf.put(id, shelf);
					setElementsIdByNodeIdMap(shelfsIdByNeIdMap, id, sranBsName);
				}
			}
		}
		shelfsIdByNeIdMap.remove(gsmbtsId);
	}

	private void updateSlots(String sranBsName, String gsmbtsId) {

		Set<String> slotsId = slotsIdByNeIdMap.get(gsmbtsId);
		if (slotsId != null) {
			for (String slotId : slotsId) {
				NodeSlot slot = nodeSlotMap.get(slotId);
				if (slot != null) {
					nodeSlotMap.remove(slotId);
					String id = slot.getId().replace(gsmbtsId, sranBsName);
					slot.setId(id);
					slot.setNodeId(sranBsName);
					if (slot.getNodeBoards() != null) {
						for (NodeBoard board : slot.getNodeBoards()) {
							board.setSlotId(id);
							board.setId(board.getId().replace(gsmbtsId, sranBsName));
							if (board.getNodeInterfaces() != null) {
								for (NodeInterface inter : board.getNodeInterfaces()) {
									inter.setNodeBoardId(board.getId());
									inter.setId(inter.getId().replace(gsmbtsId, sranBsName));
								}
							}
						}
					}
					nodeSlotMap.put(id, slot);
					setElementsIdByNodeIdMap(slotsIdByNeIdMap, slot.getId(), sranBsName);
				}
			}
		}
		slotsIdByNeIdMap.remove(gsmbtsId);
	}

	/**
	 * 
	 * @param neType
	 * @param regex
	 * @return
	 */
	private boolean isNeTypeMatched(String neType, String regex) {
		if (neType != null && neType.matches(regex)) {
			return true;
		}
		return false;
	}
	
	public ElementType matchElementType(String type, String neType) {

		if (isNeTypeMatched(neType, m2v2r0Patterns.get("bsc6900Gu")) || isNeTypeMatched(neType, "(?i)BSC6910GU") ) {
			return ElementType.BSC;
		} else if (isNeTypeMatched(neType, m2v2r0Patterns.get("bts3900"))) {
			return ElementType.SRanBS;
		} 
		else if (isNeTypeMatched(neType, "(?i)MICROBTS3900")) {
			return ElementType.SRanBS;
		}
		else if (isNeTypeMatched(neType, "(?i)BTS5900") || isNeTypeMatched(neType, "(?i)BTS59005G")) {
			return ElementType.SRanBS;
		}
		else if (type != null) {
			
			if (type.matches(m2v2r0Patterns.get("rnc"))) {
				return ElementType.RNC;
			} else if (type.matches(m2v2r0Patterns.get("nodeB"))) {
				return ElementType.NodeB;
			} else if (type.matches(m2v2r0Patterns.get("bts"))) {
				return ElementType.BTS;
			} else if (type.matches(m2v2r0Patterns.get("bsc"))) {
				return ElementType.BSC;
			} else if (type.matches(m2v2r0Patterns.get("enodeB"))) {
				return ElementType.eNodeB;
			} else if (type.matches(m2v2r0Patterns.get("ranBs"))) {
				return ElementType.SRanBS;
			} else if (type.matches(m2v2r0Patterns.get("ggsn"))) {
				return ElementType.GGSN;
			} else if (type.matches(m2v2r0Patterns.get("sgsn"))) {
				return ElementType.SGSN;
			} else if (type.matches(m2v2r0Patterns.get("mgw"))) {
				return ElementType.MGW;
			} else if (type.matches(m2v2r0Patterns.get("msc"))) {
				return ElementType.MSC;
			} else if (type.matches(m2v2r0Patterns.get("hlr"))) {
				return ElementType.HLR;
			} else if (type.matches(m2v2r0Patterns.get("cgp"))) {
				return ElementType.Cgp;
			} else if (type.matches(m2v2r0Patterns.get("sessionEngine"))) {
				return ElementType.MSC;
			} else if (type.matches(m2v2r0Patterns.get("cg"))) {
				return ElementType.MSC;
			} else if (type.matches(m2v2r0Patterns.get("IPCLK"))) {
				return ElementType.Ipclk;
			} else if (type.matches(m2v2r0Patterns.get("ugw"))) {
				return ElementType.MSC;
			} else if (type.matches(m2v2r0Patterns.get("CoreElement"))) {
				return ElementType.CoreElement;
			} else if (type.matches(m2v2r0Patterns.get("mss"))) {
				return ElementType.MSS;
			} else if (type.matches(m2v2r0Patterns.get("mme")) && !manufactureFixer.isMtcTouchImport()) {
				return ElementType.MME;
			} else if (type.matches(m2v2r0Patterns.get("sgw")) && !manufactureFixer.isMtcTouchImport()) {
				return ElementType.SGW;
			} else if (type.equalsIgnoreCase(NptConstants.HSS.toString()) ) {
				return ElementType.HSS;
			}
		}
		
		return null;
	}

	/******************************
	 ********** getters ***********
	 *******************************/
	public Map<String, ElementRnc> getElementRncs() {
		if (rncMap == null)
			this.parseFiles();
		return rncMap;
	}
	
	public Set<String> getBts5900nodes() {
		if (bts5900nodes == null)
			this.parseFiles();
		return bts5900nodes;
	}

	public Map<String, ElementNb> getElementNodeBs() {
		
		if (nodeBMap == null)
			this.parseFiles();

		return nodeBMap;
	}

	public Map<String, ElementBs> getElementBtss() {
		if (btsMap == null)
			this.parseFiles();
		return btsMap;
	}

	public Map<String, ElementBsc> getElementBscs() {
		if (bscMap == null)
			this.parseFiles();

		return bscMap;
	}

	public Map<String, ElementENodeB> getElementENodeBs() {
		if (eNodeBMap == null)
			this.parseFiles();

		return eNodeBMap;
	}

	public Map<String, ElementRanBs> getElementRanBss() {
		if (ranBsMap == null)
			this.parseFiles();

		return ranBsMap;
	}

	public Map<String, ElementGGSN> getElementGgsns() {
		if (ggsnMap == null)
			this.parseFiles();

		return ggsnMap;
	}

	public Map<String, ElementSgsn> getElementSgsns() {
		if (sgsnMap == null)
			this.parseFiles();

		return sgsnMap;
	}

	public Map<String, NodeSlot> getNodeSlots() {
		if (nodeSlotMap == null)
			this.parseFiles();

		return nodeSlotMap;
	}
	
	public List<ElementAdditionalInfo> getAddInfoList() {
		if (addInfoMap == null)
			this.parseFiles();
		return new ArrayList<ElementAdditionalInfo>(addInfoMap.values());
	}
	
	public Map<String, ElementTrau> getElementTrauMap() {
		if (elementTrauMap == null)
			this.parseFiles();

		return elementTrauMap;
	}

	public Map<String, ElementSranController> getElementSRanControllers() {
		if (sRanControllerMap == null)
			this.parseFiles();
		return sRanControllerMap;
	}

	public Map<String, String> getRanBsIdByRoleIdMap() {
		if (ranBsIdByRoleIdMap == null)
			this.parseFiles();
		return ranBsIdByRoleIdMap;
	}

	public HashMap<String, ElementMgw> getMgwMap() {
		if (mgwMap == null)
			this.parseFiles();
		return mgwMap;
	}

	public HashMap<String, ElementMsc> getMscMap() {
		if (mscMap == null)
			this.parseFiles();
		return mscMap;
	}

	public HashMap<String, ElementHlr> getHlrMap() {
		if (hlrMap == null)
			this.parseFiles();
		return hlrMap;
	}

	public HashMap<String, ElementCgp> getCgpMap() {
		if (cgpMap == null)
			this.parseFiles();
		return cgpMap;
	}

	public HashMap<String, ElementSessionEngine> getSessionEngineMap() {
		if (sessionEngineMap == null)
			this.parseFiles();
		return sessionEngineMap;
	}

	public HashMap<String, ElementCg> getCgMap() {
		if (cgMap == null)
			this.parseFiles();
		return cgMap;
	}

	public HashMap<String, ElementIpclk> getIpclkMap() {
		if (ipclkMap == null)
			this.parseFiles();
		return ipclkMap;
	}

	public HashMap<String, ElementUgw> getUgwMap() {
		if (ugwMap == null)
			this.parseFiles();
		return ugwMap;
	}

	public HashMap<String, ElementCoreElement> getCoreElementMap() {
		if (coreElementMap == null)
			this.parseFiles();
		return coreElementMap;
	}

	public HashMap<String, ElementMss> getMssMap() {
		if (mssMap == null )
			this.parseFiles();
		return mssMap;
	}

	public HashMap<String, ElementMme> getMmeMap() {
		if (mmeMap == null)
			this.parseFiles();
		return mmeMap;
	}

	public HashMap<String, ElementSgw> getSgwMap() {
		if (sgwMap == null)
			this.parseFiles();
		return sgwMap;
	}

	public Map<String, NodeSlot> getNodeSlotsForMatching() {
		if (nodeSlotForMatching == null)
			this.parseFiles();

		return nodeSlotForMatching;
	}

	public Map<String, String> getBtsNameBySiteIndexs() {
		if (btsNameBySiteIndex == null)
			this.parseFiles();
		return btsNameBySiteIndex;
	}

	public Map<String, NodeBoard> getNodeBoardsForMatching() {
		if (nodeBoardsForMatching == null)
			this.parseFiles();
		return nodeBoardsForMatching;
	}

	public Map<String, Map<String, String>> getNodeBNameByCellIdByRnc() {
		if (nodeBNameByCellIdByRnc == null)
			this.parseFiles();
		return nodeBNameByCellIdByRnc;
	}

	public Map<String, NodeSlot> getMap4SpuBoards() {
		if (map4SpuBoards == null)
			this.parseFiles();
		return map4SpuBoards;
	}

	public Map<String, String> getNodeNameMap() {
		if (nodeNameMap == null)
			this.parseFiles();
		return nodeNameMap;
	}

	public Map<String, Set<String>> getCellNameByNeIdMap() {
		if (cellNameByNeIdMap == null)
			this.parseFiles();
		return cellNameByNeIdMap;
	}

	public Map<String, String> getNeIdByNodeBFunctionNameMap() {
		if (neIdByNodeBFunctionNameMap == null)
			this.parseFiles();
		return neIdByNodeBFunctionNameMap;
	}

	public Map<String, NetworkElementDump> getNetworkElementDumpMap() {
		if (networkElementDumps == null)
			this.parseFiles();
		return networkElementDumps;
	}

	public HashMap<String, ElementHss> getHssMap() {
		if (hssMap == null)
			this.parseFiles();
		return hssMap;
	}
	
	public Map<String, NodeBoard> getNodeBoardMap() {
		if (nodeBoardMap == null)
			this.parseFiles();
		return nodeBoardMap;
	}
	
	public Map<String, String> getRanbsRelationMap() {
		if (ranbsRelationMap == null)
			this.parseFiles();
		return ranbsRelationMap;
	}

	
	public void setMultiPhysicalDumpParser(MultiPhysicalDumpParser multiPhysicalDumpParser) {
		this.multiPhysicalDumpParser = multiPhysicalDumpParser;
	}

	public Map<String, String> getSubrackMap() {
		if (subrackMap == null)
			this.parseFiles();
		return subrackMap;
	}
	
	public HashMap<String, ElementRadioAntenna> getAntennaMap() {
		if (antennaMap == null)
			this.parseFiles();
		return antennaMap;
	}

	
}
