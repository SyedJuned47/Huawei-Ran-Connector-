/**
 * 
 */
package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.test;

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
public class AUOverIpBscMgwFRSParserPrinter extends InterfaceFRSParserPrinter{

	private DumpDb dumpsDb = null;
	
	/**
	 * 
	 */
	public AUOverIpBscMgwFRSParserPrinter(String _fileName) {
		super(_fileName);
	}
	
	public void process(){
		List<DumpRow> adjnodeRows = dumpsDb.select().from("ADD ADJNODE").where("NODET").equals("A").and("ANI").equals("0").listSelect();
		Set<DumpRow> ippathRows = new HashSet<DumpRow>();
		Set<DumpRow> devipRows = new HashSet<DumpRow>();
		Set<DumpRow> ethipRows = new HashSet<DumpRow>();
		Set<DumpRow> iprtRows = new HashSet<DumpRow>();
		
		Set<DumpRow> selectedEthipRows = new HashSet<DumpRow>();
		Set<DumpRow> selectedIprtRows = new HashSet<DumpRow>();
		
		
		for(DumpRow adjnodeRow: adjnodeRows){
			
			String ani = adjnodeRow.get("ANI");					
			List<DumpRow> ippaths = dumpsDb.select("ANI","ECHOIP","TXBW","RXBW","IPADDR","PEERIPADDR","PEERMASK").from("ADD IPPATH").where("ANI").equals(ani).listSelect();
			ippathRows.addAll(ippaths);
			for(DumpRow ippathRow: ippaths){
				String ipaddr = ippathRow.get("IPADDR");
				
				List<DumpRow> devips = dumpsDb.select("IPADDR","MASK","SRN","SN").from("ADD DEVIP").where("IPADDR").equals(ipaddr).listSelect();
				devipRows.addAll(devips);
				for(DumpRow devipRow: devips){
					String srndevip = devipRow.get("SRN");
					String sndevip = devipRow.get("SN");
					List<DumpRow> ethips = dumpsDb.select("IPADDR","MASK","SRN","SN","PN").from("ADD ETHIP")
							.where("SRN").equals(srndevip).and("SN").equals(sndevip).listSelect();
					ethipRows.addAll(ethips);
					
					String peeripaddr = ippathRow.get("PEERIPADDR");
					
					List<DumpRow> iprts = dumpsDb.select("SRN","SN","NEXTHOP","DSTIP","DSTMASK").from("ADD IPRT")
					.where("DSTIP").equals(peeripaddr).and("SRN").equals(srndevip).and("SN").equals(sndevip).listSelect();
					iprtRows.addAll(iprts);					
				}
			}
		}
		
		for(DumpRow row : iprtRows){
			String nextHop = row.get("NEXTHOP");
			List<DumpRow> ethips = dumpsDb.select().from(ethipRows).whereIpRange("IPADDR", "MASK").containsIp(nextHop).listSelectIpRange();
			if(ethips.size() >0){
				selectedIprtRows.add(row);
				selectedEthipRows.addAll(ethips);
			}
		}
		
		

		println("ADD ADJNODE", adjnodeRows);
		println();
		println("ADD IPPATH", ippathRows);
		println();
		println("ADD DEVIP",  devipRows);
		println();
		println("ADD ETHIP",  ethipRows);
		println();
		println("ADD IPRT", iprtRows);
		println();
		println("ADD ETHIP", selectedEthipRows);
		println();
		println("ADD IPRT", selectedEthipRows);
		
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
        AUOverIpBscMgwFRSParserPrinter tester = new AUOverIpBscMgwFRSParserPrinter(filename);
        tester.dumpsDb =  DumpParserFactory.newInstance(DumpsOrigin.huawei).newParser(DumpsType.m2000v2r0enosp2_ran).parse(filename);
        tester.process();
	}

}
