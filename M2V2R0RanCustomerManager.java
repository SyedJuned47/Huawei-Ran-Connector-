package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobinets.nps.customer.CustomerTransmissionDefaultDataProvider;
import com.mobinets.nps.customer.transmission.common.TransmissionCommon;
import com.mobinets.nps.customer.transmission.common.exception.NoParamWasFoundForThisDate;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran.parser.logicalparser.ranLogical4G.LteUtil.Ippath;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran.parser.logicalparser.ranLogical4G.LteUtil.Sctplnk;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran.parser.logicalparser.ranLogical4G.LteUtil.Sctppeer;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran.parser.logicalparser.ranLogical4G.LteUtil.UserPlanePeer;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.M2V2R0RanPhysicalParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.common.M2V2R0RanUniqueidParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.license.M2V2R0RanLicenseParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.M2V2R0RanLogicalConnector;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.common.connection.M2V2R0_NodesLogicalInterfacesCollector;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.common.ipam.M2V2R0_IpamCollector;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.parsers.M2V2R0RanLogicalNodeBInterfacesParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.ranLogical4G.M2V2R0RanLogical4GParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.ranLogical5G.M2V2R0RanLogical5GParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.sgsn.M2V2R0RanLogicalSgsnParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.ugw.M2V2R0RanLogicalUgwParser;
import com.mobinets.nps.model.autoimporter.ElementIPData;
import com.mobinets.nps.model.autoimporter.NetworkElementDump;
import com.mobinets.nps.model.customer.data.element.ElementBs;
import com.mobinets.nps.model.customer.data.element.ElementBsc;
import com.mobinets.nps.model.customer.data.element.ElementCarrier3g;
import com.mobinets.nps.model.customer.data.element.ElementCell2g;
import com.mobinets.nps.model.customer.data.element.ElementCell3g;
import com.mobinets.nps.model.customer.data.element.ElementCell4g;
import com.mobinets.nps.model.customer.data.element.ElementCell5g;
import com.mobinets.nps.model.customer.data.element.ElementCellRadioBandWidth;
import com.mobinets.nps.model.customer.data.element.ElementCellTrackingArea;
import com.mobinets.nps.model.customer.data.element.ElementCg;
import com.mobinets.nps.model.customer.data.element.ElementCgp;
import com.mobinets.nps.model.customer.data.element.ElementCoreElement;
import com.mobinets.nps.model.customer.data.element.ElementENodeB;
import com.mobinets.nps.model.customer.data.element.ElementEnodebBandWidth;
import com.mobinets.nps.model.customer.data.element.ElementGGSN;
import com.mobinets.nps.model.customer.data.element.ElementGnodebBandWidth;
import com.mobinets.nps.model.customer.data.element.ElementGsmCellMaio;
import com.mobinets.nps.model.customer.data.element.ElementHlr;
import com.mobinets.nps.model.customer.data.element.ElementHss;
import com.mobinets.nps.model.customer.data.element.ElementIpclk;
import com.mobinets.nps.model.customer.data.element.ElementLac;
import com.mobinets.nps.model.customer.data.element.ElementLicense;
import com.mobinets.nps.model.customer.data.element.ElementMgw;
import com.mobinets.nps.model.customer.data.element.ElementMme;
import com.mobinets.nps.model.customer.data.element.ElementMsc;
import com.mobinets.nps.model.customer.data.element.ElementMss;
import com.mobinets.nps.model.customer.data.element.ElementNRCellRadioBandWidth;
import com.mobinets.nps.model.customer.data.element.ElementNRCellTrackingArea;
import com.mobinets.nps.model.customer.data.element.ElementNb;
import com.mobinets.nps.model.customer.data.element.ElementNeighbor;
import com.mobinets.nps.model.customer.data.element.ElementNodeConnection;
import com.mobinets.nps.model.customer.data.element.ElementPcu;
import com.mobinets.nps.model.customer.data.element.ElementRac;
import com.mobinets.nps.model.customer.data.element.ElementRadioAntenna;
import com.mobinets.nps.model.customer.data.element.ElementRanBs;
import com.mobinets.nps.model.customer.data.element.ElementRnc;
import com.mobinets.nps.model.customer.data.element.ElementSessionEngine;
import com.mobinets.nps.model.customer.data.element.ElementSgsn;
import com.mobinets.nps.model.customer.data.element.ElementSgw;
import com.mobinets.nps.model.customer.data.element.ElementSpusForRnc;
import com.mobinets.nps.model.customer.data.element.ElementSranController;
import com.mobinets.nps.model.customer.data.element.ElementTrau;
import com.mobinets.nps.model.customer.data.element.ElementTrauBoard;
import com.mobinets.nps.model.customer.data.element.ElementTrx;
import com.mobinets.nps.model.customer.data.element.ElementTrxArfcn;
import com.mobinets.nps.model.customer.data.element.ElementUgw;
import com.mobinets.nps.model.customer.data.element.ElementVirtualMgw;
import com.mobinets.nps.model.ipprotocol.IsisInterfaceConfiguration;
import com.mobinets.nps.model.network.ElementAdditionalInfo;
import com.mobinets.nps.model.network.logical.PointCode;
import com.mobinets.nps.model.network.logical.RanInterfaceParticipant;
import com.mobinets.nps.model.network.logical.RanLogicalInterface;
import com.mobinets.nps.model.network.logical.RanLogicalInterfacePort;
import com.mobinets.nps.model.network.logical.RanVirtualInterfacePort;
import com.mobinets.nps.model.nodeinterfaces.LogicalInterface;
import com.mobinets.nps.model.nodeinterfaces.NodeInterface;
import com.mobinets.nps.model.nodeinterfaces.NodeSlot;
import com.mobinets.nps.model.nodeinterfaces.VirtualInterface;
import com.mobinets.nps.model.objectid.ObjectIdElement;
import com.mobinets.nps.summaryReport.SummaryReportManagement;

//juned//
public class M2V2R0RanCustomerManager extends CustomerTransmissionDefaultDataProvider {

	private static final Log log = LogFactory.getLog(M2V2R0RanCustomerManager.class);

	private M2V2R0RanLogicalConnector m2v2r0RanLogicalConnector;
	private M2V2R0RanPhysicalParser m2v2r0RanPhysicalParser;
	private M2V2R0RanLogical4GParser m2v2r0RanLogical4GParser;
	private M2V2R0RanLogical5GParser m2v2R0RanLogical5GParser;
	private M2V2R0RanLogicalSgsnParser m2v2r0RanLogicalSgsnParser;
	private M2V2R0_NodesLogicalInterfacesCollector m2v2r0_NodesLogicalInterfacesCollector;
	private M2V2R0RanLogicalUgwParser m2V2R0RanLogicalUgwParser;
	private M2V2R0_IpamCollector m2v2r0_IpamCollector;
	private Map<String, NodeSlot> nodeSlotMap;
	private M2V2R0RanLogicalNodeBInterfacesParser m2v2r0RanNodeBInterfacesParser;
	private M2V2R0RanLicenseParser m2v2r0ranLicenseParser;
	private M2V2R0RanUniqueidParser m2v2r0ranUniqueidParser;


	public void setM2v2r0ranUniqueidParser(M2V2R0RanUniqueidParser m2v2r0ranUniqueidParser) {
		this.m2v2r0ranUniqueidParser = m2v2r0ranUniqueidParser;
	}

	public void setM2v2r0_IpamCollector(M2V2R0_IpamCollector m2v2r0_IpamCollector) {
		this.m2v2r0_IpamCollector = m2v2r0_IpamCollector;
	}

	public void setM2v2r0_NodesLogicalInterfacesCollector(M2V2R0_NodesLogicalInterfacesCollector m2v2r0_NodesLogicalInterfacesCollector) {
		this.m2v2r0_NodesLogicalInterfacesCollector = m2v2r0_NodesLogicalInterfacesCollector;
	}
	
	public void setM2v2r0ranLicenseParser(M2V2R0RanLicenseParser m2v2r0ranLicenseParser) {
		this.m2v2r0ranLicenseParser = m2v2r0ranLicenseParser;
	}

	public M2V2R0RanLogicalConnector getM2v2r0RanLogicalConnector() {
		return m2v2r0RanLogicalConnector;
	}

	public void setM2v2r0RanLogicalConnector(M2V2R0RanLogicalConnector m2v2r0RanLogicalConnector) {
		this.m2v2r0RanLogicalConnector = m2v2r0RanLogicalConnector;
	}

	public void setM2v2r0RanPhysicalParser(M2V2R0RanPhysicalParser m2v2r0RanPhysicalParser) {
		this.m2v2r0RanPhysicalParser = m2v2r0RanPhysicalParser;
	}

	public void setM2v2r0RanLogical4GParser(M2V2R0RanLogical4GParser m2v2r0RanLogical4GParser) {
		this.m2v2r0RanLogical4GParser = m2v2r0RanLogical4GParser;
	}
	
	public void setM2v2R0RanLogical5GParser(M2V2R0RanLogical5GParser m2v2r0RanLogical5GParser) {
		m2v2R0RanLogical5GParser = m2v2r0RanLogical5GParser;
	}

	public void setM2v2r0RanLogicalSgsnParser(M2V2R0RanLogicalSgsnParser m2v2r0RanLogicalSgsnParser) {
		this.m2v2r0RanLogicalSgsnParser = m2v2r0RanLogicalSgsnParser;
	}
	
	public void setM2v2r0RanNodeBInterfacesParser(M2V2R0RanLogicalNodeBInterfacesParser m2v2r0RanNodeBInterfacesParser) {
		this.m2v2r0RanNodeBInterfacesParser = m2v2r0RanNodeBInterfacesParser;
	}
	
	public void setM2V2R0RanLogicalUgwParser(M2V2R0RanLogicalUgwParser m2v2r0RanLogicalUgwParser) {
		m2V2R0RanLogicalUgwParser = m2v2r0RanLogicalUgwParser;
	}

	@Override
	public List<ElementSgsn> getSgsnList(Date date) {
		log.debug("Parsing List of Sgsns for M2000_V2_R0_Ran");
		return new ArrayList<ElementSgsn>(m2v2r0RanLogicalSgsnParser.getSgsnMap().values());
	}

	@Override
	public List<ElementBsc> getBscList(Date date) {
		log.debug("Parsing List of Bscs for M2000_V2_R0_Ran");
		return new ArrayList<ElementBsc>(m2v2r0RanLogicalConnector.getElementBscs().values());
	}

	@Override
	public List<ElementRnc> getRncList(Date date) {
		log.debug("Parsing List of Rncs for M2000_V2_R0_Ran");
		return new ArrayList<ElementRnc>(m2v2r0RanLogicalConnector.getElementRncs().values());
	}

	@Override
	public List<ElementBs> getBtsList(Date date) throws FileNotFoundException, NoParamWasFoundForThisDate {
		log.debug("Parsing List of Btss for M2000_V2_R0_Ran");
		List<ElementBs> btsList = m2v2r0RanLogicalConnector.getElementBtss();
		return btsList;
	}

	@Override
	public List<ElementNb> getNodebList(Date date) {
		log.debug("Parsing List of NodeBs for M2000_V2_R0_Ran");
		List<ElementNb> nbs =  m2v2r0RanLogicalConnector.getElementNodeBs();
		return nbs;
	}

	@Override
	public List<ElementTrau> getTrauList(Date date) {
		log.debug("Parsing List of TRAU for M2000_V2_R0_Ran");
		return new ArrayList<ElementTrau>(m2v2r0RanPhysicalParser.getElementTrauMap().values());
	}

	@Override
	public List<ElementTrauBoard> getTrauBoardList(Date date) {
		log.debug("Parsing List of TRAU Board for M2000_V2_R0_Ran");
		return new ArrayList<ElementTrauBoard>(m2v2r0RanLogicalConnector.getElementTrauBoardMap().values());
	}

	@Override
	public List<ElementGGSN> getGgsnList() {
		log.debug("Parsing List of Ggsns for M2000_V2_R0_Ran");
		return new ArrayList<ElementGGSN>(m2v2r0RanPhysicalParser.getElementGgsns().values());
	}

	@Override
	public List<NodeSlot> getNodeSlot() {
		log.debug("Parsing List of Node Slot for M2000_V2_R0_Ran");
		nodeSlotMap = new HashMap<>();
		nodeSlotMap.putAll(m2v2r0RanPhysicalParser.getNodeSlots());
		
		TransmissionCommon.addNodeInterfaces(nodeSlotMap, m2v2r0_IpamCollector.getNodeInterfaceList());
		TransmissionCommon.addNodeInterfaces(nodeSlotMap,	new ArrayList<NodeInterface>(m2v2r0RanLogicalConnector.getNodeInterfaceMap().values()));
		Map<String,NodeInterface>list=new HashMap<String,NodeInterface>(m2v2r0RanLogicalConnector.getNodeInterfaceMap());
		Map<String,NodeInterface>list2=new HashMap<String,NodeInterface>(m2v2r0RanLogical4GParser.getNodeInterfacesMap());
		list.keySet().retainAll(list2.keySet());
		for(NodeInterface n: list.values())
			if(n.getSubInterfaces() != null)
				list2.get(n.getId()).getSubInterfaces().addAll(n.getSubInterfaces());
		TransmissionCommon.addNodeInterfaces(nodeSlotMap, new ArrayList<NodeInterface>(list2.values()));
		TransmissionCommon.addNodeInterfaces(nodeSlotMap, new ArrayList<NodeInterface>(m2V2R0RanLogicalUgwParser.getNodeInterfaceMap().values()));
		TransmissionCommon.addNodeInterfaces(nodeSlotMap, new ArrayList<NodeInterface>(m2v2R0RanLogical5GParser.getNodeInterfaceMap().values()));
		
		List<NodeInterface> nodeInetrfaces = m2v2r0_NodesLogicalInterfacesCollector.getNodeInterfaceList();
		TransmissionCommon.addNodeInterfaces(nodeSlotMap, nodeInetrfaces);
//		TransmissionCommon.addNodeInterfaces(nodeSlotMap,	new ArrayList<NodeInterface>(m2v2r0RanNodeBInterfacesParser.getNodeInterfaces().values()));

		return new ArrayList<NodeSlot>(nodeSlotMap.values());
	}
	
//	@Override
//	public List<LogicalInterface> getLoopBackInterface() {
//		log.debug("Parsing List of LogicalInterface for M2000_V2_R0_Ran");
//		List<LogicalInterface> res = new ArrayList<LogicalInterface>();
//		res.addAll(m2v2r0_IpamCollector.getLogicalInterfaceList());
//		res.addAll(m2v2r0RanLogicalConnector.getLogicalInterfaces().values());
//		res.addAll(m2v2r0RanLogical4GParser.getLogicalInterfaces().values());
//		res.addAll(m2v2R0RanLogical5GParser.getLogicalInterfaces().values());
//		res.addAll(m2V2R0RanLogicalUgwParser.getLogicalInterfaceMap().values());
//		return res;
//	}

//	@Override
//	public List<VirtualInterface> getVirtualInterfaces() {
//		List<VirtualInterface> result = new ArrayList<>();
//		result.addAll(m2v2r0RanLogicalConnector.getVirtualInterfaces().values());
//		result.addAll(m2v2R0RanLogical5GParser.getVirtualInterfaceMap().values());
		
//		return result;
//	}

	@Override
	public List<SummaryReportManagement> getSummaryReportList() {
		List<SummaryReportManagement> listSummary = new ArrayList<SummaryReportManagement>();
		listSummary.add(m2v2r0RanPhysicalParser.getSummaryManager());
		return listSummary;
	}

	@Override
	public List<ElementAdditionalInfo> getAddInfoList() {
		List<ElementAdditionalInfo> addInfos = new ArrayList<>();
//		addInfos.addAll(m2v2r0RanLogicalConnector.getAddInfoList());
		addInfos.addAll(m2v2r0RanPhysicalParser.getAddInfoList());
//		addInfos.addAll(m2v2r0_IpamCollector.getAdditionalInfoList());
//		addInfos.addAll(m2v2R0RanLogical5GParser.getElementAdditionalInfoMap().values());
//		addInfos.addAll(m2v2r0RanLogical4GParser.getElementAdditionalInfoMap().values());
		return addInfos;
	
	}

	@Override
	public List<ElementRanBs> getRanBsList() {
		log.debug("Parsing List of RanBss for M2000_V2_R0_Ran");
		return new ArrayList<ElementRanBs>(m2v2r0RanLogicalConnector.getElementRanBss().values());
	}

//	@Override
//	public List<ElementCell2g> getCell2gList(Date date) {
//		log.debug("Parsing List of 2G Cells for M2000_V2_R0_Ran");
//		return new ArrayList<ElementCell2g>(m2v2r0RanLogicalConnector.getElementCells2G().values());
//	}
	
//	@Override
//	public List<ElementRac> getRac(Date date) {
//		List<ElementNodeConnection> emc = m2v2r0RanLogicalConnector.getElementNodeConnections();
//		List<ElementRac> sgsn = m2v2r0RanLogicalSgsnParser.getRac(emc);
//		return sgsn;
//	}

//	@Override
//	public List<ElementRac> getThreeGRac(Date date) {
//		List<ElementNodeConnection> emc = m2v2r0RanLogicalConnector.getElementNodeConnections();
//		List<ElementRac> sgsn = m2v2r0RanLogicalSgsnParser.getThreeGRac(emc);
//		return sgsn;
//	}

//	@Override
//	public List<ElementLac> getLac(Date date) {
//		List<ElementNodeConnection> emc = m2v2r0RanLogicalConnector.getElementNodeConnections();
//		List<ElementLac> sgsn = m2v2r0RanLogicalSgsnParser.getLac(emc);
//		List<ElementLac> mss = m2v2r0RanLogicalConnector.getLac(emc);
//		List<ElementLac> loc = new ArrayList<ElementLac>();
//		loc.addAll(sgsn);
//		loc.addAll(mss);
//		return loc;
//	}

//	@Override
//	public List<ElementLac> getThreeGLac(Date date) {
//		List<ElementNodeConnection> emc = m2v2r0RanLogicalConnector.getElementNodeConnections();
//		List<ElementLac> sgsn = m2v2r0RanLogicalSgsnParser.getThreeGLac(emc);
//		List<ElementLac> mss = m2v2r0RanLogicalConnector.getThreeGLac(emc);
//		List<ElementLac> loc = new ArrayList<ElementLac>();
//		loc.addAll(sgsn);
//		loc.addAll(mss);
//		return loc;
//	}

//	@Override
//	public List<ElementTrx> getTrxList(Date date) {
//		log.debug("Parsing List of TRX for M2000_V2_R0_Ran");
//		return m2v2r0RanLogicalConnector.getElementTrxs();
//	}

//	@Override
//	public List<ElementPcu> getPcuList(Date date) {
//		log.debug("Parsing List of PCUs for M2000_V2_R0_Ran");
//		return new ArrayList<ElementPcu>(m2v2r0RanLogicalConnector.getPcuCardMap().values());
//	}

//	@Override
//	public List<ElementMme> getMmeList(Date date) {
//		log.debug("Parsing List of Mme for M2000_V2_R0_Ran");
//		return new ArrayList<ElementMme>(m2v2r0RanLogicalConnector.getElementMmes().values());
//	}

//	@Override
//	public List<ElementENodeB> getENodeBList(Date date) {
//		log.debug("Parsing List of ENodeBs for M2000_V2_R0_Ran");
//		return m2v2r0RanLogical4GParser.getElementENodeBs();
//	}

//	@Override
//	public boolean checkValidity() {
		// TODO Auto-generated method stub
//		return false;
//	}

//	@Override
//	public List<ElementCoreElement> getCoreElementList() {
//		log.debug("Parsing List of coreElements for M2000_V2_R0_Ran");
//		return new ArrayList<ElementCoreElement>(m2V2R0RanLogicalUgwParser.getCoreElementMap().values());
//	}

//	@Override
//	public List<ElementCell3g> getCell3gList() {
//		log.debug("Parsing List of 3G Cells for M2000_V2_R0_Ran");
//		return new ArrayList<ElementCell3g>(m2v2r0RanLogicalConnector.getElementCells3G().values());
//	}

//	@Override
//	public List<ElementCell4g> getCell4gList() {
//		log.debug("Parsing List of 4G Cells for M2000_V2_R0_Ran");
//		Map<String, ElementCell4g> elementCells4G = m2v2r0RanLogical4GParser.getElementCells4G();
//		Iterator<Map.Entry<String, ElementCell4g>> it = elementCells4G.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry<String, ElementCell4g> entry = it.next();
//			if (entry.getKey().contains(TransmissionCommon.SEPERATOR)) {
//				it.remove();
//			}
//		}
//		return new ArrayList<ElementCell4g>(elementCells4G.values());
//	}

//	@Override
//	public List<ElementMgw> getMgwList() {
//		log.debug("Parsing List of Mgw for M2000_V2_R0_Ran");
//		return new ArrayList<ElementMgw>(m2v2r0RanLogicalConnector.getElementMgws().values());
//	}

//	@Override
//	public List<ElementMss> getMssList() {
//		log.debug("Parsing List of Mss for M2000_V2_R0_Ran");
//		return new ArrayList<ElementMss>(m2v2r0RanLogicalConnector.getElementMsss().values());
//	}

//	@Override
//	public List<ElementNeighbor> getCellNeighborList() {
//		log.debug("Parsing List of Cell Neighbors for M2000_V2_R0_Ran");
//		List<ElementNeighbor> res = m2v2r0RanLogicalConnector.getElementNeighbors();
//		res.addAll(m2v2r0RanLogical4GParser.getElementNeighbors());
//		return res;
//	}

//	@Override
//	public List<ElementTrxArfcn> getTrxArfcnList() {
//		return m2v2r0RanLogicalConnector.getElementTrxArfcns();
//	}

//	@Override
//	public List<ElementCellTrackingArea> getCellTrackingAreaList() {
//		log.debug("Parsing List of CellTrackingArea for M2000_V2_R0_Ran");
//		return m2v2r0RanLogical4GParser.getElementCellTrackingAreas();
//	}

//	@Override
//	public List<ElementEnodebBandWidth> getENodeBBandWidthList() {
//		log.debug("Parsing List of ENodeB BandWidth for M2000_V2_R0_Ran");
//		return m2v2r0RanLogical4GParser.getElementEnodebBandWidths();
//	}

//	@Override
//	public List<ElementCellRadioBandWidth> getCellRadioBandwidthList() {
//		log.debug("Parsing List of Cell BandWidth for M2000_V2_R0_Ran");
//		return m2v2r0RanLogical4GParser.getElementCellRadioBandWidths();
//	}

//	@Override
//	public List<ElementNodeConnection> getNetworkElementConnectionList() {
//		log.debug("Parsing List of NetworkElement Connection for M2000_V2_R0_Ran");
//		return m2v2r0RanLogicalConnector.getElementNodeConnections();
//	}

//	@Override
//	public List<ElementCarrier3g> getThreeGCarrierList() {
//		log.debug("Parsing List of ThreeG Carrier for M2000_V2_R0_Ran");
//		return m2v2r0RanLogicalConnector.getElementCarriers3g();
//	}

//	@Override
//	public List<ElementIPData> getElementIPData() {

//		List<ElementIPData> res = new ArrayList<ElementIPData>();
//		res.addAll(m2v2r0RanLogical4GParser.getElementIPData());
//		res.addAll(m2v2r0RanLogicalConnector.getElementIPData());

	//	return res;
	//}

//	@Override
//	public List<ElementSpusForRnc> getSpusForRncList() {
//		log.debug("Parsing List of SpusForRnc for M2000_V2_R0_Ran");
//		return m2v2r0RanLogicalConnector.getElementSpusForRnc();
//	}

//	@Override
//	public List<ElementGsmCellMaio> getGsmCellMaioList() {
//		log.debug("Parsing List of ElementGsmCellMaio for M2000_V2_R0_Ran");
//		return m2v2r0RanLogicalConnector.getElementGsmCellMaios();
//	}

//	@Override
//	public List<RanLogicalInterface> getRanLogicalInterfaceList() {
//		log.debug("Parsing List of RanLogicalInterface for M2000_V2_R0_Ran");
//		List<RanLogicalInterface> logicalList = new ArrayList<RanLogicalInterface>();
//		logicalList.addAll(m2v2r0_NodesLogicalInterfacesCollector.getRanLogicalInterfaceList());
//		logicalList.addAll(m2v2r0RanLogicalConnector.getRanLogicalInterfaces());
//		logicalList.addAll(m2v2r0RanLogical4GParser.getRanLogicalInterfaces());

//		return logicalList;
//	}

//	@Override
//	public List<RanInterfaceParticipant> getRanInterfaceParticipantList() {
//		log.debug("Parsing List of RanInterfaceParticipant for M2000_V2_R0_Ran");
//		List<RanInterfaceParticipant> logicalList = new ArrayList<RanInterfaceParticipant>();
//		logicalList.addAll(m2v2r0_NodesLogicalInterfacesCollector.getRanInterfaceParticipantList());
//		logicalList.addAll(m2v2r0RanLogicalConnector.getRanInterfaceParticipants());
//		logicalList.addAll(m2v2r0RanLogical4GParser.getRanInterfaceParticipants());
//		return logicalList;
//	}

//	@Override
//	public List<RanLogicalInterfacePort> getRanLogicalInterfacePortList() {
//		log.debug("Parsing List of RanLogicalInterfacePort for M2000_V2_R0_Ran");
//		List<RanLogicalInterfacePort> logicalList = new ArrayList<RanLogicalInterfacePort>();
//		logicalList.addAll(m2v2r0_NodesLogicalInterfacesCollector.getRanLogicalInterfacePortList());
//		logicalList.addAll(m2v2r0RanLogicalConnector.getRanLogicalInterfacePorts());
//		logicalList.addAll(m2v2r0RanLogical4GParser.getRanLogicalInterfacePorts());
//		return logicalList;
//	}

	@Override
	public List<ElementSranController> getSRanControllerList() {
		log.debug("Parsing List of SRanController for M2000_V2_R0_Ran");
		return new ArrayList<ElementSranController>(m2v2r0RanPhysicalParser.getElementSRanControllers().values());
	}

	@Override
	public List<ElementHlr> getHlrList() {
		log.debug("Parsing List of Hlr for M2000_V2_R0_Ran");
		return new ArrayList<ElementHlr>(m2v2r0RanPhysicalParser.getHlrMap().values());
	}

	@Override
	public List<ElementMsc> getMscList() {
		log.debug("Parsing List of Msc for M2000_V2_R0_Ran");
		return new ArrayList<ElementMsc>(m2v2r0RanPhysicalParser.getMscMap().values());
	}

	@Override
	public List<ElementUgw> getUgwList() {
		log.debug("Parsing List of Ugw for M2000_V2_R0_Ran");
		return new ArrayList<ElementUgw>(m2v2r0RanPhysicalParser.getUgwMap().values());
	}

	@Override
	public List<ElementCg> getCgList() {
		log.debug("Parsing List of Cg for M2000_V2_R0_Ran");
		return new ArrayList<ElementCg>(m2v2r0RanPhysicalParser.getCgMap().values());
	}

	@Override
	public List<ElementCgp> getCgpList() {
		log.debug("Parsing List of CgpOmu for M2000_V2_R0_Ran");
		return new ArrayList<ElementCgp>(m2v2r0RanPhysicalParser.getCgpMap().values());
	}

	@Override
	public List<ElementIpclk> getIpclkList() {
		log.debug("Parsing List of Ics for M2000_V2_R0_Ran");
		return new ArrayList<ElementIpclk>(m2v2r0RanPhysicalParser.getIpclkMap().values());
	}

	@Override
	public List<ElementSessionEngine> getSessionEngineList() {
		log.debug("Parsing List of Session Engine for M2000_V2_R0_Ran");
		return new ArrayList<ElementSessionEngine>(m2v2r0RanPhysicalParser.getSessionEngineMap().values());
	}

//	@Override
//	public List<ElementSgw> getSgwList() {
//		log.debug("Parsing List of Sgw for M2000_V2_R0_Ran");
//		return new ArrayList<ElementSgw>(m2v2r0RanLogicalConnector.getElementSgws().values());
//	}

//	@Override
//	public List<RanVirtualInterfacePort> getRanVirtualInterfacePortList() {
//		log.debug("Parsing List of RanVirtualInterfacePort for M2000_V2_R0_Ran");
//		List<RanVirtualInterfacePort> logicalList = new ArrayList<RanVirtualInterfacePort>();
//		logicalList.addAll(m2v2r0_NodesLogicalInterfacesCollector.getRanVirtualInterfacePortList());
//		logicalList.addAll(m2v2r0RanLogical4GParser.getRanVirtualInterfacePortMap().values());
//		return logicalList;
//	}

	@Override
	public List<IsisInterfaceConfiguration> getIsisInterfaceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NetworkElementDump> getNetworkElementDump() {
		return new ArrayList<NetworkElementDump>(m2v2r0RanPhysicalParser.getNetworkElementDumpMap().values());
	}


//	@Override
	//public List<PointCode> getPointCodeList() {
	//	log.debug("Parsing List of Hss for M2000_V2_R0_Ran");
	//	List<PointCode> pointInetrfaces = m2v2r0_NodesLogicalInterfacesCollector.getPointCodeList();
	//	List<PointCode> pointConnector = m2v2r0RanLogicalConnector.getPointCodes();
	//	List<PointCode> pc = new ArrayList<PointCode>();
	//	pc.addAll(pointInetrfaces);
	//	pc.addAll(pointConnector);
	//	return pc;
//	}

	@Override
	public List<ElementHss> getHssList() {
		log.debug("Parsing List of Hss for M2000_V2_R0_Ran");
		return new ArrayList<ElementHss>(m2v2r0RanPhysicalParser.getHssMap().values());
	}

//	@Override
//	public List<ElementVirtualMgw> getVirtualMgws() {
//		return m2v2r0RanLogicalConnector.getVirtualMgws();
//	}

//	@Override
//	public List<Sctplnk> getSctplnks() {
	//	return m2v2r0RanLogical4GParser.getSctplnks();
	//}

//	@Override
//	public List<Sctppeer> getSctppeers() {
//		return m2v2r0RanLogical4GParser.getSctppeers();
//}

//	@Override
//	public List<UserPlanePeer> getUserPlanePeers() {
//		return m2v2r0RanLogical4GParser.getUserPlanePeers();
//	}

//	@Override
//	public List<Ippath> getIpPaths() {
//		return m2v2r0RanLogical4GParser.getIpPaths();
//	}

	@Override
	public List<ElementLicense> getElementLicenseList() {
		List<ElementLicense> result = new ArrayList<>();
		
		if(m2v2r0ranLicenseParser != null)
			result.addAll(m2v2r0ranLicenseParser.getLicenses());
		return result;
	}
	@Override
	public List<ObjectIdElement> getObjectIdElements() {
		List<ObjectIdElement> result = new ArrayList<>();
		
		if(m2v2r0ranUniqueidParser != null)
			result.addAll(m2v2r0ranUniqueidParser.getObjectIdElement());
		return result;
	}
	
//	@Override
//	public List<ElementCell5g> getCell5gList() {
//		List<ElementCell5g> result = new ArrayList<>();
		
//		if(m2v2R0RanLogical5GParser != null)
//			result.addAll(m2v2R0RanLogical5GParser.getCellMap().values());
//		return result;
//	}
	
//	@Override
//	public List<ElementNRCellTrackingArea> getElementNRCellTrackingAreaList() {
//		List<ElementNRCellTrackingArea> result = new ArrayList<>();
		
//		if(m2v2R0RanLogical5GParser != null)
//			result.addAll(m2v2R0RanLogical5GParser.getElementNRCellTrackingAreas());
//		return result;
//	}
	
//	@Override
//	public List<ElementNRCellRadioBandWidth> getNRCellRadioBandwidthList() {
//		List<ElementNRCellRadioBandWidth> result = new ArrayList<>();
		
//		if(m2v2R0RanLogical5GParser != null)
//			result.addAll(m2v2R0RanLogical5GParser.getElementNrCellRadioBandWidths());
//		return result;
//	}
	
	//@Override
//	public List<ElementGnodebBandWidth> getGNodeBBandWidthList() {
//		List<ElementGnodebBandWidth> result = new ArrayList<>();
		
	//	if(m2v2R0RanLogical5GParser != null)
	//		result.addAll(m2v2R0RanLogical5GParser.getElementGnodebBandWidths());
//		return result;
//	}
	
	@Override
	public List<ElementRadioAntenna> getElementRadioAntennaList() {
		List<ElementRadioAntenna> result = new ArrayList<>();
		
		if(m2v2r0RanPhysicalParser != null)
			result.addAll(new ArrayList<ElementRadioAntenna>(m2v2r0RanPhysicalParser.getAntennaMap().values()));
		return result;	
	}
}
