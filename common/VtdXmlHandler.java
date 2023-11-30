package com.mobinets.nps.customer.transmission.manufacture.huawei.m2000v2r0en0sp2_ran_stable.parser.common;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.common.io.ByteStreams;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.extended.VTDNavHuge;

public class VtdXmlHandler {

	private static Logger log = Logger.getLogger(VtdXmlHandler.class);
	
	private static Pattern nameSpacePattern = Pattern.compile("xmlns((:)([a-z]+))?");
	private static Pattern pattern = Pattern.compile("(?i)encoding=\"(.[^\"]*)\"");

	public static String getAttributeValue(String attributeName, VTDNav vn) {
		try
		{
			if (vn.hasAttr(attributeName)) {
				int attributeValueIndex = vn.getAttrVal(attributeName);
				if (attributeValueIndex != -1) {
					return vn.toNormalizedString(attributeValueIndex);
				}
			}
		}
		catch (Exception e)
		{
			log.error("Cannot get attribute " + attributeName);
		}
		return null;
	}
	
	public static String getTagValue(VTDNav vn, AutoPilot ap, String query) {
		
		String result = null;
		try
		{
			ap.selectXPath(query);
			
			int i = ap.evalXPath();
			if(i != -1)
			{
				int index = vn.getText();
				if (index != -1)
					result = vn.toNormalizedString(index);
			}
		}
		catch (Exception e)
		{
			log.error("Cannot get value from tag " + query);
		}
		
		return result;
	}
	
	public static void analyzeNamespaces(VTDNav vtdNav, AutoPilot ap) {
		
		try
		{
			Map<String, String> registeredNamespaces = new HashMap<String, String>();
			
	        VTDNav namespaceNavigation = vtdNav.cloneNav();
	        boolean rootElementAnalyzed = false;
	        int index = namespaceNavigation.getRootIndex();
	        while (!rootElementAnalyzed) {
	            index++;
	            if (namespaceNavigation.getTokenType(index) == VTDNavHuge.TOKEN_STARTING_TAG || namespaceNavigation.getTokenType(index) == VTDNavHuge.TOKEN_ENDING_TAG)
	                rootElementAnalyzed = true;
	            else
	            {
	                try
	                {
	                    if (namespaceNavigation.getTokenType(index) == VTDNavHuge.TOKEN_ATTR_NS)
	                    {
	                        Matcher namespaceMatcher = nameSpacePattern.matcher(namespaceNavigation.toString(index));
	                        if (namespaceMatcher.matches())
	                        {
	                            String prefix = namespaceMatcher.group(3);
	                            index++;
	                            if (prefix != null)
	                                registeredNamespaces.put(prefix, namespaceNavigation.toString(index));
	                        }
	                    }
	                }
	                catch (Exception ex)
	                {
	                	log.error("could not parse namespace", ex);
	                }
	            }
	        }
	        
	        for (Map.Entry<String, String> entry : registeredNamespaces.entrySet()) {
	        	ap.declareXPathNameSpace(entry.getKey(), entry.getValue());
	        }
		}
		catch (Exception e)
		{
			log.error("Cannot analyze Namespaces", e);
		}
    }
	
	public static VTDGen getVTDGen(InputStream inputStream) {
		try {
			byte[] bytes = sanitizeXml(ByteStreams.toByteArray(inputStream));

			VTDGen vg = new VTDGen();
			vg.setDoc(bytes);
			vg.parse(true);
			return vg;
		} catch (Exception e) {
			log.error("Could not parse the file due to : " + e.getMessage(),e);
		} 
		return null;
	}
	
	
	private static byte[] sanitizeXml(byte[] in) {
		String inS = new String(in, Charset.forName("UTF-8"));
		Matcher m = pattern.matcher(inS);
		if(m.find()){
			String encoding = m.group(0);
			inS = inS.replace(encoding, "encoding=\"UTF-8\"");
		}
		return inS.getBytes();
	}
}
