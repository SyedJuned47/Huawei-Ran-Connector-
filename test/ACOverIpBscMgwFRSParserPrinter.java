/**
 * 
 */
package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mobinets.nps.customer.transmission.manufacture.common.DumpParserFactory;
import com.mobinets.nps.customer.transmission.manufacture.common.DumpsOrigin;
import com.mobinets.nps.customer.transmission.manufacture.common.DumpsType;
import com.mobinets.toolbox.ai.common.DumpDb;
import com.mobinets.toolbox.ai.common.DumpRow;

/**
 * @author majouz
 *
 */
public class ACOverIpBscMgwFRSParserPrinter extends InterfaceFRSParserPrinter{

	private DumpDb dumpsDb = null;
	
	/**
	 * 
	 */
	public ACOverIpBscMgwFRSParserPrinter(String _fileName) {
		super(_fileName);
	}
	
	public void process(){
		List<DumpRow> n7dpcRows = dumpsDb.select().from("ADD N7DPC").where("DPCT").equals("A").listSelect();
		Set<DumpRow> m3deRows = new HashSet<DumpRow>();
		Set<DumpRow> m3lksRows = new HashSet<DumpRow>();
		Set<DumpRow> m3lnkRows = new HashSet<DumpRow>();
		Set<DumpRow> sctplnkRows = new HashSet<DumpRow>();
		Set<DumpRow> devipRows = new HashSet<DumpRow>();
		Set<DumpRow> ethipRows = new HashSet<DumpRow>();
		Set<DumpRow> iprtRows = new HashSet<DumpRow>();
		
		
		for(DumpRow n7dpcRow: n7dpcRows){
			
			String dpx = n7dpcRow.get("DPX");					
			Collection<DumpRow> m3des = dumpsDb.select().from("ADD M3DE").where("DPX").equals(dpx).listSelect();
			m3deRows.addAll(m3des);
			for(DumpRow m3deRow: m3des){
				String deno = m3deRow.get("DENO");
				Collection<DumpRow> m3lkss = dumpsDb.select().from("ADD M3LKS").where("DENO").equals(deno).listSelect();
				m3lksRows.addAll(m3lkss);
				for(DumpRow m3lksRow: m3lkss){
					String siglksx = m3lksRow.get("SIGLKSX");
					Collection<DumpRow> m3lnks = dumpsDb.select().from("ADD M3LNK").where("SIGLKSX").equals(siglksx).listSelect();
					m3lnkRows.addAll(m3lnks);
					for(DumpRow m3lnkRow: m3lnks){
						String srn = m3lnkRow.get("SRN");
						String sn = m3lnkRow.get("SN");
						String sctplnkn = m3lnkRow.get("SCTPLNKN");
						Collection<DumpRow> sctplnks = dumpsDb.select("LOCIP1","SRN","SN","SCTPLNKN","PEERIP1","PEERIP2")
								.from("ADD SCTPLNK").where("SRN").equals(srn).and("SN").equals(sn).and("SCTPLNKN").equals(sctplnkn).listSelect();
						sctplnkRows.addAll(sctplnks);
						for(DumpRow sctplnkRow: sctplnks){
							String locip1 = sctplnkRow.get("LOCIP1");
							Collection<DumpRow> devips = dumpsDb.select("IPADDR","MASK","SRN","SN").from("ADD DEVIP")
									.where("IPADDR").equals(locip1).listSelect();
							devipRows.addAll(devips);
							for(DumpRow devipRow: devips){
								String srndevip = devipRow.get("SRN");
								String sndevip = devipRow.get("SN");
								Collection<DumpRow> ethips = dumpsDb.select("IPADDR","MASK","SRN","SN","PN").from("ADD ETHIP")
										.where("SRN").equals(srndevip).and("SN").equals(sndevip).listSelect();
								ethipRows.addAll(ethips);
								
							}
						}
					}
				}
			}
		}
		
		
		for(DumpRow sctplnk: sctplnkRows){
			String peerip1 = sctplnk.get("PEERIP1");
			List<DumpRow> iprts = dumpsDb.select("SRN","SN","NEXTHOP","DSTIP","DSTMASK").from("ADD IPRT")
					.whereIpRange("DSTIP", "DSTMASK").containsIp(peerip1).listSelectIpRange();
			iprtRows.addAll(iprts);			
		}
		
		
		Set<DumpRow> selectedEthipRows = new HashSet<DumpRow>();
		Set<DumpRow> selectedIprtRows = new HashSet<DumpRow>();

		for(DumpRow iprtRow:iprtRows){
			String nexthop = iprtRow.get("NEXTHOP");
			List<DumpRow> ethips = dumpsDb.select("IPADDR","MASK","SRN","SN","PN").from(ethipRows)
					.whereIpRange("IPADDR", "MASK").containsIp(nexthop).listSelectIpRange();
			
			if(ethips != null && ethips.size() >0){
				selectedEthipRows.addAll(ethips);
				selectedIprtRows.add(iprtRow);
			}
			
		}

		println("ADD N7DPC", n7dpcRows);
		println();
		println("ADD M3DE", m3deRows);
		println();
		println("ADD M3LKS", m3lksRows);
		println();
		println("ADD M3LNK", m3lnkRows);
		println();
		println("ADD SCTPLNK", sctplnkRows);
		println();
		println("ADD DEVIP", devipRows);
		println();
		println("ADD ETHIP", ethipRows);
		println();
		println("ADD IPRT", iprtRows);
		println();
		println("ADD ETHIP", selectedEthipRows);
		println();
		println("ADD IPRT", selectedIprtRows);
		

		

		
	}
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = null;
        
        
        for(String s: args){
        	filename = s;
        }

        if (filename == null) {
            System.err.println("Usage: java <huawei file>");
            System.exit(1);
        }
        ACOverIpBscMgwFRSParserPrinter tester = new ACOverIpBscMgwFRSParserPrinter(filename);
        tester.dumpsDb =  DumpParserFactory.newInstance(DumpsOrigin.huawei).newParser(DumpsType.m2000v2r0enosp2_ran).parse(filename);
        tester.process();
	}

}
