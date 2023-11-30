package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobinets.nep.model.autoimporter.Category;
import com.mobinets.nps.customer.transmission.common.TransmissionCommon;
import com.mobinets.nps.customer.transmission.manufacture.common.ConnectorUtility;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.cfg.M2V2R0RanLogicalDataCollector;
import com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.logicalparser.cfg.M2V2R0RanLogicalDataCollector.AddVLANID;
import com.mobinets.nps.model.autoimporter.ElementIPData;
import com.mobinets.nps.model.autoimporter.ElementIPDetails;
import com.mobinets.nps.model.autoimporter.ElementIPDetails.RANTrafficType;
import com.mobinets.nps.model.nodeinterfaces.LogicalInterface;
import com.mobinets.nps.model.nodeinterfaces.NodeInterface;
import com.mobinets.toolbox.ai.common.DumpDb;
import com.mobinets.toolbox.ai.common.DumpRow;
import com.mobinets.toolbox.utils.ipmpls.IPSubnetUtils;

public class M2V2R0RanCommon {
	
	private static final Log log = LogFactory.getLog(M2V2R0RanCommon.class);

	/**
	 * 
	 * @param mask
	 * @param neType
	 * @param neName
	 * @return
	 */
	public static String fixSubnetMask(String mask, String neType, String neName, String fileName) {
		if(mask == null)
			// default mask
			return "255.255.255.248";
		
		String newMask = mask;
		if(mask.equals("255.255.255.255"))
		{
			newMask = "255.255.255.248";
			TransmissionCommon.addWarningAlarm(Category.INVALID_IP_SUBNET, neType, "Mask of " + neType + ": " + neName + " is changed from " + mask +
					" to " + newMask, fileName);
		}
		return newMask;
	}
	
	/**
	 * @param lac
	 * @param cellId
	 * @param elementNodeB
	 * @param fileName
	 * @param withError
	 * @return
	 */
	public static Integer getLacInt(String lac, String cellId, String cellName, String cellType, String fileName, boolean withError, Pattern hexadecimalPattern) {
		Integer lacInt = null;
		if(lac != null)
		{
			Matcher hexadecimalMatcher = hexadecimalPattern.matcher(lac);
			if(hexadecimalMatcher.matches())
			{
				try
				{
					lacInt = Integer.parseInt(hexadecimalMatcher.group(1), 16);
				}
				catch(Exception e)
				{
					if(withError)
						TransmissionCommon.addErrorAlarm(Category.SHOULDBE_INTEGER, cellType, "LAC: " + lac + " could not be converted from Hexa to Decimal for Cell: (" +
								cellId + ", " + cellName + ")", fileName);
				}
			}
			else
			{
				try
				{
					lacInt = Integer.parseInt(lac);
				}
				catch(Exception e)
				{
					if(withError)
						TransmissionCommon.addErrorAlarm(Category.SHOULDBE_INTEGER, cellType, "LAC: " + lac + " has not the form of Hexa Decimal and it is not Integer" +
								"for Cell: ("+ cellId + ", " + cellName + ")", fileName);
				}
			}
		}
		return lacInt;
	}
	
	/**
	 * 
	 * @param pathFolder
	 * @return
	 */
	public static List<File> filterLastApdatedFiles(String pathFolder) {
		List<File> files = new ArrayList<File>();
		try
		{
			ConnectorUtility.listf(pathFolder, files);
						
			Map<String, File> fileMap = new HashMap<String, File>();
			Collections.sort(files, Collections.reverseOrder()); // Order by last updated
			for(File file : files) {
				String fileName = file.getName();
				int duchOccurance = fileName.replaceAll("[^-]", "").length();
				if(duchOccurance > 2) {
					int lastDuchIndex = fileName.lastIndexOf("-");
					fileName = fileName.substring(0, lastDuchIndex) + fileName.substring(fileName.lastIndexOf("."));
				}
				
				if(!fileMap.containsKey(fileName))
					fileMap.put(fileName, file);
			}
			
			if(!fileMap.isEmpty()) {
				files.clear();
				files.addAll(fileMap.values());
				fileMap.clear();
			}
		}
		catch(Exception e)
		{
			log.error("Error", e);
		}
		return files;
	}
	
	/**
	 * 
	 * @param setEthip
	 * @param nextHopIp
	 * @return
	 */
	public static Set<String> deducePhysicalConfFromNextHopIp(Set<String> ipsSet, String nextHopIp) {
		
		Set<String> physicalConfDeduced = new HashSet<String>();
		
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		for(String ips : ipsSet)
		{
			String ip =  ips.split("_")[0];
			String mask =  ips.split("_")[1];
			
			IPSubnetUtils ipSubnetUtils = new IPSubnetUtils(ip, mask);
			String networkAddr = ipSubnetUtils.getInfo().getNetworkAddress();
			
			Set<String> set = map.get(networkAddr + "_" + mask);
			if(set == null)
				set = new HashSet<String>();
			set.add(ips);
			
			map.put(networkAddr + "_" + mask, set);
		}
		

		for(String key : map.keySet())
		{
			String mask = key.split("_")[1];
			
			IPSubnetUtils ipSubnetUtils = new IPSubnetUtils(nextHopIp, mask);
			String networkAddrForNextHop = ipSubnetUtils.getInfo().getNetworkAddress();
			
			String network = key.split("_")[0];
			
			if(networkAddrForNextHop.equals(network))
				physicalConfDeduced.addAll(map.get(key));
		}
		
		return physicalConfDeduced;
	}
	
	
	public static String compute2GVlanId(DumpDb dumpDb, String nextHop){
		String vlanId = null;
		if(nextHop != null){
			List<DumpRow> vlansRow = dumpDb.from("ADD VLANID").where("IPADDR").equals(nextHop).listSelect();
			for(DumpRow dr: vlansRow){
				vlanId = dr.get("VLANID");
				break;
			}
		}
		return vlanId;
	}
	
	public static String compute2GVlanId(M2V2R0RanLogicalDataCollector m2v2r0RanLogicalDataCollector, String nextHop){
		String vlanId = null;
		if(nextHop != null){
			for(AddVLANID dr: m2v2r0RanLogicalDataCollector.getAddvlanidlist()){
				vlanId = dr.vlanid;
				break;
			}
		}
		return vlanId;
	}
	
	/**
	 * 
	 * @param elementIPDataMap
	 * @param id
	 * @param nodeType
	 * @param shelfIdx
	 * @param slotIdx
	 * @param ip
	 * @param mask
	 */
	public static ElementIPData createElementIpData(Map<String, ElementIPData> elementIPDataMap, String id, String type, String cabIdx, String shelfIdx, String slotIdx,
			String boardIdx, String portIdx, String ip, String mask, String bandWidth, RANTrafficType ranTrafficType) {
		
		ElementIPData elementIPData = elementIPDataMap.get(id.toUpperCase());
		if(elementIPData == null)
		{
			elementIPData = new ElementIPData();
			elementIPData.setId(id);
			elementIPData.setType(type);
		}
		if(elementIPData.getIps() == null)
			elementIPData.setIps(new HashSet<ElementIPDetails>());
		
		ElementIPDetails ipData = new ElementIPDetails();
		ipData.setSubRack(shelfIdx);
		ipData.setSlotNb(slotIdx);
		ipData.setIpAddress(ip);
		ipData.setMask(mask);		
		ipData.setRack(cabIdx);
		ipData.setBoardNb(boardIdx);
		ipData.setPortNb(portIdx);		
		ipData.setTrafficType(ranTrafficType);
		ipData.setBandWidth(bandWidth);
		
		elementIPData.getIps().add(ipData);
		
		elementIPDataMap.put(id.toUpperCase(), elementIPData);
		
		return elementIPData;
	}
	
	public static class IpPath {
		public String ani;
		public String txbw;
		public String rxbw;
		public String ip;
		public String peerIp;
		public String peerMask;
	}
	
	public static class Aal2Path {
		public String cabinet;
		public String shelf;
		public String slot;
		public String portFromCARRYNCOPTN;
		public String portFromCARRYIMAGRPN;
	}
	
	public static class Unodebesn {
		public String cabinet;
		public String shelf;
		public String slot;
	}
	
	public static class Devip {
		public String srn;
		public String sn;
	}
	
	
	public static class Ethip {		
		public String ip;
		public String srn;
		public String sn;
		public String board;
		public String pn;
		public String mask;
		public String ipIndex;		
		public String remark;
		public String cabinet;
		public String neId;
		public List<IpRoute> ipRoutes = new ArrayList<>();
	}
	
	public static class Iprt {
		public String dstIp;
		public String dstMask;
		public String srn;
		public String sn;
		public String nextHopIp;
		public String type;
	}
	public static class Iprt_gate {
		public String ip;
		public String mask;
		public String gate;
	}
	
	public static class Sctplnk {
		public String locip1;
		public String locip2;
		public String srn;
		public String sn;
		public String peerIp1;
		public String peerIp2;
		public String sctplnkn;
		public String id;
	}
	
	public static class N7dpc {
		public String dpct;
		public String dpc;
		public String spx;
		public String dpx;
	}
	
	public static class M3lnk {
		public String siglksx;
		public String srn;
		public String sn;
		public String sctplnkn;
	}
	
	public static class BtsDevip {
		public String ip;
		public String mask;
		public String btsId;
		public String cn;
		public String srn;
		public String sn;
		public String pn;
	}
	
	public static class AdjNode {
		public String name;
		public String nodet;			
		public String transt;
		public String nodeBId;
		public String ani;
		public String dpx;
		public String btsId;
		public String poolIndex;
		public String txbw;
		public String rxbw;
	}
	
	public static class IpPool {
		public String ip;
		public String index;			
		public String srn;
		public String sn;
	}
	
	public static class AbisCP {
		public String id;
		public String btsId;			
		public String type;
	}
	
	public static class BtsConnect {
		public String destNode;
		public String btsId;
		public String incn;
		public String insrn;
		public String insn;
		public String inpn;
		public String srn;
		public String sn;
		public String pn;
		public String upbtsid;
		public String fcn;
		public String fsrn;
		public String fsn;
		public String fpn;
	}
	
	public static class Gcnnode {
		public String cnId;
		public String dpcgidx;
		
	}
	
	public static class Ae1t1 {
		public String dpcgidx;
		public String srn;
		public String sn;
		public String pn;
	}
	
	public static class Nse {
		public String nsei;
		public String cnId;
		public String svrip;
	}
	
	public static class NsvlLocal {
		public String nsei;
		public String srn;
		public String sn;
		public String ip;
	}
	
	public static class NodeIpam{
		public String nodeName;
		public Set<NodeInterface> nodeInterfaceSet = new HashSet<NodeInterface>();
		public Set<LogicalInterface> logicalInterfaceSet = new HashSet<LogicalInterface>();
	}
	
	public static class Uncp {
		public String nodebid ;
		public String logicrncid;
		public String carrylnkt;
		public String sctplnkid;
		public String idtype;
	}
	public static class Uccp {
		public String nodebid ;
		public String logicrncid;
		public String carrylnkt;
		public String sctplnkid;
		public String idtype;
		public String pn;
	}
	public static class EthTrkIp {
		public String type ;
		public String name;
		public String srn ;
		public String sn;
		public String trkn;
		public String ipindex;
		public String ipaddr;
		public String mask;
		public String gateway;
		public String vlan;
		public String pn;
		public String cabinet;
		@Override
		public String toString() {
			return "EthTrkIp [type=" + type + ", name=" + name + ", srn=" + srn + ", sn=" + sn + ", trkn=" + trkn
					+ ", ipindex=" + ipindex + ", ipaddr=" + ipaddr + ", mask=" + mask + ", gateway=" + gateway
					+ ", vlan=" + vlan + "]";
		}
		
	}
	public static class EthTrkLinkIp {
		public String srn ;
		public String trklnksn;
		public String trklnkpn;
		public String sn;
		public String trkn;
		public String workmode;
		public String portpri;
	}
	
	public static class IpRoute {
		public String dstIp;
		public String dstMask;
		public String nextHopIp;
	}
	
	public static class M2000Physical{
		public String file;
		public String nefdn;
		public String nename;
		public String netype;
		public List<Rack> racks;
		public List<Frame> frames;
		public List<Board> boards;
		public List<HostVer> hostvers;
		public List<GCell> gcells;
		public List<UCell> ucells;
		public List<LCell> lcells;
		public NodeB nodeb;
		public Bts bts;
		public ENodeB enodeb;
		public List<BtsForBsc> btsforbsc;
	}
	
	public static class Rack{
		public String rackno;
		public String racktype;
		public String bomcode;
		public String dateofmanufacture;
		public String issuenumber;
		public String serialnumber;
		public String manufacturerdata;
	}
	
	public static class Frame{
		public String rackno;
		public String frameno;
		public String frametype;
		public String bomcode;
		public String dateofmanufacture;
		public String issuenumber;
		public String serialnumber;
		public String manufacturerdata;
	}
	
	public static class Board {
		public String rackno;
		public String frameno;
		public String slotno;
		public String slotpos;
		public String subslotno;
		public String boardname;
		public String boardtype;
		public String vendorunitfamilytype;
		public String serialnumber;
		public String dateofmanufacture;
		public String unitposition;
		public String manufacturerdata;
		public String issuenumber;
		public String bomcode;
		public String portno;
		public String item;
	}
	
	public static class NodeB {
		public String nodebfdn;
		public String nodebname;
		public String nodebtype;
		public String rncname;
	}
	
	public static class Bts {
		public String btsfdn;
		public String btsname;
		public String btstype;
		public String bscname;
	}
	
	public static class ENodeB {
		public String enodebfdn;
		public String enodebname;
		public String enodebtype;
		public String fddtddind;
	}
	
	
	public static class HostVer {
		public String hostversion;
		public String hostvernetype;
	}
	
	public static class GCell {
		public String cellid;
		public String cellname;
		public String gbtsfunctionname;
	}
	
	public static class UCell {
		public String cellid;
		public String cellname;
		public String nodebfunctionname;
		public String nodebname;
	}
	
	public static class LCell {
		public String cellid;
		public String cellname;
		public String enodebfunctionname;
	}
	
	public static class BtsForBsc {
		public String sitename;
		public String siteindex;
		public String sitetype;
	}
	
	public static class Antenna {
		public String inventoryunitid;
		public String unitposition;
		public String antennadevicetype;
		public String serialnumber;
		public String serialnumberex;
		public String neid;
		public String parentnode;
		public String site;
		public String file;

	}
}
