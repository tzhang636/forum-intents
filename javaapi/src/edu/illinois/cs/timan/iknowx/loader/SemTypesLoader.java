package edu.illinois.cs.timan.iknowx.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.timan.iknowx.constants.Constants;

public class SemTypesLoader {
	public static Map<String, String> loadAbbv2Full() throws Exception {
		Map<String, String> abbv2Full = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(Constants.semTypesFile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\\|");
			abbv2Full.put(parts[0], parts[2]);
		}
		br.close();
		return abbv2Full;
	}
	
	public static Map<String, String> loadAbbv2Code() throws Exception {
		Map<String, String> abbv2Code = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(Constants.semTypesFile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\\|");
			abbv2Code.put(parts[0], parts[1]);
		}
		br.close();
		return abbv2Code;
	}
	
	public static Map<String, String> loadCode2GroupAbbv() throws Exception {
		Map<String, String> code2GroupAbbv = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(Constants.semGroupsFile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\\|");
			code2GroupAbbv.put(parts[2], parts[0]);
		}
		br.close();
		return code2GroupAbbv;
	}
}