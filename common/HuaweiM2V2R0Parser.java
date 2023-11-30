/**
 * 
 */
package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobinets.nps.customer.transmission.common.TransmissionCommon;
import com.mobinets.toolbox.ai.common.DumpDb;
import com.mobinets.toolbox.ai.common.DumpDbFactory;
import com.mobinets.toolbox.ai.common.DumpParser;
import com.mobinets.toolbox.ai.common.DumpRow;

/**
 * @author majouz
 *
 */
public class HuaweiM2V2R0Parser extends DumpParser {

	private static final Log log = LogFactory.getLog(HuaweiM2V2R0Parser.class);

	public static final Pattern lineStartsWithRegex = Pattern.compile("(?i)(.*?)\\s(.*?):(.*)");
	public static final Pattern lineValuesRegex = Pattern.compile("(.*?)=(.*?)(,|;)");
	//public static final Pattern addLinePattern = Pattern.compile("(?i)(\\b(?i)ADD\\s(.*?):.*\\b).*");
	public static final Pattern linePattern = Pattern.compile("(?i)(\\b(?i)(ADD|SET|Sys|UIN|DEA|BRD|USE|MOD|POOLINFO|LAIGCI|LAISAI|MGC|ACT)\\s(.*?):.*\\b).*");
	

	
	private DumpDb dumpsFileDb = null;
	private File file = null;
	private CharBuffer cb = null;

	
	
	public HuaweiM2V2R0Parser() {
		
	}
	
	public static void parseRow(String row, DumpDb _dumpDb){
		if(row == null || row.isEmpty()){
			return;
		}
		
		Matcher m = linePattern.matcher(CharBuffer.wrap(row));
		if(m.find()){
			add(m.group(), _dumpDb);
		}
	}
	
	protected static void add(String rowFile, DumpDb _dumpDb){
		Map<String, String> row = ungroup(rowFile);
		
		if(row != null && !row.isEmpty()){

			String operation = row.get("operation");
			String operationDesc = row.get("operationDesc");
			
			String tableName = 	getTableName(operation, operationDesc);
			row.remove("operation");
			row.remove("operationDesc");
			_dumpDb.insertInto(tableName).values(new DumpRow(row));
		}

	}

	protected void add(String rowFile){//this is more related to parsing M2V2R0 file
		add(rowFile, dumpsFileDb);	
	}
	
	protected static Map<String,String> ungroup(String rowFile){
		Map<String, String> row = TransmissionCommon.getData(lineStartsWithRegex, lineValuesRegex, rowFile);
		return row;
	}
	
	
	protected static String getTableName(String operation, String operationDesc){//this is more related to parsing
		return new StringBuffer(operation).append(" ").append(operationDesc).toString();
	}

	
	protected void parseRows(){

		Matcher m = null;
		m = linePattern.matcher(cb);
		
		while (m.find()){
			add(m.group());
		}
	}
	
	

	@Override
	public DumpDb parse(String _file) {
		dumpsFileDb = DumpDbFactory.getInstance(_file);
		FileInputStream fis = null;
		FileChannel fc = null;
		ByteBuffer bb = null;
		try
		{
			file = new File(_file);
			fis = new FileInputStream(file);
			fc = fis.getChannel();
			bb = fc.map(MapMode.READ_ONLY, 0, (int) fc.size());
			Charset cs = Charset.forName("8859_1");
			CharsetDecoder cd = cs.newDecoder();
			cb = cd.decode(bb);
			

			parseRows();
						
			log.info("**********    Finished parsing of " + file + 
					" in huawei logical dumps   **********");
			
		}
		catch(Exception e)
		{
			log.error("Error", e);
		}finally{
			if(fc!=null && fc.isOpen())
				try
				{
					fc.close();
				}
                catch(IOException e)
                {
                    e.printStackTrace();
                }
			if(fis!=null){
				try
                {
                    fis.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
			}
			if(bb!=null)
				bb.clear();
			if(cb!=null)
				cb.clear();
		}
		return dumpsFileDb;
	}
	
	public static void main(String[] args) {
//		String filename = null;
//        
//        
//        for(String s: args){
//        	filename = s;
//        }
//
//        if (filename == null) {
//            System.err.println("Usage: java <huawei file>");
//            System.exit(1);
//        }
//        
//        new HuaweiM2V2R0Parser().parse(filename);
		
		String line = "ADD M3RT: RN=\"BSDMDWT1\", DEX=81, LSX=81;";
		Matcher m = linePattern.matcher(CharBuffer.wrap(line));
		if(m.find()){
			System.out.println(m.group());
		}
		else{
			System.out.println("not found");
		}
		
        
		
	}
}
