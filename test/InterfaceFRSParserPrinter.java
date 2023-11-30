/**
 * 
 */
package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.test;

import java.util.Collection;
import java.util.Properties;

import com.mobinets.toolbox.ai.common.DumpRow;
import com.mobinets.toolbox.ai.common.FilePrinter;


/**
 * @author majouz
 *
 */
public abstract class InterfaceFRSParserPrinter {

	FilePrinter printer = null;
	
	
	/**
	 * 
	 */
	public InterfaceFRSParserPrinter(String _dumpFileName) {
		printer = new FilePrinter(_dumpFileName);
	}
	
	
	protected void println(String message){
		printer.print(message);
	}
	
	
	protected void println(String rowName, DumpRow keyValue){
		StringBuffer sb = new StringBuffer();
		sb.append(rowName).append(":");
		for(String k: keyValue.keySet()){
			sb.append(k+"="+keyValue.get(k)+",");
		}
		println(sb.toString());
	}
	
	protected void println(String rowName, Collection<DumpRow> list){

		for(DumpRow row: list){
			println(rowName, row);
		}

	}
	
	protected void println(){
		println("");
	}
	
	public static void main(String[] args) {
		Properties props = System.getProperties();
		for(Object k: props.keySet()){
			System.out.println(k +"="+props.getProperty(k.toString()));
		}
		
	}


}
