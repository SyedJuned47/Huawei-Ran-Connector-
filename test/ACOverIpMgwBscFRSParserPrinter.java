/**
 * 
 */
package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.test;

import java.util.List;

import com.mobinets.nps.customer.transmission.manufacture.common.DumpParserFactory;
import com.mobinets.nps.customer.transmission.manufacture.common.DumpsOrigin;
import com.mobinets.nps.customer.transmission.manufacture.common.DumpsType;
import com.mobinets.toolbox.ai.common.DumpDb;
import com.mobinets.toolbox.ai.common.DumpRow;

/**
 * @author majouz
 *
 */
public class ACOverIpMgwBscFRSParserPrinter extends InterfaceFRSParserPrinter{

	private DumpDb dumpsDb = null;
	
	/**
	 * 
	 */
	public ACOverIpMgwBscFRSParserPrinter(String _fileName) {
		super(_fileName);

	}

	
	public void process(){
		
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
        ACOverIpMgwBscFRSParserPrinter tester = new ACOverIpMgwBscFRSParserPrinter(filename);
        tester.dumpsDb =  DumpParserFactory.newInstance(DumpsOrigin.huawei).newParser(DumpsType.m2000v2r0enosp2_ran).parse(filename);
        tester.process();
	}

}
