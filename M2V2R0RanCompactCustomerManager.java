package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobinets.nps.customer.CustomerTransmissionDefaultDataProvider;
import com.mobinets.nps.customer.transmission.common.exception.NoParamWasFoundForThisDate;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.M2V2R0RanPhysicalParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.M2V2R0RanLogicalConnector;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.common.connection.M2V2R0_NodesLogicalInterfacesCollector;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.common.ipam.M2V2R0_IpamCollector;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.ranLogical4G.M2V2R0RanLogical4GParser;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.sgsn.M2V2R0RanLogicalSgsnParser;
import com.mobinets.nps.model.autoimporter.ElementIPData;
import com.mobinets.nps.model.autoimporter.NetworkElementDump;
import com.mobinets.nps.model.customer.data.element.ElementBs;
import com.mobinets.nps.model.customer.data.element.ElementBsc;
import com.mobinets.nps.model.customer.data.element.ElementCarrier3g;
import com.mobinets.nps.model.customer.data.element.ElementCell2g;
import com.mobinets.nps.model.customer.data.element.ElementCell3g;
import com.mobinets.nps.model.customer.data.element.ElementCell4g;
import com.mobinets.nps.model.customer.data.element.ElementCellRadioBandWidth;
import com.mobinets.nps.model.customer.data.element.ElementCellTrackingArea;
import com.mobinets.nps.model.customer.data.element.ElementCg;
import com.mobinets.nps.model.customer.data.element.ElementCgp;
import com.mobinets.nps.model.customer.data.element.ElementCoreElement;
import com.mobinets.nps.model.customer.data.element.ElementENodeB;
import com.mobinets.nps.model.customer.data.element.ElementEnodebBandWidth;
import com.mobinets.nps.model.customer.data.element.ElementGGSN;
import com.mobinets.nps.model.customer.data.element.ElementGsmCellMaio;
import com.mobinets.nps.model.customer.data.element.ElementHlr;
import com.mobinets.nps.model.customer.data.element.ElementIpclk;
import com.mobinets.nps.model.customer.data.element.ElementMgw;
import com.mobinets.nps.model.customer.data.element.ElementMsc;
import com.mobinets.nps.model.customer.data.element.ElementMss;
import com.mobinets.nps.model.customer.data.element.ElementNb;
import com.mobinets.nps.model.customer.data.element.ElementNeighbor;
import com.mobinets.nps.model.customer.data.element.ElementNodeConnection;
import com.mobinets.nps.model.customer.data.element.ElementPcu;
import com.mobinets.nps.model.customer.data.element.ElementRanBs;
import com.mobinets.nps.model.customer.data.element.ElementRnc;
import com.mobinets.nps.model.customer.data.element.ElementSessionEngine;
import com.mobinets.nps.model.customer.data.element.ElementSgsn;
import com.mobinets.nps.model.customer.data.element.ElementSpusForRnc;
import com.mobinets.nps.model.customer.data.element.ElementSranController;
import com.mobinets.nps.model.customer.data.element.ElementTrau;
import com.mobinets.nps.model.customer.data.element.ElementTrauBoard;
import com.mobinets.nps.model.customer.data.element.ElementTrx;
import com.mobinets.nps.model.customer.data.element.ElementTrxArfcn;
import com.mobinets.nps.model.customer.data.element.ElementUgw;
import com.mobinets.nps.model.network.ElementAdditionalInfo;
import com.mobinets.nps.model.network.logical.RanInterfaceParticipant;
import com.mobinets.nps.model.network.logical.RanLogicalInterface;
import com.mobinets.nps.model.network.logical.RanLogicalInterfacePort;
import com.mobinets.nps.model.network.logical.RanVirtualInterfacePort;
import com.mobinets.nps.model.nodeinterfaces.LogicalInterface;
import com.mobinets.nps.model.nodeinterfaces.NodeBoard;
import com.mobinets.nps.model.nodeinterfaces.NodeInterface;
import com.mobinets.nps.model.nodeinterfaces.NodeSlot;
import com.mobinets.nps.model.nodeinterfaces.VirtualInterface;
import com.mobinets.nps.summaryReport.SummaryReportManagement;

public class M2V2R0RanCompactCustomerManager extends CustomerTransmissionDefaultDataProvider {

	private static final Log log = LogFactory.getLog(M2V2R0RanCustomerManager.class);

	private M2V2R0RanLogicalConnector m2v2r0RanLogicalConnector;
	private M2V2R0RanPhysicalParser m2v2r0RanPhysicalParser;
	private M2V2R0RanLogical4GParser m2v2r0RanLogical4GParser;
	private M2V2R0RanLogicalSgsnParser m2v2r0RanLogicalSgsnParser;
	private M2V2R0_NodesLogicalInterfacesCollector m2v2r0_NodesLogicalInterfacesCollector;
	private M2V2R0_IpamCollector m2v2r0_IpamCollector;

	public void setM2v2r0_IpamCollector(M2V2R0_IpamCollector m2v2r0_IpamCollector) {
		this.m2v2r0_IpamCollector = m2v2r0_IpamCollector;
	}

	public void setM2v2r0_NodesLogicalInterfacesCollector(
			M2V2R0_NodesLogicalInterfacesCollector m2v2r0_NodesLogicalInterfacesCollector) {
		this.m2v2r0_NodesLogicalInterfacesCollector = m2v2r0_NodesLogicalInterfacesCollector;
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

	public void setM2v2r0RanLogicalSgsnParser(M2V2R0RanLogicalSgsnParser m2v2r0RanLogicalSgsnParser) {
		this.m2v2r0RanLogicalSgsnParser = m2v2r0RanLogicalSgsnParser;
	}

	@Override
	public List<ElementSgsn> getSgsnList(Date date) {
		log.debug("Parsing List of Sgsns for M2000_V2_R0_Ran");
		return new ArrayList<ElementSgsn>(m2v2r0RanLogicalSgsnParser.getSgsnMap().values());
	}

	/*@Override
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
		return m2v2r0RanLogicalConnector.getElementBtss();
	}

	@Override
	public List<ElementNb> getNodebList(Date date) {
		log.debug("Parsing List of NodeBs for M2000_V2_R0_Ran");
		return m2v2r0RanLogicalConnector.getElementNodeBs();
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

		Map<String, NodeSlot> nodeSlotMap = m2v2r0RanPhysicalParser.getNodeSlots();

		addNodeInterfaces(nodeSlotMap, m2v2r0_IpamCollector.getNodeInterfaceList());
		addNodeInterfaces(nodeSlotMap,
				new ArrayList<NodeInterface>(m2v2r0RanLogicalConnector.getNodeInterfaceMap().values()));
		addNodeInterfaces(nodeSlotMap,
				new ArrayList<NodeInterface>(m2v2r0RanLogical4GParser.getNodeInterfacesMap().values()));

		List<NodeInterface> nodeInetrfaces = m2v2r0_NodesLogicalInterfacesCollector.getNodeInterfaceList();
		addNodeInterfaces(nodeSlotMap, nodeInetrfaces);

		return new ArrayList<NodeSlot>(nodeSlotMap.values());
	}

	*//**
	 * 
	 * @param nodeSlotMap
	 * @param nodeInterfaces
	 *//*
	private void addNodeInterfaces(Map<String, NodeSlot> nodeSlotMap, List<NodeInterface> nodeInterfaces) {

		for (NodeInterface nodeInterface : nodeInterfaces) {
			String boardId = nodeInterface.getNodeBoardId();

			if (boardId == null)
				continue;

			Integer lastIndexOf_ = boardId.lastIndexOf("_");

			String slotId = boardId.substring(0, lastIndexOf_);
			NodeSlot nodeSlot = nodeSlotMap.get(slotId);

			if (nodeSlot == null)
				continue;

			for (NodeBoard nodeBoard : nodeSlot.getNodeBoards()) {
				if (nodeBoard.getId().equalsIgnoreCase(boardId)) {
					Collection<NodeInterface> listNodeInterface = nodeBoard.getNodeInterfaces();

					if (listNodeInterface.contains(nodeInterface)) {
						for (NodeInterface physNodeInterface : listNodeInterface) {
							if (physNodeInterface.getId().equalsIgnoreCase(nodeInterface.getId())) {
								physNodeInterface.setSubInterfaces(nodeInterface.getSubInterfaces());
								physNodeInterface.setIpData(nodeInterface.getIpData());
								physNodeInterface.setInterfaceName(nodeInterface.getInterfaceName());
								physNodeInterface.setVlanId(nodeInterface.getVlanId());
								break;
							}
						}
					} else {
						listNodeInterface.add(nodeInterface);
						nodeBoard.setNodeInterfaces(listNodeInterface);
					}
				}
			}
		}
	}

	@Override
	public List<LogicalInterface> getLoopBackInterface() {
		log.debug("Parsing List of LogicalInterface for M2000_V2_R0_Ran");
		List<LogicalInterface> res = new ArrayList<LogicalInterface>();
		res.addAll(m2v2r0_IpamCollector.getLogicalInterfaceList());
		res.addAll(m2v2r0RanLogicalConnector.getLogicalInterfaces().values());
		res.addAll(m2v2r0RanLogical4GParser.getLogicalInterfaces().values());
		return res;
	}

	@Override
	public List<VirtualInterface> getVirtualInterfaces() {
		return new ArrayList<VirtualInterface>(m2v2r0RanLogicalConnector.getVirtualInterfaces().values());
	}

	@Override
	public List<SummaryReportManagement> getSummaryReportList() {
		List<SummaryReportManagement> listSummary = new ArrayList<SummaryReportManagement>();
		listSummary.add(m2v2r0RanPhysicalParser.getSummaryManager());
		return listSummary;
	}

	@Override
	public List<ElementAdditionalInfo> getAddInfoList() {
		return m2v2r0RanLogicalConnector.getAddInfoList();
	}

	@Override
	public List<ElementRanBs> getRanBsList() {
		log.debug("Parsing List of RanBss for M2000_V2_R0_Ran");
		return new ArrayList<ElementRanBs>(m2v2r0RanLogicalConnector.getElementRanBss().values());
	}

	@Override
	public List<ElementCell2g> getCell2gList(Date date) {
		log.debug("Parsing List of 2G Cells for M2000_V2_R0_Ran");
		return new ArrayList<ElementCell2g>(m2v2r0RanLogicalConnector.getElementCells2G().values());
	}

	@Override
	public List<ElementTrx> getTrxList(Date date) {
		log.debug("Parsing List of TRX for M2000_V2_R0_Ran");
		return m2v2r0RanLogicalConnector.getElementTrxs();
	}

	@Override
	public List<ElementPcu> getPcuList(Date date) {
		log.debug("Parsing List of PCUs for M2000_V2_R0_Ran");
		return new ArrayList<ElementPcu>(m2v2r0RanLogicalConnector.getPcuCardMap().values());
	}

	@Override
	public List<ElementENodeB> getENodeBList(Date date) {
		log.debug("Parsing List of ENodeBs for M2000_V2_R0_Ran");
		return m2v2r0RanLogical4GParser.getElementENodeBs();
	}

	@Override
	public List<ElementCoreElement> getCoreElementList() {
		log.debug("Parsing List of coreElements for M2000_V2_R0_Ran");
		return new ArrayList<ElementCoreElement>(m2v2r0RanPhysicalParser.getCoreElementMap().values());
	}

	@Override
	public List<ElementCell3g> getCell3gList() {
		log.debug("Parsing List of 3G Cells for M2000_V2_R0_Ran");
		return new ArrayList<ElementCell3g>(m2v2r0RanLogicalConnector.getElementCells3G().values());
	}

	@Override
	public List<ElementCell4g> getCell4gList() {
		log.debug("Parsing List of 4G Cells for M2000_V2_R0_Ran");
		return new ArrayList<ElementCell4g>(m2v2r0RanLogical4GParser.getElementCells4G().values());
	}

	@Override
	public List<ElementMgw> getMgwList() {
		log.debug("Parsing List of Mgw for M2000_V2_R0_Ran");
		return new ArrayList<ElementMgw>(m2v2r0RanLogicalConnector.getElementMgws().values());
	}

	@Override
	public List<ElementMss> getMssList() {
		log.debug("Parsing List of Mss for M2000_V2_R0_Ran");
		return new ArrayList<ElementMss>(m2v2r0RanLogicalConnector.getElementMsss().values());
	}

	@Override
	public List<ElementNeighbor> getCellNeighborList() {
		return m2v2r0RanLogicalConnector.getElementNeighbors();
	}

	@Override
	public List<ElementTrxArfcn> getTrxArfcnList() {
		return m2v2r0RanLogicalConnector.getElementTrxArfcns();
	}

	@Override
	public List<ElementCellTrackingArea> getCellTrackingAreaList() {
		log.debug("Parsing List of CellTrackingArea for M2000_V2_R0_Ran");
		return m2v2r0RanLogical4GParser.getElementCellTrackingAreas();
	}

	@Override
	public List<ElementEnodebBandWidth> getENodeBBandWidthList() {
		log.debug("Parsing List of ENodeB BandWidth for M2000_V2_R0_Ran");
		return m2v2r0RanLogical4GParser.getElementEnodebBandWidths();
	}

	@Override
	public List<ElementCellRadioBandWidth> getCellRadioBandwidthList() {
		log.debug("Parsing List of Cell BandWidth for M2000_V2_R0_Ran");
		return m2v2r0RanLogical4GParser.getElementCellRadioBandWidths();
	}

	@Override
	public List<ElementNodeConnection> getNetworkElementConnectionList() {
		log.debug("Parsing List of NetworkElement Connection for M2000_V2_R0_Ran");
		return m2v2r0RanLogicalConnector.getElementNodeConnections();
	}

	@Override
	public List<ElementCarrier3g> getThreeGCarrierList() {
		log.debug("Parsing List of ThreeG Carrier for M2000_V2_R0_Ran");
		return m2v2r0RanLogicalConnector.getElementCarriers3g();
	}

	@Override
	public List<ElementIPData> getElementIPData() {

		List<ElementIPData> res = new ArrayList<ElementIPData>();
		res.addAll(m2v2r0RanLogical4GParser.getElementIPData());
		res.addAll(m2v2r0RanLogicalConnector.getElementIPData());

		return res;
	}

	@Override
	public List<ElementSpusForRnc> getSpusForRncList() {
		log.debug("Parsing List of SpusForRnc for M2000_V2_R0_Ran");
		return m2v2r0RanLogicalConnector.getElementSpusForRnc();
	}

	@Override
	public List<ElementGsmCellMaio> getGsmCellMaioList() {
		log.debug("Parsing List of ElementGsmCellMaio for M2000_V2_R0_Ran");
		return m2v2r0RanLogicalConnector.getElementGsmCellMaios();
	}

	@Override
	public List<RanLogicalInterface> getRanLogicalInterfaceList() {
		log.debug("Parsing List of RanLogicalInterface for M2000_V2_R0_Ran");
		List<RanLogicalInterface> logicalList = new ArrayList<RanLogicalInterface>();
		logicalList.addAll(m2v2r0_NodesLogicalInterfacesCollector.getRanLogicalInterfaceList());
		logicalList.addAll(m2v2r0RanLogicalConnector.getRanLogicalInterfaces());
		logicalList.addAll(m2v2r0RanLogical4GParser.getRanLogicalInterfaces());
		return logicalList;
	}

	@Override
	public List<RanInterfaceParticipant> getRanInterfaceParticipantList() {
		log.debug("Parsing List of RanInterfaceParticipant for M2000_V2_R0_Ran");
		List<RanInterfaceParticipant> logicalList = new ArrayList<RanInterfaceParticipant>();
		logicalList.addAll(m2v2r0_NodesLogicalInterfacesCollector.getRanInterfaceParticipantList());
		logicalList.addAll(m2v2r0RanLogicalConnector.getRanInterfaceParticipants());
		logicalList.addAll(m2v2r0RanLogical4GParser.getRanInterfaceParticipants());
		return logicalList;
	}

	@Override
	public List<RanLogicalInterfacePort> getRanLogicalInterfacePortList() {
		log.debug("Parsing List of RanLogicalInterfacePort for M2000_V2_R0_Ran");
		List<RanLogicalInterfacePort> logicalList = new ArrayList<RanLogicalInterfacePort>();
		logicalList.addAll(m2v2r0_NodesLogicalInterfacesCollector.getRanLogicalInterfacePortList());
		logicalList.addAll(m2v2r0RanLogicalConnector.getRanLogicalInterfacePorts());
		logicalList.addAll(m2v2r0RanLogical4GParser.getRanLogicalInterfacePorts());
		return logicalList;
	}

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

	@Override
	public List<RanVirtualInterfacePort> getRanVirtualInterfacePortList() {
		log.debug("Parsing List of RanVirtualInterfacePort for M2000_V2_R0_Ran");
		List<RanVirtualInterfacePort> logicalList = new ArrayList<RanVirtualInterfacePort>();
		logicalList.addAll(m2v2r0_NodesLogicalInterfacesCollector.getRanVirtualInterfacePortList());
		return logicalList;
	}

	@Override
	public List<NetworkElementDump> getNetworkElementDump() {
		return new ArrayList<NetworkElementDump>(m2v2r0RanPhysicalParser.getNetworkElementDumpMap().values());
	}*/
}
