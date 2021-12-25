// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages all of the configuration settings available within Harmony
 * @author CWOLF
 */
public class ConfigManager
{
	// Patterns used to extract configuration information
	static private Pattern configPattern = Pattern.compile("<config>(.*?)</config>");
	static private Pattern parmPattern = Pattern.compile("<(.*?)>(.*?)</\\1>");
	
	/** Private class for managing parameters */
	static private class Parms
	{
		/** Stores all of the parameters */
		private HashMap<String,String> parms = new HashMap<String,String>();
		
		/** Extracts parameters */
		private void extractParms(String prefix, String value)
		{
			boolean matchesFound = false;
			Matcher parmMatcher = parmPattern.matcher(value);
			while(parmMatcher.find())
			{
				extractParms(prefix+(prefix.length()>0?".":"")+parmMatcher.group(1),parmMatcher.group(2));
				matchesFound = true;
			}
			if(!matchesFound && prefix.length()>0) parms.put(prefix,value.trim());
		}
		
		/** Constructs the parameter class */
		private Parms(String parms)
			{ extractParms("",parms); }
		
		/** Returns the specified parameter */
		private String get(String parm)
			{ return parms.get(parm); }
		
		/** Sets the specified parameter */
		private void set(String parm, String value)
			{ parms.put(parm,value); }
		
		/** Displays the parameters in XML format */
		public String toString()
		{
			StringBuffer output = new StringBuffer();
			
			ArrayList<String> parmList = new ArrayList<String>(parms.keySet());
			Collections.sort(parmList);
			ArrayList<String> prefix = new ArrayList<String>();
			int indent = 2;
			for(int i=0; i<parmList.size(); i++)
			{
				String[] parm = parmList.get(i).split("\\.");
				
				// Display opening parm tags
				while(prefix.size() < parm.length-1)
				{
					String tag = parm[prefix.size()];
					for(int j=0; j<indent; j++) output.append(" "); indent+=2;
					output.append("<"+tag+">\n");
					prefix.add(tag);
				}
				
				// Display parm value
				for(int j=0; j<indent; j++) output.append(" ");
				output.append("<"+parm[parm.length-1]+">"+parms.get(parmList.get(i))+"</"+parm[parm.length-1]+">\n");
				
				// Display closing parm tags
				String nextParm[] = i<parmList.size()-1 ? parmList.get(i+1).split("\\.") : new String[0];
				int simDepth = 0;
				for(simDepth=0; simDepth<Math.min(parm.length, nextParm.length); simDepth++)
					if(!parm[simDepth].equals(nextParm[simDepth])) break;
				while(prefix.size() > simDepth)
				{
					String tag = parm[prefix.size()-1];
					indent-=2; for(int j=0; j<indent; j++) output.append(" ");
					output.append("</"+tag+">\n");
					prefix.remove(prefix.size()-1);
				}
			}
			
			return output.toString();
		}
	}
	
	/** Stores a listing of parameters */
	static private Parms parms = null;	
	
	/** Initializes the configuration manager with all defined configuration settings */
	static
	{	
		String configString = "";
		
		// Load tool information from file
		try {
			// Pull the entire file into a string
			InputStream configStream = new FileInputStream(new File("org.org.mitre.harmony.config.xml"));
			BufferedReader in = new BufferedReader(new InputStreamReader(configStream));	
			StringBuffer buffer = new StringBuffer("");
			String line; while((line=in.readLine())!=null) buffer.append(line);
			in.close();
			
			// Parse out the configuration parameters
			Matcher configMatcher = configPattern.matcher(buffer);
			if(configMatcher.find()) configString = configMatcher.group(1);
		}
		catch(Exception e) {}

		parms = new Parms(configString);
	}
	
	/** Returns the specified parameter */
	static public String getParm(String name)
		{ return parms.get(name); }
	
	/** Returns the specified integer parameter */
	static public Integer getIntegerParm(String name)
	{
		try { return Integer.parseInt(parms.get(name)); } catch(Exception e) {}
		return null;
	}
	
	/** Returns the specified array parameter */
	static public ArrayList<String> getArray(String name)
	{
		ArrayList<String> array = new ArrayList<String>();
		String value = getParm(name);
		if(value!=null)
		{
			value = value.substring(1,value.length()-1);
			if(value.length()>0)
			{
				value = value.replaceAll(",(?=[^\\[]*\\])", "|");
				for(String item : value.split(","))
					array.add(item.trim().replaceAll("\\|",","));
			}
		}
		return array;
	}

	/** Returns the specified integer array parameter */
	static public ArrayList<Integer> getIntegerArray(String name)
	{
		ArrayList<Integer> array = new ArrayList<Integer>();
		for(String item : getArray(name))
			try { array.add(Integer.parseInt(item)); } catch(Exception e) {}
		return array;
	}

	/** Returns the specified hash set parameter */
	static public HashMap<Integer,String> getHashMap(String name)
	{
		HashMap<Integer,String> hashMap = new HashMap<Integer,String>();
		for(String item : getArray(name))
		{
			Integer key = Integer.parseInt(item.replaceAll("=.*","").trim());
			String value = item.replaceAll(".*=","").trim();
			try { hashMap.put(key,value); } catch(Exception e) {}
		}
		return hashMap;
	}	
	
	/** Sets the parameter for the specified tool */
	static public void setParm(String name, String value)
		{ parms.set(name,value); save(); }
	
	/** Saves the tool file */
	static private void save()
	{
		File configFile = new File("org.org.mitre.harmony.config.xml");
		File tempConfigFile = new File(configFile.getParentFile(),"org.org.mitre.harmony.config.xml.tmp");
		try {
			// Create a temporary file to output to
			BufferedWriter out = new BufferedWriter(new FileWriter(tempConfigFile));	

			// Write out global parameters
			out.write("<config>\n");
			out.write(parms.toString());
			out.write("</config>\n");
			out.close();
			
			// Replace the tool file with the new tool file
			configFile.delete();
			tempConfigFile.renameTo(configFile);
		}
		catch(Exception e)
			{ System.out.println("(E)ConfigManager:save - The configuration parameters have failed to save!\n"+e.getMessage()); }		
	}
}