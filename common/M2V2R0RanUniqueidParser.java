package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobinets.nep.npt.model.gis.UserTypes.PossibleNodeType;
import com.mobinets.nps.customer.transmission.common.CommonConfig;
import com.mobinets.nps.customer.transmission.common.TransmissionCommon;
import com.mobinets.nps.customer.transmission.manufacture.common.ConnectorUtility;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.M2V2R0RanPhysicalParser;
import com.mobinets.nps.model.customer.data.element.ElementBs;
import com.mobinets.nps.model.customer.data.element.ElementBsc;
import com.mobinets.nps.model.customer.data.element.ElementENodeB;
import com.mobinets.nps.model.customer.data.element.ElementNb;
import com.mobinets.nps.model.customer.data.element.ElementNode;
import com.mobinets.nps.model.customer.data.element.ElementRanBs;
import com.mobinets.nps.model.customer.data.element.ElementRnc;
import com.mobinets.nps.model.objectid.ObjectIdElement;

public class M2V2R0RanUniqueidParser {

	private static final Log log = LogFactory.getLog(M2V2R0RanUniqueidParser.class);
	private List<ObjectIdElement> objectidelements;
	private Map<String,String>objectidMap;
	private List<String> uniqueIdReports;
	private M2V2R0RanPhysicalParser m2v2r0RanPhysicalParser;
	private CommonConfig m2v2r0Config;
	// private Map<String, String>uniqueMap;
	private Map<String, ElementBsc> bscsByName;
	private Map<String, ElementRnc> rncsByName;
	private Map<String, ElementRanBs> ranBssByName;
	private Map<String, String> ranBssRoleMap;
	private Map<String, ElementBs> btssByName;
	private Map<String, ElementNb> nodeBsByName;
	private Map<String, ElementENodeB> enodeBsByName;

	public void setM2v2r0Config(CommonConfig m2v2r0Config) {
		this.m2v2r0Config = m2v2r0Config;
	}

	public void setM2v2r0RanPhysicalParser(M2V2R0RanPhysicalParser m2v2r0RanPhysicalParser) {
		this.m2v2r0RanPhysicalParser = m2v2r0RanPhysicalParser;
	}

	public void parseFile() {
		init();
		if (uniqueIdReports == null)
			uniqueIdReports = new ArrayList<>();
		HashMap<String, String> UniqueMap = new HashMap<>();

		// physicalNodeMap = new HashMap<>();
		//
		List<File> files = new ArrayList<>();

		if (m2v2r0Config == null) {
			log.error("please set the config context variable");
			return;
		}

		if (!m2v2r0Config.containsKey("huawei.m2v2r0.ran.uniqueiddump.folder")) {
			log.error("No property 'huawei.m2v2r0.ran.uniqueiddump.folder' found in the context file");
		}
		File folder = new File(m2v2r0Config.getProperty("huawei.m2v2r0.ran.uniqueiddump.folder"));
		if (!folder.exists()) {
			log.error("Folder : <" + folder.getAbsolutePath() + "> does not exist");
			return;
		}
		if (folder.isDirectory()) {
			ConnectorUtility.listf(folder.getAbsolutePath(), files);
		}

		// for (String node : bscMap.keySet()) {
		// ElementBsc bsc = bscMap.get(node);
		// if (bsc != null && bsc.getId() != null)
		// physicalNodeMap.put(bsc.getName(), bsc.getId());
		// }
		//
		// for (String node : rncMap.keySet()) {
		// ElementRnc rnc = rncMap.get(node);
		// if (rnc != null && rnc.getId() != null)
		// physicalNodeMap.put(rnc.getId(), rnc.getName());
		// }

		for (File file : files) {

			log.debug("Starting parsing file : " + file.getAbsolutePath());
			parseUniqueIdFile(file);

			log.debug("End parsing file : " + file.getAbsolutePath());
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		// uniqueMap = new HashMap<>();
		objectidelements = new ArrayList<>();
		bscsByName = new HashMap<>();
		rncsByName = new HashMap<>();
		ranBssByName = new HashMap<>();
		btssByName = new HashMap<>();
		nodeBsByName = new HashMap<>();
		enodeBsByName = new HashMap<>();
		ranBssByName = new HashMap<>();
		for (ElementBsc bsc : m2v2r0RanPhysicalParser.getElementBscs().values())
			bscsByName.put(bsc.getName().toUpperCase(), bsc);

		for (ElementRnc rnc : m2v2r0RanPhysicalParser.getElementRncs().values())
			rncsByName.put(rnc.getName().toUpperCase(), rnc);

		for (ElementBs bts : m2v2r0RanPhysicalParser.getElementBtss().values())
			btssByName.put(bts.getName().toUpperCase(), bts);

		for (ElementENodeB enodeB : m2v2r0RanPhysicalParser.getElementENodeBs().values())
			enodeBsByName.put(enodeB.getName().toUpperCase(), enodeB);

		for (ElementNb nodeB : m2v2r0RanPhysicalParser.getElementNodeBs().values())
			nodeBsByName.put(nodeB.getName().toUpperCase(), nodeB);
		for (ElementRanBs ranBs : m2v2r0RanPhysicalParser.getElementRanBss().values())
			ranBssByName.put(ranBs.getName().toUpperCase(), ranBs);

		ranBssRoleMap = m2v2r0RanPhysicalParser.getRanBsIdByRoleIdMap();
	}

	private void parseUniqueIdFile(File file) {
		// TODO Auto-generated method stub
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

			// String line;
			boolean headerfound = false;
			String nodeName = null;
			String uniqueId = null;
			String elementType = null;
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				try {
					if (StringUtils.isBlank(line))
						continue;
					line = line.trim();

					if (StringUtils.containsIgnoreCase(line, "---    END")) {
						headerfound = false;
					}

					if (headerfound) {

						try {
							if (line.startsWith("NE"))
								nodeName = TransmissionCommon.getStringByGroup(line, "^NE\\s*:\\s*(.*)", 1);

							if (line.startsWith("ESN"))

								uniqueId = TransmissionCommon.getStringByGroup(line, "^ESN\\s*=\\s*(.*)", 1);

							if (nodeName != null && uniqueId != null) {
								// ElementNode elementNode =
								// deduceNodeTypeFromNodeName(nodeName);
								if (bscsByName.containsKey(nodeName.toUpperCase()))
									elementType = "BSC";

								else if (rncsByName.containsKey(nodeName.toUpperCase()))
									elementType = "RNC";

								else if (btssByName.containsKey(nodeName.toUpperCase()))
									elementType = "BTS";

								else if (enodeBsByName.containsKey(nodeName.toUpperCase()))
									elementType = "eNodeB";

								else if (nodeBsByName.containsKey(nodeName.toUpperCase()))
									elementType = "NodeB";

								else if (ranBssByName.containsKey(nodeName.toUpperCase()))
									elementType = "RanBs";
								else if (ranBssRoleMap.containsKey(nodeName.toUpperCase()))
									elementType = "RanBs";

								ObjectIdElement objectIdElementReport = new ObjectIdElement();
								objectIdElementReport.setElementType(elementType);
								objectIdElementReport.setNewObjectId(uniqueId);
								objectIdElementReport.setOldObjectId(nodeName);
								objectidelements.add(objectIdElementReport);
								objectidMap.put(nodeName, uniqueId);

								uniqueId = null;
								nodeName = null;
								elementType = null;
							}

						} catch (Exception e) {
							// TODO: handle exception
						}

					}

					if (StringUtils.containsIgnoreCase(line, "MML Command-----LST ESN:;")) {
						headerfound = true;

					}

				} catch (Exception e) {
					log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
				}
			}
		} catch (Exception e) {
			log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
		}

	}

	// public static class UniqueData {
	// public String neName;
	// public String un
	// }
	public List<ObjectIdElement> getObjectIdElement() {

		if (objectidelements == null)
			parseFile();
		return objectidelements;
	}
	public Map<String,String> getNewObjectId() {

		if (objectidMap == null)
			parseUniqueIdFile();
		return objectidMap;
	}

	private void parseUniqueIdFile() {
		if (objectidMap == null)
			objectidMap = new HashMap<>();
		
		List<File> files = new ArrayList<>();

		if (m2v2r0Config == null) {
			log.error("please set the config context variable");
			return;
		}

		if (!m2v2r0Config.containsKey("huawei.m2v2r0.ran.uniqueiddump.folder")) {
			log.error("No property 'huawei.m2v2r0.ran.uniqueiddump.folder' found in the context file");
		}
		File folder = new File(m2v2r0Config.getProperty("huawei.m2v2r0.ran.uniqueiddump.folder"));
		if (!folder.exists()) {
			log.error("Folder : <" + folder.getAbsolutePath() + "> does not exist");
			return;
		}
		if (folder.isDirectory()) {
			ConnectorUtility.listf(folder.getAbsolutePath(), files);
		}
		for (File file : files) {

			log.debug("Starting parsing file : " + file.getAbsolutePath());
			parseUniqueIdFileforNodes(file);

			log.debug("End parsing file : " + file.getAbsolutePath());
		}
	
		
		
	}

	private void parseUniqueIdFileforNodes(File file) {

		// TODO Auto-generated method stub
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

			// String line;
			boolean headerfound = false;
			String nodeName = null;
			String uniqueId = null;
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				try {
					if (StringUtils.isBlank(line))
						continue;
					line = line.trim();

					if (StringUtils.containsIgnoreCase(line, "---    END")) {
						headerfound = false;
					}

					if (headerfound) {

						try {
							if (line.startsWith("NE"))
								nodeName = TransmissionCommon.getStringByGroup(line, "^NE\\s*:\\s*(.*)", 1);

							if (line.startsWith("ESN"))

								uniqueId = TransmissionCommon.getStringByGroup(line, "^ESN\\s*=\\s*(.*)", 1);

							if (nodeName != null && uniqueId != null) {
							
								objectidMap.put(nodeName, uniqueId);

								uniqueId = null;
								nodeName = null;
							}

						} catch (Exception e) {
							// TODO: handle exception
						}

					}

					if (StringUtils.containsIgnoreCase(line, "MML Command-----LST ESN:;")) {
						headerfound = true;

					}

				} catch (Exception e) {
					log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
				}
			}
		} catch (Exception e) {
			log.error("Error while parsing the file : " + file.getAbsolutePath(), e);
		}

	
		
	}

}
