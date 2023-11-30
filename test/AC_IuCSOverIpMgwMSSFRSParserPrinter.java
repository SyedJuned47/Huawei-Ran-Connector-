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
public class AC_IuCSOverIpMgwMSSFRSParserPrinter extends InterfaceFRSParserPrinter{

	private DumpDb dumpsDb = null;
	
	/**
	 * 
	 */
	public AC_IuCSOverIpMgwMSSFRSParserPrinter(String _fileName) {
		super(_fileName);

	}

	
	public void process(){ 
		List<DumpRow> m3de = dumpsDb.from("ADD M3DE").listSelect();
		List<DumpRow> m3lks = dumpsDb.from("ADD M3LKS").where("ADX").in("DEX", m3de).listSelect();
		List<DumpRow> m3lnk = dumpsDb.from("ADD M3LNK").where("LSX").in("LSX", m3lks).listSelect();
		Set<DumpRow> ipAddrs = new HashSet<DumpRow>();
		ipAddrs.addAll(dumpsDb.select("IPADDR").from("ADD IPADDR").where("IPADDR").in("LIP1", m3lnk).listSelect());
		ipAddrs.addAll(dumpsDb.select("IPADDR").from("ADD IPADDR").where("IPADDR").in("LIP2", m3lnk).listSelect());
		
		printer.println("ADD M3DE", m3de);
		printer.println("ADD IPADDR", ipAddrs);
		
		
		
		
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
        AC_IuCSOverIpMgwMSSFRSParserPrinter tester = new AC_IuCSOverIpMgwMSSFRSParserPrinter(filename);
        tester.dumpsDb =  DumpParserFactory.newInstance(DumpsOrigin.huawei).newParser(DumpsType.m2000v2r0enosp2_ran).parse(filename);
        tester.process();
	}

}
